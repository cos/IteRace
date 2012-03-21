package iterace

import iterace.oldjava.WalaAnalysisStart
import scala.util.matching.Regex
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
import scala.collection._
import iterace.util.S
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.properties.WalaProperties
import iterace.oldjava.AnalysisScopeBuilder

class RacePointerAnalysis(startClass: String, startMethod: String, analysisScope: AnalysisScopeBuilder) 
	extends PointerAnalysis(startClass, startMethod, analysisScope) {

  lazy val allInstructions = {
    callGraph.map(n => n.getIR().iterateAllInstructions().map(i => (n, i))).flatten.toSet
  }

  def statementsReachableFrom(n: N): Set[S[I]] = 
    DFS.getReachableNodes(callGraph, Set(n)) map (nn => nn.instructions.map( i => S(nn,i)).toSet) flatten 
  
  lazy val loops: immutable.Set[Loop] = {
    callGraph collect
      { case n: N if n.getContext().isInstanceOf[LoopContext] => n.getContext().asInstanceOf[LoopContext].loop } toSet
  }
  
  lazy val parLoops = loops filter {! _.n.m.toString().contains("Seq")}
  lazy val seqLoops = loops filter {_.n.m.toString().contains("Seq")}
  
  implicit def loopWithIterations(l: Loop) = new {
    lazy val alphaIterationN = {
      callGraph.getSuccNodes(l.n).find(n => n.getContext().get(LoopIteration).asInstanceOf[LoopIteration].alpha).get
    }
    lazy val betaIterationN = {
      callGraph.getSuccNodes(l.n).find(n => !n.getContext().get(LoopIteration).asInstanceOf[LoopIteration].alpha).get
    }
  }
  
  implicit def accessIWithReferencePointer[T <: AccessI](s :S[T]) = new {
    lazy val refP: P = P(s.n, s.i.getRef())
  }
  
  def firstIteration(n: N): Boolean = {
    n.getContext() match {
      case LoopContext(_, true) => true
      case _ => false
    }
  }

  def inLoop(n: N): Boolean = {
    n.getContext() match {
      case LoopContext(_, _) => true
      case _ => false
    }
  }

  def inParallel(n: N): Boolean = {
    val seqPattern = ".*Seq.*".r
    n.getContext() match {
      case LoopContext(N(_, M(_, seqPattern())), _) => false
      case _ => true
    }
  }  
  
}