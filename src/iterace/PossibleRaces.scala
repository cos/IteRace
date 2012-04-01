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

    val alphaWrites = statementsReachableFrom(l.alphaIterationN) filter
      (s => s.i.isInstanceOf[PutI] || s.i.isInstanceOf[ArrayStoreI])

    val betaAccesses = statementsReachableFrom(l.betaIterationN) filter
      (s => s.i.isInstanceOf[AccessI] || s.i.isInstanceOf[ArrayReferenceI])

    val allPairs = crossProduct(alphaWrites, betaAccesses)

    val pairsOnSameField = allPairs filter { case (s1, s2) => s1.i.f == s2.i.f }

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

        relevantObjects map { new RaceOnField(l, _, s1.i.f.get, s1, s2) }
    } flatten
  })) flatten
}

abstract class Race(val l: Loop, val o: O, val a: S[I], val b: S[I]) extends PrettyPrintable {

  override def hashCode = l.hashCode * 41 + o.hashCode
  override def equals(other: Any) = other match {
    case that: Race =>
      (that isComparable this) &&
        this.l == that.l && this.o == that.o && this.a == that.a && this.b == that.b
    case _ => false
  }
  def isComparable(other: Any) = other.isInstanceOf[Race]

  def prettyPrint() = {
    " (a)  " + a.prettyPrint() + "\n" +
      " (b)  " + b.prettyPrint() + "\n"
  }
}

class RaceOnField(l: Loop, o: O, val f: F, a: S[I], b: S[I]) extends Race(l, o, a, b) with PrettyPrintable {
  override def hashCode = super.hashCode * 71 + f.hashCode
  override def equals(other: Any) = other match {
    case that: RaceOnField => 
      (that isComparable this) && 
      super.equals(that) && this.f == that.f
    case _ => false
  }
  override def isComparable(other: Any) = other.isInstanceOf[RaceOnField]
  
  override def prettyPrint() =
    o.prettyPrint() + "   " + f.getName() + "\n" + super.prettyPrint()
}

class ShallowRace(l: Loop, o: O, a: S[I], b: S[I]) extends Race(l, o, a, b) {
  override def prettyPrint() =
    o.prettyPrint() + "   \n" + super.prettyPrint()
}

abstract class RaceSet[T <: Race](races: Set[T]) extends java.lang.Iterable[T] with PrettyPrintable {
  override def iterator = races.iterator.asJava
  
  def alphaAccesses() = races.map(r => new RacingAccess(r.a, r, true))
  def betaAccesses() = races.map(r => new RacingAccess(r.b, r, false))
  
  override def prettyPrint: String = {
    if(races.isEmpty)
      "empty raceset"
    
    def printSameSet(p: (String, Set[T])) = p._1 + (if (p._2.size > 1) " [" + p._2.size + "]" else "")

    val aAccesses = races.groupBy(r => r.a.prettyPrint()).toStringSorted.map(printSameSet).toStringSorted.reduce(_ + "\n        " + _)
    val bAccesses = races.groupBy(r => r.b.prettyPrint()).toStringSorted.map(printSameSet).toStringSorted.reduce(_ + "\n        " + _)
      "   (a)  " + aAccesses + "\n   (b)  " + bAccesses
  }
}
abstract trait MetaRaceSet {
  def children(): Array[_ <: RaceSet[_]]
}

case class RacingAccess(s: S[I], race: Race, alpha: Boolean)

class ShallowRaceSet(races: Set[ShallowRace]) extends RaceSet(races)

class FieldRaceSet(val f: F, races: Set[RaceOnField]) extends RaceSet(races) {
	override def prettyPrint: String = " ." + f.getName() + "\n" + super.prettyPrint()
  def children() = (alphaAccesses & betaAccesses) toArray
}
case class ObjectRaceSet(val o: O, races: Set[Race]) extends RaceSet(races) with MetaRaceSet {
  override def children() = { 
    val groups = races collect { case r: RaceOnField => r } groupBy { _.f } 
    groups.keys map (f => new FieldRaceSet(f, groups(f))) } toArray
}
case class LoopRaceSet(val l: Loop, races: Set[Race]) extends RaceSet(races) with MetaRaceSet {
  override def children() = { val groups = races.groupBy { _.o }; groups.keys map (o => new ObjectRaceSet(o, groups(o))) } toArray
}
case class ProgramRaceSet(races: Set[Race]) extends RaceSet(races) with MetaRaceSet {
  override def children() = { val groups = races.groupBy { _.l }; groups.keys map (l => new LoopRaceSet(l, groups(l))) } toArray
}