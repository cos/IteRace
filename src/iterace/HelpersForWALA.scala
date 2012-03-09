package iterace
import scala.collection.JavaConversions._
import WALAConversions._

class HelpersForWALA(pa: PointerAnalysis) {
  import pa._
  import iterace.memoizeRec
  
  def allInstructions = {
    callGraph.map(n => n.getIR().iterateAllInstructions().map(i => (n, i))).flatten.toSet
  }

  private def instructionsReachableFromRec(rec: N => Set[(N,I)])(n: N): Set[(N,I)] = {
    val localStatements = n.getIR().iterateAllInstructions().map({(n,_)}).toSet 
    
    
    return localStatements ++ callGraph.getSuccNodes(n).map(m => rec(m)).flatten
  }
  val statementsReachableFrom: N => Set[(N,I)] = memoizeRec(instructionsReachableFromRec)
}