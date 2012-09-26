package iterace.util
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import WALAConversions._
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.ibm.wala.classLoader.ShrikeBTMethod
import iterace.pointeranalysis.RacePointerAnalysis
import iterace.datastructure.Lock

object S {
  def unapply(b: BasicBlockInContext[IExplodedBasicBlock]): Option[(N, I)] = {
    Some(b.getNode(), b.getDelegate().getInstruction())
  }
  def apply(n: N, i: I) = new S(n, i)
}

class S[+J <: I](val n: N, val i: J) extends PrettyPrintable {

  // mutable but not part of the object's identity
  var lockset: Option[Set[Lock]] = None

  def prettyPrint(): String =
    printCodeLocation() +
      (lockset match {
        case Some(lockset) => lockset.map("         "+_.prettyPrint).reduceOption(_+"\n"+_).map("\n"+_).getOrElse("")
        case None => ""
      }) +
      (if (debug.detailContexts) " --- " + n else "")

  def printCodeLocation(): String = {
    if (irNo >= 0) {
      n.getMethod() match {
        case m: ShrikeBTMethod => {
          val bytecodeIndex = m.getBytecodeIndex(irNo)
          WALAConversions.printCodeLocation(m, bytecodeIndex)
        }
        case _ => m.toString()
      }
    } else {
      val index = n.instructions collect { case i if i != null => i.toString } findIndexOf { _ == i.toString }
      "IRNo-1 " + index + " ---- " + i
    }
  }

  lazy val m = n.m

  lazy val lineNo = m.getLineNumber(irNo)

  lazy val irNo = n.getIR().getInstructions().findIndexOf(ii => i.equals(ii))

  def valuesForVariableName(name: String): Iterable[V] = {
    val maxValue = n.getIR().getSymbolTable().getMaxValueNumber();
    Stream.range(1, maxValue + 1).filter(v => {
      val names = n.getIR().getLocalNames(irNo, v)
      if (names != null) {
        names.contains(name)
      } else
        false
    })
  }

  lazy val isStatic: Boolean = i match {
    case i: AccessI => i.isStatic
    case i: InvokeI => i.isStatic
    case _ => false
  }

  def variableNames(v: Int): Iterable[String] = {
    if (irNo == -1) return Iterable()
    val names = n.getIR().getLocalNames(irNo, v)
    if (names != null) names.filter(_ != null) else Iterable()
  }

  override def toString = "S(" + n + "," + i + ")"

  override def equals(other: Any) = other match {
    case that: S[_] => this.n == that.n && this.i == that.i
    case _ => false
  }

  override def hashCode = n.hashCode * 41 + i.hashCode
}