package iterace.stage
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection._
import scala.collection.JavaConverters._
import wala.WALAConversions._
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import iterace.pointeranalysis.RacePointerAnalysis
import iterace.datastructure.ProgramRaceSet
import iterace.datastructure.Race
import iterace.util._
import iterace.datastructure.threadSafe
import iterace.datastructure.ObjectRaceSet
import iterace.datastructure.LoopRaceSet
import iterace.datastructure.FieldRaceSet
import iterace.IteRaceOption
import wala.S
import wala.extra.StaticClassObject
import wala.O

class PotentialRaces(pa: RacePointerAnalysis) extends Function0[ProgramRaceSet] {

  override def apply(): ProgramRaceSet = {
    races
  }

  import pa._

  private val icfg = ExplodedInterproceduralCFG.make(callGraph)

  def toObjectMap(accesses: Iterable[S[I]]) =
    accesses flatMap (s => s.i match {
      case i: AccessI if i.isStatic => s.i.f match {
        case Some(f) => Seq((new StaticClassObject(f.getDeclaringClass()), s))
        case _ => Seq()
      }
      case _ => s.refP.get.pt map { (_, s) } toSeq
    }) groupBy { _._1 } mapValues { _ map { _._2 } toSet }

  private val races = new ProgramRaceSet(parLoops map (l => {
    val alphaWrites = statementsReachableFrom(l.alphaIterationN).
      filter(s => s.i.isInstanceOf[PutI] || s.i.isInstanceOf[ArrayStoreI]).
      filter(s => !iteraceOptions.contains(IteRaceOption.KnownSafeFiltering) || !threadSafe(s))

    val betaAccesses = statementsReachableFrom(l.betaIterationN).
      filter(s => s.i.isInstanceOf[AccessI] || s.i.isInstanceOf[ArrayReferenceI]).
      filter(s => !iteraceOptions.contains(IteRaceOption.KnownSafeFiltering) || !threadSafe(s))

    // it is enough to consider object created outside and in the the first iteration
    // so, filter out the objects created in the second iteration. they are duplicates of the first iteration 
    // isRelevant is about the above
    val aObjectMap = toObjectMap(alphaWrites) filter { case (o, _) => isRelevant(o) }
    val bObjectMap = toObjectMap(betaAccesses) filter { case (o, _) => isRelevant(o) }

    val aMap = aObjectMap mapValues { _ groupBy { _.i.f.get } }
    val bMap = bObjectMap mapValues { _ groupBy { _.i.f.get } }

    val pairsByObjects = aMap collect { case (o, aSet) if bMap.contains(o) => (o, (aSet, bMap(o))) }

    val greatMapping = pairsByObjects map {
      case (o, (aSet, bSet)) =>
        (o, aSet collect { case (f, aSet1) if bSet.contains(f) => (f, (aSet1, bSet(f))) })
    }

    new LoopRaceSet(l, greatMapping map {
      case (o, mapByField) =>
        new ObjectRaceSet(l, o, mapByField map {
          case (f, (aSet, bSet)) =>
            new FieldRaceSet(l, o, f, aSet, bSet)
        } filter { _.size > 0 } toSet)
    } filter { _.size > 0 } toSet)
  }) filter { _.size > 0 })

  //    val pairsOnSameField = crossProduct(alphaWrites groupBy { _.i.f.get }, betaAccesses groupBy { _.i.f.get }).
  //      filter { case ((f1, _), (f2, _)) => f1 == f2 }.
  //      map { case ((f, accesses1), (_, accesses2)) => crossProduct(accesses1, accesses2) } flatten
  //
  //    pairsOnSameField.collect {
  //      case (s1: S[I], s2: S[I]) =>
  //        val sharedObjects = s1.i match {
  //          case i: AccessI if i.isStatic => Set(new StaticClassObject(s1.i.f.get.getDeclaringClass()))
  //          case _ => s1.refP.get.pt & s2.refP.get.pt
  //        }
  //
  //        val relevantObjects = filterOutSecondIteration(sharedObjects)
  //
  //        relevantObjects map { Race(l, _, s1.i.f.get, s1, s2) }
  //    } flatten
  //  })) flatten

  // duplicated functionality in bubbleUp - check that if deciding to modify something here, loop for inLoop and
  // firstIteration
  def filterOutSecondIteration[T <: O](objects: Set[T]) = objects filter isRelevant

  def isRelevant(o: O): Boolean = o match {
    case O(n, i) => !inLoop(n) || firstIteration(n);
    case _ => true
  }
}