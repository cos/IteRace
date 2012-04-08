package iterace.datastructure

import scala.collection._
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
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
import iterace.util.S
import iterace.pointeranalysis._
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.ibm.wala.util.collections.Filter
import com.ibm.wala.ssa.SSAInvokeInstruction
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction
import com.ibm.wala.util.graph.GraphUtil
import com.ibm.wala.ipa.callgraph.impl.PartialCallGraph

abstract class Lock extends PrettyPrintable

object LockSet {
  type LockDomain = TabulationDomain[Lock, SS]
}

object IExplodedBasicBlock {
  def unapply(bb: IExplodedBasicBlock): Option[(I)] = {
    Some(bb.getInstruction())
  }
}

trait LockConstructor {
  def apply(p: P): Option[Lock]
  def apply(c: C): Some[Lock]
}

object threadSafeFilter extends Filter[N] { def accepts(n: N): Boolean = n.getContext() != THREAD_SAFE }

class LockSet(pa: RacePointerAnalysis, lockConstructor: LockConstructor) {
  import pa._

  val supergraph = {
    val filterdCallGraph = PartialCallGraph.make(callGraph,callGraph.getEntrypointNodes(), threadSafeFilter)
    ICFGSupergraph.make(filterdCallGraph , pa.analysisCache)
  }

  /**
   * Get all the locks that appear in the loop
   */
  val locksForLoop: mutable.Map[Loop, Set[Lock]] = mutable.Map()
  def getLocks(l: Loop): Set[Lock] = locksForLoop.getOrElseUpdate(l,

    DFS.getReachableNodes(callGraph, Set(l.n), threadSafeFilter) flatMap (n => {

      // synchronized method locks
      val nLockSet: Set[Lock] = if (n.m.isSynchronized())
        if (n.m.isStatic()) Set(lockConstructor(n.m.getDeclaringClass()).get) // for now, consider the convention that 0 is a pointer to the class
        else lockConstructor(P(n, 1)) map { Set(_) } getOrElse Set() // lock on "this"
      else Set()

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
    }) toSet)

  def getLockSetMapping(l: Loop): S[I] => Set[Lock] = getLockSetMapping(l, getLocks(l))

  def getLockSetMapping(l: Loop, s: Set[Lock]): S[I] => Set[Lock] = {
    val locksDomain = new UnorderedDomain[Lock, SS]()
    locksDomain.add(null); // so that we have a 0 value
    s.foreach({ locksDomain.add(_) })

    getLockSetMapping(l, locksDomain)
  }

  /**
   * get the lockset for any instruction in the program
   */
  def getLockSetMapping(l: Loop, locksDomain: LockSet.LockDomain): S[I] => Set[Lock] = {

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

    val solver = TabulationSolver.make(problem)
    solver.solve()
    def funct(s: S[I]) = {
      val icfg = supergraph.getICFG().getCFG(s.n)
      val explodedBasicBlock = icfg.getBlockForInstruction(s.irNo)
      val bbic = new BasicBlockInContext(s.n, explodedBasicBlock)
      val intSet = solver.getResult(bbic)
      val notHeldLocks = intSet.map({ locksDomain.getMappedObject(_) }).toSet
      locksDomain.elements.toSet.&~(notHeldLocks).&~(Set(null)) // have to figure out why it doesn't propagate 0 here
    }
    return funct _
  }
}