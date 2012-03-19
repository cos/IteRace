package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection._
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import com.ibm.wala.ipa.callgraph.ContextKey
import com.ibm.wala.ipa.callgraph.ContextItem
import com.ibm.wala.dataflow.IFDS.PathEdge
import com.ibm.wala.properties.WalaProperties
import iterace.oldjava.AnalysisScopeBuilder

class IteRace(startClass: String, startMethod: String, analysisScope: AnalysisScopeBuilder) {
  val pa = new RacePointerAnalysis(startClass, startMethod, analysisScope)
  import pa._
  
  val possibleRaces = new PossibleRaces(pa)()

  private val lockSet = new LockSet(pa)

  val races = new FilterByMayAlias(pa, lockSet)(possibleRaces)
  
  def racesAsRaceSet = new ProgramRaceSet(races)
}

class AnalysisException(m: String) extends Throwable {

}