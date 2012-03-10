package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.JavaConversions._
import WALAConversions._
import com.ibm.wala.util.graph.traverse.DFS
import iterace.LoopContextSelector.LoopN
import iterace.LoopContextSelector.LoopContext
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
 
class IteRace(startClass: String, startMethod: String, dependencies: List[String]) {
  val pa = new PointerAnalysis(startClass, startMethod, dependencies)
  import pa._
  
  val races = new PossibleRaces(pa).races
}