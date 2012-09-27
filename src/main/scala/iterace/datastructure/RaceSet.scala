package iterace.datastructure

import wala.WALAConversions._
import scala.collection.immutable._
import scala.collection.JavaConverters._
import wala.S
import iterace.util._
import scala.collection.mutable.{ Builder, SetBuilder }
import scala.collection.generic.CanBuildFrom
import scala.util.Sorting
import scala.collection.immutable.TreeSet
import iterace.pointeranalysis.Loop
import iterace.pointeranalysis.LoopCallSiteContext

abstract sealed class RaceSet extends Set[Race] with PrettyPrintable {
  type This <: RaceSet

  override def seq = this
  def accepts(r: Race): Boolean
  override def +(r: Race): This
  override def -(r: Race): This

  /**
   * Aid for compact pretty printing
   */
  def printSameSet(p: (String, Set[_])) = p._1 + (if (p._2.size > 1) " [" + level + p._2.size + "x]" else "")
  def level: String
}

object RaceSet {
  def apply[T <: Race](r: T): LowLevelRaceSet = r match {
    case r: RaceOnField => new FieldRaceSet(r.l, r.o, r.f, Set(r.a), Set(r.b))
    case r: ShallowRace => new ShallowRaceSet(r.l, r.o, Set(r.a), Set(r.b))
  }
  //  def newBuilder: Builder[Race, RaceSet] = new SetBuilder[Race, RaceSet](empty)
  //  def raceSetCanBuildFrom = new CanBuildFrom[RaceSet, Race, RaceSet] {
  //    def apply(from: RaceSet) = newBuilder
  //    def apply() = newBuilder
  //  }
}

abstract sealed class LowLevelRaceSet(val l: Loop, val o: O, val alphaAccesses: Set[S[I]], val betaAccesses: Set[S[I]])
  extends RaceSet with PrettyPrintable {

  lazy val races = crossProduct(alphaAccesses, betaAccesses) map {
    case (s1: S[I], s2: S[I]) => getRace(s1, s2)
  }
  protected def getRace(s1: S[I], s2: S[I]): Race
  protected def getRaceSet(aAccesses: Set[S[I]], bAccesses: Set[S[I]]): This

  override def contains(race: Race) = alphaAccesses.contains(race.a) && betaAccesses.contains(race.b)
  override def iterator: Iterator[Race] = races.iterator
  override def +(race: Race) = getRaceSet(alphaAccesses + race.a, betaAccesses + race.b)
  override def -(race: Race) = getRaceSet(alphaAccesses - race.a, betaAccesses - race.b)
  override def empty: This = getRaceSet(Set.empty, Set.empty)
  override def foreach[U](f: (Race) => U): Unit = races.foreach(f)
  override def size = alphaAccesses.size * betaAccesses.size

  override def prettyPrint: String = {
    if (races.isEmpty)
      "empty raceset"

    def printAccesses(accs: Set[S[I]]) = (accs groupBy { _.prettyPrint } map (printSameSet)).toList.sorted.reduce(_ + "\n        " + _)

    val aString = printAccesses(alphaAccesses)
    val bString = printAccesses(betaAccesses)
    "   (a)  " + aString + "\n   (b)  " + bString
  }

  override def equals(other: Any) = other match {
    case that: LowLevelRaceSet => this.alphaAccesses == that.alphaAccesses && this.betaAccesses == that.betaAccesses
    case _ => false
  }
  
  override def level = ""
}

final class FieldRaceSet(l: Loop, o: O, val f: F, alphaAccesses: Set[S[I]], betaAccesses: Set[S[I]])
  extends LowLevelRaceSet(l, o, alphaAccesses, betaAccesses) with collection.SetLike[Race, FieldRaceSet] {

  type This = FieldRaceSet

  override def getRace(s1: S[I], s2: S[I]) = Race(l, o, f, s1, s2)
  override def getRaceSet(aAccesses: Set[S[I]], bAccesses: Set[S[I]]) = new FieldRaceSet(l, o, f, aAccesses, bAccesses)
  override def accepts(r: Race) = r match {
    case r: RaceOnField => r.l == l && r.o == o && r.f == f
    case _ => false
  }
  override def prettyPrint: String = " ." + f.getName() + "\n" + super.prettyPrint()

  override def equals(other: Any) = other match {
    case that: FieldRaceSet => this.l == that.l && this.o == that.o && this.f == that.f && super.equals(that)
    case _ => false
  }

  override def hashCode = l.hashCode() * 71 + o.hashCode() * 31 + f.hashCode()
}

final class ShallowRaceSet(l: Loop, o: O, alphaAccesses: Set[S[I]], betaAccesses: Set[S[I]])
  extends LowLevelRaceSet(l, o, alphaAccesses, betaAccesses) with collection.SetLike[Race, ShallowRaceSet] {

  type This = ShallowRaceSet

  def getRace(s1: S[I], s2: S[I]) = Race(l, o, s1, s2)
  override def getRaceSet(aAccesses: Set[S[I]], bAccesses: Set[S[I]]) = new ShallowRaceSet(l, o, aAccesses, bAccesses)
  override def accepts(r: Race) = r match {
    case r: ShallowRace => r.l == l && r.o == o
    case _ => false
  }

  override def prettyPrint: String = " application level\n" + super.prettyPrint()

  override def equals(other: Any) = other match {
    case that: ShallowRaceSet => this.l == that.l && this.o == that.o && super.equals(that)
    case _ => false
  }
  override def hashCode = l.hashCode() * 67 + o.hashCode()
}

abstract class CompositeRaceSet[Child <: RaceSet](val children: Set[Child])
  extends RaceSet {

  override def empty: This = getRaceSet(Set.empty)
  override def contains(race: Race) = children.exists({ _.contains(race) })
  override def iterator: Iterator[Race] = children.reduce[Set[Race]]({ _ ++ _ }) iterator
  override def +(race: Race): This = {
    if (!accepts(race)) throw new Exception("Race not accepted: " + race + " by " + this)
    val hasAcceptingChild = children exists { _.accepts(race) }
    val newChildren = if (hasAcceptingChild) {
      (children map (child => if (child.accepts(race)) child + race else child)).asInstanceOf[Set[Child]] // ugly but I'm tired of messing with types
    } else
      children + getChild(race)
    getRaceSet(newChildren)
  }
  def getChild(r: Race): Child
  override def -(race: Race): This = {
    if (!accepts(race)) throw new Exception("Race not accepted: " + race + " by " + this)
    val newChildren = (children map { _ - race } filter { _.size > 0 }).asInstanceOf[Set[Child]]
    getRaceSet(newChildren)
  }
  override def foreach[U](f: (Race) => U): Unit = children.foreach({ _.foreach(f) })
  override def size = children.toList map { _.size } reduceOption { _ + _ } getOrElse 0

  override def prettyPrint = children.groupBy(_.prettyPrint).map(printSameSet).
    toList.sorted.reduceOption(_ + "\n" + _).getOrElse("")

  def getRaceSet(children: Set[Child]): This

  override def equals(other: Any) = other match {
    case that: CompositeRaceSet[_] => this.children == that.children
    case _ => false
  }

  def getLowLevelRaceSets: Set[LowLevelRaceSet]
}

final class ObjectRaceSet(val l: Loop, val o: O, override val children: Set[LowLevelRaceSet])
  extends CompositeRaceSet(children) with collection.SetLike[Race, ObjectRaceSet] {
  type This = ObjectRaceSet

  override def getRaceSet(children: Set[LowLevelRaceSet]): This = new ObjectRaceSet(l, o, children)
  override def accepts(r: Race) = r.l == l && r.o == o
  override def getChild(r: Race) = RaceSet(r)

  override def prettyPrint = o.prettyPrint() + "\n" + super.prettyPrint

  override def equals(other: Any) = other match {
    case that: ObjectRaceSet => this.l == that.l && this.o == that.o && super.equals(that)
    case _ => false
  }

  override def hashCode = l.hashCode() * 97 + o.hashCode()
  def getLowLevelRaceSets: Set[LowLevelRaceSet] = children
  
  override def level = "Object - "
}

final class LoopRaceSet(val l: Loop, children: Set[ObjectRaceSet])
  extends CompositeRaceSet(children) with collection.SetLike[Race, LoopRaceSet] {
  type This = LoopRaceSet

  override def getRaceSet(children: Set[ObjectRaceSet]): This = new LoopRaceSet(l, children)
  override def accepts(r: Race) = r.l == l
  override def getChild(r: Race) = new ObjectRaceSet(l, r.o, Set.empty) + r

  override def prettyPrint =
    "Loop: " + l.n.getContext().asInstanceOf[LoopCallSiteContext].prettyPrint + "\n\n" + super.prettyPrint

  override def equals(other: Any) = other match {
    case that: LoopRaceSet => this.l == that.l && super.equals(that)
    case _ => false
  }
  override def hashCode = l.hashCode()
  def getLowLevelRaceSets: Set[LowLevelRaceSet] = children flatMap { _.getLowLevelRaceSets }
  override def level = "Loop - "
}

final class ProgramRaceSet(children: Set[LoopRaceSet])
  extends CompositeRaceSet(children) with collection.SetLike[Race, ProgramRaceSet] {
  type This = ProgramRaceSet

  override def getRaceSet(children: Set[LoopRaceSet]): This = new ProgramRaceSet(children)
  override def accepts(r: Race) = true
  override def getChild(r: Race) = new LoopRaceSet(r.l, Set.empty) + r

  def getLowLevelRaceSets: Set[LowLevelRaceSet] = children flatMap { _.getLowLevelRaceSets }
  
  override def level = "Program - "
}

object ProgramRaceSet {
  def apply(races: Set[Race]): ProgramRaceSet = {
    var prs = new ProgramRaceSet(Set.empty)
    for (r <- races) {
      prs = prs + r
    }
    prs
  }

  def fromRaceSets(racesets: Set[LowLevelRaceSet]): ProgramRaceSet = {
    new ProgramRaceSet(racesets groupBy { _.l } map {
      case (l, racesets) =>
        new LoopRaceSet(l, racesets groupBy { _.o } map {
          case (o, racesets) =>
            new ObjectRaceSet(l, o, racesets)
        } toSet)
    } toSet)
  }
}