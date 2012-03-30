package iterace.util
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import WALAConversions._

object O {
  def unapply(o: O): Option[(N, I)] = {
    o match {
      case o: AllocationSiteInNode => Some(o.getNode(), o.getNode().getIR().getNew(o.getSite()))
      case _ => None
    }
  }
  def prettyPrint(o: O) = o match {
    case o: AllocationSiteInNode => printCodeLocation(o.getNode(), o.getSite().getProgramCounter())
    case o: StaticClassObject => "Static: "+o.getKlass().prettyPrint()
    case _ => o.toString()
  }
}