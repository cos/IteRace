package iterace.util
import com.ibm.wala.ssa.SSAPutInstruction
import scala.collection.JavaConversions._

// "Pointer Local" - equivalent of LocalPointerKey
trait WALAConversionsForP { self: WALAConversions =>
  object P {
    def apply(n: N, v: Int) = {
      new P(n, v)
    }
    def unapply(p: P): Option[(N, Int)] = {
      Some((p.getNode(), p.getValueNumber()))
    }
  }

  implicit def enhanceP(p: P) = new {
    def n = p.getNode()
    def v = p.getValueNumber()
    def getDef() = n.getDU().getDef(v)
    def getUses(): Iterable[I] = n.getDU().getUses(v).toIterable

    /**
     * Gets all uses of this pointer that write to a field of it
     */
    def getPuts(): Iterable[SSAPutInstruction] =
      (for (i <- getUses() if i.isInstanceOf[PutI] && i.asInstanceOf[PutI].getRef() == v)
        yield i.asInstanceOf[SSAPutInstruction])

    def variableNames(): Iterable[String] =
      n.instructions.toIterable.map(i => S(n, i).variableNames(v)).flatten.toSet

    def prettyPrint(): String =
      n.prettyPrint + " v" + v + "(" + (if (!variableNames().isEmpty) variableNames.reduce(_ + "," + _) else "") + ")"
  }
}