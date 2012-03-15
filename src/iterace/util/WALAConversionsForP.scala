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
  
    implicit def p2nv(p: P) = new {
    def n = p.getNode()
    def v = p.getValueNumber()
  }
  implicit def p2def(p: P) = new {
    def getDef() = p.n.getDU().getDef(p.v)
  }
  implicit def pWithUses(p: P) = new {
    def getUses(): Iterable[I] = {
      p.n.getDU().getUses(p.v).toIterable
    }
    /**
     * Gets all uses of this pointer that write to a field of it
     */
    def getPuts(): Iterable[SSAPutInstruction] = {
      (for (i <- getUses() if i.isInstanceOf[SSAPutInstruction] && i.asInstanceOf[SSAPutInstruction].getRef() == p.v)
        yield i.asInstanceOf[SSAPutInstruction])
    }
  }
  implicit def pToVariableNames(p: P) = new {
    def names(): Iterable[String] = {
      (p.n.instructions.toIterable).map(i => S(p.n, i).variableNames(p.v)).flatten.toSet
    }
  }
  implicit def p2prettyprintable(p: P): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = {
      p.n.prettyPrint + " v" + p.v + "(" + (if (!p.names().isEmpty) p.names.reduce(_ + "," + _) else "") + ")"
    }
  }
}