package iterace.pointeranalysis

import scala.util.matching.Regex
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import iterace.util.WALAConversions._
import scala.collection._
import iterace.util.S
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.properties.WalaProperties
import iterace.util.ArrayContents
import com.ibm.wala.util.collections.Filter
import iterace.datastructure.LockSets
import iterace.datastructure.Lock
import iterace.IteRaceOption

class RacePointerAnalysis(startClass: String, startMethod: String, analysisScope: AnalysisScopeBuilder, options: Set[IteRaceOption])
  extends PointerAnalysis(startClass, startMethod, analysisScope, options) {

  lazy val allInstructions = {
    callGraph.map(n => n.getIR().iterateAllInstructions().map(i => (n, i))).flatten.toSet
  }

  // we return an interable but we know it is actually a set
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
      if (options.contains(IteRaceOption.TwoThreadModel))
        callGraph.getSuccNodes(l.n).find(n => n.c(Iteration) match {
          case BetaIteration => true
          case _ => false
        }).get
      else
      	alphaIterationN
  }

  implicit def iWithField(i: I) = new {
    lazy val f: Option[F] = i match {
      case i: ArrayReferenceI => Some(ArrayContents.v)
      case i: AccessI => Option(cha.resolveField(i.getDeclaredField()))
      case _ => None
    }
  }

  implicit def iWithReferencePointer[T <: I](s: S[T]) = new {
    lazy val refP: Option[P] = s.i match {
      case i: AccessI if !i.isStatic => Some(P(s.n, i.getRef()))
      case i: ArrayReferenceI => Some(P(s.n, i.getArrayRef()))
      case i: InvokeI if !i.isStatic => Some(P(s.n, i.getReceiver()))
      case _ => None
    }
  }

  implicit def invokeIWithMethod(i: InvokeI) = new {
    lazy val m = cha.resolveMethod(i.getDeclaredTarget())
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