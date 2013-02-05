package iterace.datastructure

import edu.illinois.wala.ipa.callgraph.propagation.O
import iterace.pointeranalysis.Iteration
import iterace.pointeranalysis.RacePointerAnalysis
import edu.illinois.wala.Facade._

class MayAliasLockConstructor(pa: RacePointerAnalysis) extends LockConstructor {
  import pa._

  def apply(p: P) = if (p.pt.size == 1) Some(OLock(p.pt.head)) else None
  def apply(c: C) = Some(CLock(c))
}

case class OLock(o: O) extends Lock {
  def prettyPrint = "L: " + o.prettyPrint + 
  		(o match {
  		  case O(n,_) => "-"+ (Option(n.c(Iteration)) getOrElse "outside")
  		  case _ => ""
  		})
}
case class CLock(c: C) extends Lock {
  def prettyPrint = "L: " + c.prettyPrint
}