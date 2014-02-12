package iterace.stage
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection._
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import edu.illinois.wala.Facade._
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
import iterace.datastructure.RegionRaceSet
import iterace.datastructure.FieldRaceSet
import iterace.IteRaceOption
import edu.illinois.wala.S
import edu.illinois.wala.ipa.callgraph.propagation.O
import edu.illinois.wala.ipa.callgraph.propagation.StaticClassObject
import iterace.datastructure.RegionRaceSet
import iterace.pointeranalysis.MayRunInParallel
import iterace.pointeranalysis.MayRunInParallel
import com.ibm.wala.classLoader.IClassLoader
import com.ibm.wala.util.graph.GraphUtil

class PotentialRaces(pa: RacePointerAnalysis) extends Function0[ProgramRaceSet] {

  override def apply(): ProgramRaceSet = {
    races
  }

  import pa._

  val callGraph = pa.getCallGraph()

  private val icfg = ExplodedInterproceduralCFG.make(callGraph)

  def toObjectMap(accesses: Iterable[S[I]]) =
    accesses flatMap (s => s.i match {
      case i: AccessI if i.isStatic => s.i.f match {
        case Some(f) => Seq((new StaticClassObject(f.getDeclaringClass()), s))
        case _ => Seq()
      }
      case _ => s.refP.get.pt map { (_, s) } toSeq
    }) groupBy { _._1 } mapValues { _ map { _._2 } toSet }

  private val notThreadSafeOrFiltered = (s: S[I]) => !iteraceOptions.contains(IteRaceOption.Filtering) || !threadSafe(s)
  private val isWriteLike = (s: S[I]) => s.i.isInstanceOf[PutI] || s.i.isInstanceOf[ArrayStoreI]
  private val isAccessLike = (s: S[I]) => s.i.isInstanceOf[AccessI] || s.i.isInstanceOf[ArrayReferenceI]

  implicit class BooleanFunction[T](f1: T => Boolean) {
    def ||(f2: T => Boolean)(x: T): Boolean = f1(x) || f2(x)
    def &&(f2: T => Boolean)(x: T): Boolean = f1(x) && f2(x)
  }

  def makeRegionRaceSet(l: MayRunInParallel, s1: Iterable[S[I]], s2: Iterable[S[I]]) = {
    val alphaWrites = s1.filter(isWriteLike && notThreadSafeOrFiltered)

    val betaAccesses = s2.filter(isAccessLike && notThreadSafeOrFiltered)

    // it is enough to consider object created outside and in the the first iteration
    // so, filter out the objects created in the second iteration. they are duplicates of the first iteration 
    // isRelevant is about the above
    val aObjectMap = toObjectMap(alphaWrites) filter { case (o, _) => isRelevant(o) }
    val bObjectMap = toObjectMap(betaAccesses) filter { case (o, _) => isRelevant(o) }

    val aMap = aObjectMap mapValues { _ groupBy { _.i.f.getOrElse(null) } }
    val bMap = bObjectMap mapValues { _ groupBy { _.i.f.getOrElse(null) } }

    val pairsByObjects = aMap collect { case (o, aSet) if bMap.contains(o) => (o, (aSet, bMap(o))) }

    val greatMapping = pairsByObjects map {
      case (o, (aSet, bSet)) =>
        (o, aSet collect { case (f, aSet1) if bSet.contains(f) => (f, (aSet1, bSet(f))) })
    }

    new RegionRaceSet(l, greatMapping map {
      case (o, mapByField) =>
        new ObjectRaceSet(l, o, mapByField map {
          case (f, (aSet, bSet)) =>
            new FieldRaceSet(l, o, f, aSet, bSet)
        } filter { _.size > 0 } toSet)
    } filter { _.size > 0 } toSet)
  }

  private val racesInLoops = parLoops map { l =>
    makeRegionRaceSet(l,
      statementsReachableFrom(l.alphaIterationN),
      statementsReachableFrom(l.betaIterationN))
  } filter { _.size > 0 }

  private val reachableAsyncTasks = callGraph filter { n =>
    (n.toString contains "doInBackground") &&
      !(n.instructions exists {
        case i: InvokeI => i.getDeclaredTarget().toString contains "doInBackground"
        case _ => false
      })
  }

  private val instructionsOutsideAsyncs =
    DFS.getReachableNodes(callGraph, callGraph.getEntrypointNodes(), { n: N =>
      !Seq("doInBackground", "onPostExecute")
        .exists(n.toString contains)
    }) flatMap { n => n.instructions map (S(n, _)) }

  case class AsyncTask(n: N) extends MayRunInParallel {
    def prettyPrintDetail = "async task: " + n
  }

  private val racesInAsyncs: Set[RegionRaceSet] = reachableAsyncTasks map { t =>
    val writeOnArgumentOfDoInBackground = t.instructions filter {
      case i: ArrayReferenceI => i.getUse(0) == 2
      case _ => false
    } map { S(t, _) } toList

//    println("!!! " + writeOnArgumentOfDoInBackground)
//    println(t.instructions.toList mkString "\n")

    val inTask = statementsReachableFrom(t) filterNot (writeOnArgumentOfDoInBackground contains _)

//    println(instructionsOutsideAsyncs mkString "\n")
//    println("---" * 10)
//    println(inTask mkString "\n")

    makeRegionRaceSet(AsyncTask(t), instructionsOutsideAsyncs, inTask) ++
      makeRegionRaceSet(AsyncTask(t), inTask, instructionsOutsideAsyncs filterNot isWriteLike)
  } toSet

  private val races = new ProgramRaceSet(racesInLoops ++ racesInAsyncs)

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