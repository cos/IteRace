package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection._
import scala.collection.JavaConversions._
import wala.WALAConversions._
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
import wala.AnalysisScopeBuilder
import iterace.stage._
import iterace.pointeranalysis._
import iterace.datastructure.LockSets
import iterace.datastructure.MayAliasLockConstructor
import iterace.IteRaceOption._
import sppa.util._

class IteRace private (
  startClass: String, startMethod: String, analysisScope: AnalysisScopeBuilder,
  options: Set[IteRaceOption]) {

  debug("Options: " + options.mkString(", "))

  log.startTimer("pointer-analysis");
  val pa = new RacePointerAnalysis(startClass, startMethod, analysisScope, options)
  import pa._
  log.endTimer
  log.startTimer("potential-races")
  private val potentialRaces = new PotentialRaces(pa)()
  log.endTimer
  log("potential-races", potentialRaces.size)
//  potentialRaces.children.foreach { _.children.foreach(set => debug(set.prettyPrint)) }
  private var currentRaces = potentialRaces

  log.startTimer("locksets")
  val lockSetMapping = new LockSets(pa, new MayAliasLockConstructor(pa))
  log.endTimer
//  log("locks",)
  
  val filterByLockMayAlias = new FilterByLockMayAlias(pa, lockSetMapping)

  if (options(DeepSynchronized)) {
    log.startTimer("deep-synchronized")
    currentRaces = filterByLockMayAlias(currentRaces)
    log.endTimer
    log("deep-synchronized", currentRaces.size)
  }

  if (options(IteRaceOption.BubbleUp)) {
    log.startTimer("bubble-up")
    currentRaces = stage.BubbleUp(pa)(currentRaces)
    log.endTimer
    log("bubble-up", currentRaces.size)
  }

  if (options(AppLevelSynchronized)) {
    log.startTimer("app-level-synchronized")
    currentRaces = filterByLockMayAlias(currentRaces)
    log.endTimer
    log("app-level-synchronized", currentRaces.size)
  }

  val races = currentRaces
  log("races", races.size)

  debug(log.entries)
  debug(" \n\n ******************************************************** \n\n  ")
  debug(races.prettyPrint)
}

object IteRace {
  def apply(
    startClass: String,
    startMethod: String,
    analysisScope: AnalysisScopeBuilder,
    options: Set[IteRaceOption] = Set(DeepSynchronized, IteRaceOption.BubbleUp)) =
    new IteRace(startClass, startMethod, analysisScope, options)
}

class AnalysisException(m: String) extends Throwable {

}