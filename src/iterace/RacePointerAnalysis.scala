package iterace

import iterace.oldjava.WalaAnalysisStart
import scala.util.matching.Regex
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
import scala.collection._
import iterace.util.S
import com.ibm.wala.util.graph.traverse.DFS

class RacePointerAnalysis(startClass: String, startMethod: String, dependencies: List[String]) 
	extends PointerAnalysis(startClass, startMethod, dependencies) {

  def allInstructions = {
    callGraph.map(n => n.getIR().iterateAllInstructions().map(i => (n, i))).flatten.toSet
  }

  def statementsReachableFrom(n: N): Set[S[I]] = 
    DFS.getReachableNodes(callGraph, Set(n)) map (nn => nn.instructions.map({S(nn,_)}).toSet) flatten 
  
  def getLoops(): immutable.Set[Loop] = {
    callGraph collect
      { case n: N if n.getContext().isInstanceOf[LoopContext] => n.getContext().asInstanceOf[LoopContext].loop } toSet
  }
  
  implicit def loopWithIterations(l: Loop) = new {
    lazy val alphaIterationN = {
      callGraph.getSuccNodes(l.n).filter(n => n.getContext().get(LoopIteration).asInstanceOf[LoopIteration].alpha)
    }
    lazy val betaIterationN = {
      callGraph.getSuccNodes(l.n).filter(n => !n.getContext().get(LoopIteration).asInstanceOf[LoopIteration].alpha)
    }
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