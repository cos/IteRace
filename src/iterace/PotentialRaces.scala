package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection._
import scala.collection.JavaConverters._
import iterace.util.WALAConversions._
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import util._

class PotentialRaces(pa: RacePointerAnalysis) extends Function0[ProgramRaceSet] {

  override def apply(): ProgramRaceSet = {
    ProgramRaceSet(races)
  }

  import pa._

  private val icfg = ExplodedInterproceduralCFG.make(callGraph)

  private val races: immutable.Set[Race] = (parLoops map (l => {

    val alphaWrites = statementsReachableFrom(l.alphaIterationN, {_.getContext() != THREAD_SAFE}) filter
      (s => s.i.isInstanceOf[PutI] || s.i.isInstanceOf[ArrayStoreI])

    val betaAccesses = statementsReachableFrom(l.betaIterationN, {_.getContext() != THREAD_SAFE}) filter
      (s => s.i.isInstanceOf[AccessI] || s.i.isInstanceOf[ArrayReferenceI])
      
    val pairsOnSameField = crossProduct(alphaWrites groupBy {_.i.f.get}, betaAccesses groupBy {_.i.f.get}) .
    				filter {case ((f1,_),(f2,_)) => f1 == f2} .
    				map {case ((f, accesses1),(_, accesses2)) => crossProduct(accesses1, accesses2)} flatten
      
    pairsOnSameField.collect {
      case (s1: S[I], s2: S[I]) =>
        val sharedObjects = s1.i match {
          case i: AccessI if i.isStatic => Set(new StaticClassObject(s1.i.f.get.getDeclaringClass()))
          case _ => s1.refP.get.pt & s2.refP.get.pt
        }

        // it is enough to consider object created outside and in the the first iteration
        // so, filter out the objects created in the second iteration. they are duplicates of the first iteration      	
        val relevantObjects = sharedObjects filter {
          case O(n, i) => !inLoop(n) || firstIteration(n);
          case _ => true
        }

        relevantObjects map { Race(l, _, s1.i.f.get, s1, s2) }
    } flatten
  })) flatten
}