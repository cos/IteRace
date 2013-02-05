package iterace.pointeranalysis

import scala.util.matching.Regex
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import edu.illinois.wala.Facade._
import scala.collection._
import edu.illinois.wala.S
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.properties.WalaProperties
import com.ibm.wala.util.collections.Filter
import iterace.datastructure.LockSets
import iterace.datastructure.Lock
import iterace.IteRaceOption
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.callgraph.ContextSelector
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import edu.illinois.wala.ipa.callgraph.AnalysisOptions
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder

class RacePointerAnalysis(options: AnalysisOptions, val iteraceOptions: Set[IteRaceOption])
  extends FlexibleCallGraphBuilder(options) {

  override def cs: ContextSelector = new LoopContextSelector(iteraceOptions, cha, instanceKeys)
  
  // Hooks
  override def policy = { import ZeroXInstanceKeys._;  ALLOCATIONS }

  // we return an Iterable but we know it is actually a set
  def statementsReachableFrom(n: N, filter: N => Boolean = null): Iterable[S[I]] = {
    val reachableNs = if (filter == null)
      DFS.getReachableNodes(callGraph, Set(n))
    else
      DFS.getReachableNodes(callGraph, Set(n), new Filter[N] { def accepts(n: N): Boolean = filter(n) })

    (reachableNs map (nn => nn.instructions.map(i => S(nn, i)).toSet)).flatten
  }

  // I think it is very inefficient 
  lazy val loops: immutable.Set[Loop] = callGraph collect
    { case n: N if n.getContext().get(Loop) != null => n.getContext().get(Loop).asInstanceOf[Loop] } toSet

  lazy val parLoops =
    loops filter { !_.n.m.toString().contains("Seq") }

  lazy val seqLoops = loops filter { _.n.m.toString().contains("Seq") }

  implicit def loopWithIterations(l: Loop) = new {
    lazy val alphaIterationN =
      callGraph.getSuccNodes(l.n).find(n => n.c(Iteration) match {
        case AlphaIteration => true
        case _ => false
      }).get

    lazy val betaIterationN =
      if (iteraceOptions.contains(IteRaceOption.TwoThreads))
        callGraph.getSuccNodes(l.n).find(n => n.c(Iteration) match {
          case BetaIteration => true
          case _ => false
        }).get
      else
        alphaIterationN
  }

  implicit def accessIWithReferencePointer[T <: AccessI](s: S[T]) = new {
    lazy val isStatic: Boolean = s.i isStatic
  }

  def firstIteration(n: N): Boolean =
    n.c(Iteration) match {
      case AlphaIteration => true
      case _ => false
    }

  def inLoop(n: N): Boolean = n.getContext().get(Loop) != null

  def inParallel(n: N): Boolean = {
    val seqPattern = ".*Seq.*".r
    n.c(Iteration) match {
      case BetaIteration => true
      case _ => false
    }
  }

  var lockMapping: Option[LockSets] = None

  implicit def sWithLockSet[T <: I](s: S[T]) = new {
    lazy val lockset: Option[Set[Lock]] = lockMapping map { _.getLockSet(s) }
  }

}