package iterace
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

case class Lock(p: P) {
  def prettyPrint = "L: " + p.prettyPrint
}

object LockSet {
  type LockDomain = TabulationDomain[Lock, SS]
}

class LockSet(pa: PointerAnalysis) {
  import pa._

  val supergraph = ICFGSupergraph.make(pa.callGraph, pa.analysisCache)
  
  /**
   * Get all the locks that appear in the loop
   */
  def getLocks(l: Loop): Set[Lock] = {
    (for (
      n <- asScalaIterator(DFS.iterateDiscoverTime(callGraph, l.n));
      i <- n.instructions
    ) yield {
      i match {
        case i: SSAMonitorInstruction => Lock(P(n, i.getRef()))
        case _ => null
      }
    }) filter { _ != null } toSet
  }

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
      override def getCallFlowFunction(src: SS, dest: SS, ret: SS) = IdentityFlowFunction.identity();
      override def getCallNoneToReturnFlowFunction(src: SS, dest: SS) = IdentityFlowFunction.identity();
      override def getCallToReturnFlowFunction(src: SS, dest: SS) = SingletonFlowFunction.create(0);
      override def getNormalFlowFunction(src: SS, dest: SS) = {
        src.getLastInstruction() match { // I don't know why it is "last", just some ugly interface, matching to generally
          case i: SSAMonitorInstruction => {
            if (i.isMonitorEnter()) {
              val theSet = SparseIntSet.singleton(locksDomain.getMappedIndex(Lock(P(src.getNode(), i.getRef()))))
              VectorKillFlowFunction.make(theSet)
            } else {
              // for some reason the their gen function needs 0 explicitly 
              // (a nice way to waste 3 hours figuring why the algorithm doesn't work)
              val theSet = SparseIntSet.pair(0, locksDomain.getMappedIndex(Lock(P(src.getNode(), i.getRef()))))
              VectorGenFlowFunction.make(theSet)
            }
          }
          case _ => IdentityFlowFunction.identity()
        }
      }
      override def getReturnFlowFunction(call: SS, src: SS, dest: SS) = IdentityFlowFunction.identity();
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