package iterace
import scala.collection.JavaConversions._
import iterace.WALAConversions._
import scala.collection._

class PAHelpers(pa: PointerAnalysis) {
  import pa._
  import iterace.memoizeRec
  
  def allInstructions = {
    callGraph.map(n => n.getIR().iterateAllInstructions().map(i => (n, i))).flatten.toSet
  }

  private def instructionsReachableFromRec(rec: N => Set[S[I]])(n: N): Set[S[I]] = {
    val localStatements = n.getIR().iterateAllInstructions().map({S(n,_)}).toSet 
    return localStatements ++ callGraph.getSuccNodes(n).map(m => rec(m)).flatten
  }
  val statementsReachableFrom: N => Set[S[I]] = memoizeRec(instructionsReachableFromRec)
  
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