package iterace.datastructure

import scala.collection._
import edu.illinois.wala.Facade._
import iterace.util._
import iterace.pointeranalysis.Loop
import edu.illinois.wala.S
import edu.illinois.wala.PrettyPrintable

object Race {
  def apply(l: Loop, o: O, f:F, a: S[I], b: S[I]) = new RaceOnField(l,o,f,a,b)
  def apply(l: Loop, o: O, a: S[I], b: S[I]) = new ShallowRace(l,o,a,b) 
}

abstract sealed class Race(val l: Loop, val o: O, val a: S[I], val b: S[I]) extends PrettyPrintable {

  override def hashCode = l.hashCode * 41 + o.hashCode
  
  override def equals(other: Any) = other match {
    case that: Race =>
      (that isComparable this) &&
        this.l == that.l && this.o == that.o && this.a == that.a && this.b == that.b
    case _ => false
  }
  
  def isComparable(other: Any) = other.isInstanceOf[Race]

  def prettyPrint() = {
    " (a)  " + a.prettyPrint() + "\n" + " (b)  " + b.prettyPrint() + "\n"
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
    o.prettyPrint + "   " + f.getName() + "\n" + super.prettyPrint()
}

class ShallowRace(l: Loop, o: O, a: S[I], b: S[I]) extends Race(l, o, a, b) {
  override def prettyPrint() =
    o.prettyPrint + "   \n" + super.prettyPrint()
}