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
import iterace.stage._
import iterace.pointeranalysis._
import iterace.datastructure.LockSets
import iterace.datastructure.MayAliasLockConstructor
import iterace.IteRaceOption._
import sppa.util._
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import wala.AnalysisOptions

class IteRace private (
  options: AnalysisOptions,
  iteRaceOptions: Set[IteRaceOption]) {

  def this(options: AnalysisOptions) =
    this(options, Set(DeepSynchronized, IteRaceOption.BubbleUp))

  debug("Options: " + iteRaceOptions.mkString(", "))

  log.startTimer("pointer-analysis");

  val pa = new RacePointerAnalysis(options, iteRaceOptions)
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

  if (iteRaceOptions(DeepSynchronized)) {
    log.startTimer("deep-synchronized")
    currentRaces = filterByLockMayAlias(currentRaces)
    log.endTimer
    log("deep-synchronized", currentRaces.size)
  }

  if (iteRaceOptions(IteRaceOption.BubbleUp)) {
    log.startTimer("bubble-up")
    currentRaces = stage.BubbleUp(pa)(currentRaces)
    log.endTimer
    log("bubble-up", currentRaces.size)
  }

  if (iteRaceOptions(AppLevelSynchronized)) {
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
  def apply(options: AnalysisOptions, iteRaceoOptions: Set[IteRaceOption]) = new IteRace(options, iteRaceoOptions)

  def apply(options: AnalysisOptions) = new IteRace(options, IteRaceOptions.all)
}

class AnalysisException(m: String) extends Throwable {

}