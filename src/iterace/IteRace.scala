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
import iterace.util.log
import iterace.pointeranalysis.AnalysisScopeBuilder
import iterace.stage._
import iterace.pointeranalysis._

class IteRace private (
    startClass: String, startMethod: String, analysisScope: AnalysisScopeBuilder,
    stages: Seq[StageConstructor],
    options: Set[String]) {
  
  log.startTimer("pointer analysis");
  val pa = new RacePointerAnalysis(startClass, startMethod, analysisScope)
  import pa._
  log.endTimer
  
  log.startTimer("possible races")
  private val potentialRaces = new PotentialRaces(pa)()
  log.endTimer
  log(potentialRaces.size)

  log.startTimer("activating stages - should be quick");
  // we only activate one stage for each constructor type
  // if a stage appears twice, it is only activated once but can be used as many times as necessary
  val activatedStagesSets = {
    val listOfUniqueStages = stages.toSet
    val listOfUniqueActivatedStages = listOfUniqueStages map {_(pa)}
    (listOfUniqueStages zip listOfUniqueActivatedStages) toMap
  }
  val activatedStages = stages map {activatedStagesSets(_)}
  log.endTimer
  
  private var currentRaces = potentialRaces
  activatedStages foreach(stage => {
    log.startTimer(stage.getClass().toString())
    currentRaces = stage(currentRaces)
    log.endTimer
    log(stage.getClass().toString() +" resulted in : "+currentRaces.size+" races")
    } )
  
  val races = currentRaces
}

object IteRace {
  def apply(
    startClass: String, startMethod: String, analysisScope: AnalysisScopeBuilder,
    stages: Seq[StageConstructor] = Seq(FilterByLockMayAlias, BubbleUp, FilterByLockMayAlias),
    options: Set[String] = Set()) = new IteRace(startClass, startMethod, analysisScope, stages, options)
}

class AnalysisException(m: String) extends Throwable {

}