package iterace.datastructure

import scala.collection._
import scala.collection.JavaConversions._
import edu.illinois.wala.Facade._
import com.ibm.wala.dataflow.IFDS.PartiallyBalancedTabulationProblem
import com.ibm.wala.dataflow.IFDS.UnorderedDomain
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph
import com.ibm.wala.dataflow.IFDS.PathEdge
import com.ibm.wala.dataflow.IFDS.IPartiallyBalancedFlowFunctions
import com.ibm.wala.ipa.slicer.ReachabilityFunctions
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.ssa.SSAMonitorInstruction
import com.ibm.wala.util.collections.ObjectArrayMapping
import com.ibm.wala.dataflow.IFDS.TabulationDomain
import com.ibm.wala.dataflow.IFDS.IdentityFlowFunction
import com.ibm.wala.dataflow.IFDS.VectorKillFlowFunction
import com.ibm.wala.util.intset.SparseIntSet
import com.ibm.wala.dataflow.IFDS.VectorGenFlowFunction
import com.ibm.wala.dataflow.IFDS.PartiallyBalancedTabulationSolver
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.dataflow.IFDS.SingletonFlowFunction
import com.ibm.wala.dataflow.IFDS.TabulationProblem
import com.ibm.wala.dataflow.IFDS.IFlowFunctionMap
import com.ibm.wala.dataflow.IFDS.TabulationSolver
import com.ibm.wala.util.intset.IntSet
import com.ibm.wala.dataflow.IFDS.IMergeFunction
import edu.illinois.wala.S
import iterace.pointeranalysis._
import com.ibm.wala.util.collections.Filter
import com.ibm.wala.ssa.SSAInvokeInstruction
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction
import com.ibm.wala.util.graph.GraphUtil
import com.ibm.wala.ipa.callgraph.impl.PartialCallGraph
import sppa.util._
import edu.illinois.wala.PrettyPrintable
import edu.illinois.wala.classLoader.M
import edu.illinois.wala.classLoader.C
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import edu.illinois.wala.ipa.callgraph.propagation.P

abstract class Lock extends PrettyPrintable

object LockSets {
  type LockDomain = TabulationDomain[Lock, SS]
}

object IExplodedBasicBlock {
  import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
  def unapply(bb: IExplodedBasicBlock): Option[(I)] = {
    Some(bb.getInstruction())
  }
}

trait LockConstructor {
  def apply(p: P): Option[Lock]
  def apply(c: C): Some[Lock]
}

//object threadSafeFilter extends Filter[N] { def accepts(n: N): Boolean = !threadSafe(n.m) }

class LockSets(pa: RacePointerAnalysis, lockConstructor: LockConstructor) {
  import pa._
  
  val callGraph = pa.getCallGraph()
  
  val supergraph = ICFGSupergraph.make(callGraph, cache)
  //    val filterdCallGraph = PartialCallGraph.make(callGraph,callGraph.getEntrypointNodes())

  /**
   * Get all the locks that appear in the loop
   */
  val locksForLoop: mutable.Map[Loop, Set[Lock]] = mutable.Map()
  def getLocks(l: Loop): Set[Lock] =
    locksForLoop.getOrElseUpdate(l,
      DFS.getReachableNodes(callGraph, Set(l.n)) map (n => {

        if (threadSafe(n))
          Set[Lock]()
        else {
          // synchronized method locks
          val nLockSet: Set[Lock] =
            if (n.m.isSynchronized())
              if (n.m.isStatic())
                Set(lockConstructor(n.m.getDeclaringClass()).get) // lock on "class object"
              else
                lockConstructor(P(n, 1)) map { Set(_) } getOrElse Set() // lock on "this"
            else
              Set[Lock]()

          // synchronized locks block
          nLockSet ++ (
            n.instructions collect {
              case i: SSAMonitorInstruction => lockConstructor(P(n, i.getRef()))
              case i: InvokeI => {
                i.m match {
                  case null => None
                  case M(C("java/util/concurrent/locks", "ReentrantLock"), "lock()V") => lockConstructor(P(n, i.getUse(0)))
                  case _ => None
                }
              }
            }
            collect { case Some(l) => l })
          // will add ReentrantLock locks here at some point
        }
      }) flatten)

  //  should move these at some point
  def getLoopFor(n: N): Option[Loop] =
    n.c(Loop) match {
      case l: Loop => Some(l)
      case _ => None
    }

  implicit def statementWithLoop(s: S[_]) = new {
    lazy val l: Option[Loop] = getLoopFor(s.n)
  }

  def getLockSet[T <: I](s: S[T]): Set[Lock] = getLockSetMapping(s.l.get)(s)

  val cachedFunctions: mutable.Map[Loop, S[I] => immutable.Set[Lock]] = mutable.ListMap()

  def getLockSetMapping(l: Loop): S[I] => immutable.Set[Lock] =
    cachedFunctions.getOrElseUpdate(l, { getLockSetMapping(l, getLocks(l)) })

  private def getLockSetMapping(l: Loop, s: Set[Lock]): S[I] => immutable.Set[Lock] = {
    val locksDomain = new UnorderedDomain[Lock, SS]()
    locksDomain.add(null); // so that we have a 0 value
    s.foreach({ locksDomain.add(_) })

    getLockSetMapping(l, locksDomain)
  }

  /**
   * get the lockset for any instruction in the program
   */
  private def getLockSetMapping(l: Loop, locksDomain: LockSets.LockDomain): S[I] => immutable.Set[Lock] = {

    debug("locksets-lattice-size   ----    " + l.n + "     ---- " + locksDomain.size)

    object flowFunctions extends IFlowFunctionMap[SS] {
      override def getCallFlowFunction(src: SS, dest: SS, ret: SS) = identity
      override def getCallNoneToReturnFlowFunction(src: SS, dest: SS) = identity
      override def getCallToReturnFlowFunction(src: SS, dest: SS) = src.getDelegate() match {
        // reentrant locks
        case IExplodedBasicBlock(i: InvokeI) => {
          i.m match {
            case null => identity

            case M(C("java/util/concurrent/locks", "ReentrantLock"), "lock()V") =>
              lockConstructor(P(src.getNode(), i.getUse(0))) map { getLockEnterFlowFunction(_) } getOrElse identity

            case M(C("java/util/concurrent/locks", "ReentrantLock"), "unlock()V") =>
              lockConstructor(P(src.getNode(), i.getUse(0))) map { getLockExitFlowFunction(_) } getOrElse identity

            case _ => identity
          }
        }
      }
      override def getNormalFlowFunction(src: SS, dest: SS) = {

        val n: N = src.getNode()

        src.getDelegate() match {
          // monitor instruction; i.e., synchronized blocks
          case IExplodedBasicBlock(i: MonitorI) => {
            lockConstructor(P(n, i.getRef())) match {
              case Some(l) => getLockFlowFunction(i.isMonitorEnter(), l)
              case _ => identity
            }
          }
          // synchronized methods
          case bb: IExplodedBasicBlock if n.m.isSynchronized() && (bb.isEntryBlock() || bb.isExitBlock()) => {
            val lock = if (n.m.isStatic())
              lockConstructor(bb.getMethod().getDeclaringClass())
            else
              lockConstructor(P(n, 1))

            lock match {
              case Some(l) => getLockFlowFunction(bb.isEntryBlock(), l)
              case _ => identity
            }
          }

          case _ => identity
        }
      }

      override def getReturnFlowFunction(call: SS, src: SS, dest: SS) = identity

      private def getLockFlowFunction(isEnter: Boolean, lock: Lock) =
        if (isEnter) getLockEnterFlowFunction(lock) else getLockExitFlowFunction(lock)

      private def getLockEnterFlowFunction(lock: Lock) = {
        val theSet = SparseIntSet.singleton(locksDomain.getMappedIndex(lock))
        VectorKillFlowFunction.make(theSet)
      }
      private def getLockExitFlowFunction(lock: Lock) = {
        // for some reason the their gen function needs 0 explicitly 
        // (a nice way to waste 3 hours figuring why the algorithm doesn't work)
        val theSet = SparseIntSet.pair(0, locksDomain.getMappedIndex(lock))
        VectorGenFlowFunction.make(theSet)
      }
      private val identity = IdentityFlowFunction.identity()
    }

    object problem extends TabulationProblem[SS, N, Lock] {
      override def getDomain = locksDomain
      override def getFunctionMap = flowFunctions
      override def getMergeFunction = null
      override def getSupergraph = supergraph
      override def initialSeeds = {
        val initialNode = supergraph.getEntriesForProcedure(l.n)(0)
        locksDomain map { locksDomain.getMappedIndex(_) } collect
          { case x: Int => PathEdge.createPathEdge(initialNode, x, initialNode, x) }
      }
    }

    val allLocks = locksDomain.toSet
    val solver = TabulationSolver.make(problem)
    solver.solve()
    def funct(s: S[I]) = {
      val icfg = supergraph.getICFG().getCFG(s.n)
      val notHeldLocks = s.irNo map { i =>
        val explodedBasicBlock = icfg.getBlockForInstruction(i)
        val bbic = new BasicBlockInContext(s.n, explodedBasicBlock)
        val intSet = solver.getResult(bbic)
        intSet.map({ locksDomain.getMappedObject(_) }).toSet
      } getOrElse allLocks
      allLocks.&~(notHeldLocks).&~(Set(null)) // have to figure out why it doesn't propagate 0 here
    }
    (funct _)
  }
  parLoops.foreach { getLockSetMapping(_) }
}