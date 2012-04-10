package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
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
import iterace.datastructure.LockSet
import iterace.datastructure.MayAliasLockConstructor

class IteRace private (
  startClass: String, startMethod: String, analysisScope: AnalysisScopeBuilder,
  options: Set[IteRaceOption]) {

  log.startTimer("pointer analysis");
  val pa = new RacePointerAnalysis(startClass, startMethod, analysisScope)
  import pa._
  log.endTimer

  log.startTimer("possible races")
  private val potentialRaces = new PotentialRaces(pa)()
  log.endTimer

  log(potentialRaces.prettyPrint())
  log("potential races : +" + potentialRaces.size)
  
  private var currentRaces = potentialRaces

  val filterByLockMayAlias = new FilterByLockMayAlias(pa, new LockSet(pa, new MayAliasLockConstructor(pa)))
  
  if (options(FilterByLockMayAlias)) {
    log.startTimer("lock may alias")
    currentRaces = filterByLockMayAlias(currentRaces)
    log.endTimer
    log("lock may alias resulted in : " + currentRaces.size + " races")
  }

  if (options(BubbleUp)) {
    log.startTimer("bubble up")
    currentRaces = BubbleUp(pa)(currentRaces)
    log.endTimer
    log("bubble up resulted in : " + currentRaces.size + " races")
  }

  if (options(FilterByLockMayAlias)) {
    log.startTimer("lock may alias")
    currentRaces = filterByLockMayAlias(currentRaces)
    log.endTimer
    log("lock may alias resulted in : " + currentRaces.size + " races")
  }

  val races = currentRaces

  log(" \n\n ******************************************************** \n\n  ")
  log(races.prettyPrint)
}

object IteRace {
  def apply(
    startClass: String,
    startMethod: String,
    analysisScope: AnalysisScopeBuilder,
    options: Set[IteRaceOption] = Set(FilterByLockMayAlias, BubbleUp)) =
    new IteRace(startClass, startMethod, analysisScope, options)
}

trait IteRaceOption

class AnalysisException(m: String) extends Throwable {

}