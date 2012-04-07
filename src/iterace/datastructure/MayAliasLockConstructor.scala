package iterace.datastructure

import iterace.pointeranalysis.PointerAnalysis
import iterace.util.WALAConversions._

class MayAliasLockConstructor(pa: PointerAnalysis) extends LockConstructor {
  import pa._

  def apply(p: P) = if (p.pt.size == 1) Some(OLock(p.pt.head)) else None
  def apply(c: C) = Some(CLock(c))
}

case class OLock(o: O) extends Lock {
  def prettyPrint = "L: " + o.prettyPrint
}
case class CLock(c: C) extends Lock {
  def prettyPrint = "L: " + c.prettyPrint
}