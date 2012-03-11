package iterace
import conversions._
import scala.collection.mutable.HashSet

case class R(l: Loop, o: O, f: F, a: S[I], b: S[I]) extends PrettyPrintable {
  def prettyPrint() = {
    o.prettyPrint() + "   " + f.getName() + "\n" +
      " (a)  " + a.prettyPrint() + "\n" +
      " (b)  " + b.prettyPrint() + "\n"
  }
}
class RSet extends HashSet[R] with PrettyPrintable {
  def prettyPrint(): String = {
    def printSameSet(p: (String, HashSet[R])) = p._1 + (if(p._2.size > 1) " [" + p._2.size + "]" else "")
    
    val aAccesses = this.groupBy(r => r.a.prettyPrint()).map(printSameSet).reduce(_ + "\n        " + _)
    val bAccesses = this.groupBy(r => r.b.prettyPrint()).map(printSameSet).reduce(_ + "\n        " + _)
    "   (a)  " + aAccesses + "\n   (b)  " + bAccesses
  }
}
object RSet {
  def apply() = {
    new RSet()
  }
} 