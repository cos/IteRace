package iterace.util
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import WALAConversions._
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.ibm.wala.classLoader.ShrikeBTMethod

object S {
  def unapply(b: BasicBlockInContext[IExplodedBasicBlock]): Option[(N, I)] = {
    Some(b.getNode(), b.getDelegate().getInstruction())
  }
  def apply[T <: I](n: N, i: T) = new S(n, i)
}

class S[J <: I](val n: N, val i: J) extends PrettyPrintable {
  
  def prettyPrint() = {
    printCodeLocation()
  }
  def printCodeLocation(): String = {
    if (irNo >= 0) {
      val m = n.getMethod().asInstanceOf[ShrikeBTMethod]
      val bytecodeIndex = m.getBytecodeIndex(irNo)
      WALAConversions.printCodeLocation(m, bytecodeIndex)
    } else {
      val index = n.instructions collect { case i if i != null => i.toString } findIndexOf { _ == i.toString }
      "IRNo-1 " + index + " ---- " + i
    }
  }

  def m = n.m
  
  def lineNo = m.getLineNumber(irNo)
    
  def irNo = n.getIR().getInstructions().findIndexOf(ii => i.equals(ii))

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

  def variableNames(v: Int): Iterable[String] = {
    if (irNo == -1) return Iterable()
    val names = n.getIR().getLocalNames(irNo, v)
    if (names != null) names.filter(_ != null) else Iterable()
  }

  override def toString = "S(" + n + "," + i + ")"
}