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

class PossibleRaces(pa: RacePointerAnalysis) extends Function0[immutable.Set[Race]] {

  override def apply(): immutable.Set[Race] = races

  import pa._

  private val icfg = ExplodedInterproceduralCFG.make(callGraph)

  Thread.sleep(10);

  private val races: immutable.Set[Race] = (parLoops map (l => {

    val alphaWrites = statementsReachableFrom(l.alphaIterationN) collect
      { case s: S[PutI] if s.i.isInstanceOf[PutI] => s }

    val betaAccesses = statementsReachableFrom(l.betaIterationN) collect
      { case s: S[AccessI] if s.i.isInstanceOf[AccessI] => s }

    val allPairs = crossProduct(alphaWrites, betaAccesses)

    val pairsOnSameField = allPairs filter { case (s1, s2) => s1.i.getDeclaredField() == s2.i.getDeclaredField() }

    pairsOnSameField.collect {
      case (s1, s2) =>
        val sharedObjects = if (s1 isStatic)
          Set(new StaticClassObject(s1.i.getDeclaredField().getDeclaringClass()))
        else
          s1.refP.get.pt & s2.refP.get.pt
        // it is enough to consider object created outside and in the the first iteration
        // so, filter out the objects created in the second iteration. they are duplicates of the first iteration      	
        val relevantObjects = sharedObjects filter {
          case O(n, i) => !inLoop(n) || firstIteration(n);
          case _ => true
        }

        val f = s1.i.getDeclaredField();

        relevantObjects map { Race(l, _, f, s1, s2) }
    } flatten
  })) flatten
}

case class Race(l: Loop, o: O, f: F, a: S[PutI], b: S[AccessI]) extends PrettyPrintable {
  def prettyPrint() = {
    o.prettyPrint() + "   " + f.getName() + "\n" +
      " (a)  " + a.prettyPrint() + "\n" +
      " (b)  " + b.prettyPrint() + "\n"
  }
}

abstract class RaceSet(races: Set[Race]) extends java.lang.Iterable[Race] {
  override def iterator = races.iterator.asJava
}
abstract trait MetaRaceSet {
  def children(): Array[_ <: RaceSet]
}

case class RacingAccess(s: S[AccessI], race: Race, alpha: Boolean)

case class FieldRaceSet(val f: F, races: Set[Race]) extends RaceSet(races) {
  def alphaAccesses() = races.map(r => new RacingAccess(r.a, r, true))
  def betaAccesses() = races.map(r => new RacingAccess(r.b, r, false))

  def prettyPrint: String = {
    def printSameSet(p: (String, Set[Race])) = p._1 + (if (p._2.size > 1) " [" + p._2.size + "]" else "")

    val aAccesses = races.groupBy(r => r.a.prettyPrint()).toStringSorted.map(printSameSet).toStringSorted.reduce(_ + "\n        " + _)
    val bAccesses = races.groupBy(r => r.b.prettyPrint()).toStringSorted.map(printSameSet).toStringSorted.reduce(_ + "\n        " + _)

    " ." + f.getName() + "\n" +
      "   (a)  " + aAccesses + "\n   (b)  " + bAccesses
  }

  def children() = (alphaAccesses & betaAccesses) toArray
}
case class ObjectRaceSet(val o: O, races: Set[Race]) extends RaceSet(races) with MetaRaceSet {
  override def children() = { val groups = races.groupBy { _.f }; groups.keys map (f => new FieldRaceSet(f, groups(f))) } toArray
}
case class LoopRaceSet(val l: Loop, races: Set[Race]) extends RaceSet(races) with MetaRaceSet {
  override def children() = { val groups = races.groupBy { _.o }; groups.keys map (o => new ObjectRaceSet(o, groups(o))) } toArray
}
case class ProgramRaceSet(races: Set[Race]) extends RaceSet(races) with MetaRaceSet {
  override def children() = { val groups = races.groupBy { _.l }; groups.keys map (l => new LoopRaceSet(l, groups(l))) } toArray
}