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
import iterace.util.log

class IteRace(startClass: String, startMethod: String, analysisScope: AnalysisScopeBuilder) {
  
  log.startTimer("pointer analysis");
  val pa = new RacePointerAnalysis(startClass, startMethod, analysisScope)
  import pa._
  log.endTimer
  
  // -------
  // The analysis steps
  log.startTimer("possible races")
  val possibleRaces = new PossibleRaces(pa)()
  log.endTimer
  log(possibleRaces.size)
  possibleRaces foreach ( r => println(r.prettyPrint))
  
//  log.startTimer("filter known thread-safe")
//  private val filterByKnownThreadSafe = new FilterByKnownThreadSafe  
//  val filteredPossibleRaces = filterByKnownThreadSafe(possibleRaces).asInstanceOf[ProgramRaceSet]
//  log.endTimer
//  log(filteredPossibleRaces.size)

  log.startTimer("filter by lock may-alias")
  private val lockSet = new LockSet(pa)
  private val filterByLockMayAlias = new FilterByLockMayAlias(pa, lockSet)
  val races = filterByLockMayAlias(possibleRaces).asInstanceOf[ProgramRaceSet]
  log.endTimer
  log(races.size)
  
  log.startTimer("bubble up")
  private val bubbleUp = new BubbleUpToAppLevel(pa)
  val shallowRaces = bubbleUp(races).asInstanceOf[ProgramRaceSet]
  log.endTimer
  log(shallowRaces.size)
  
}

class AnalysisException(m: String) extends Throwable {

}