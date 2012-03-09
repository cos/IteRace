package iterace
import com.ibm.wala.ipa.callgraph.ContextSelector
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey
import com.ibm.wala.ipa.callgraph.Context
import com.ibm.wala.util.intset.IntSet
import com.ibm.wala.util.intset.EmptyIntSet
import com.ibm.wala.ipa.callgraph.ContextKey
import com.ibm.wala.ipa.callgraph.ContextItem
import iterace.WALAConversions._

object LoopContextSelector extends ContextSelector {
  case object LoopN extends ContextKey
  case class LoopN(n: N) extends ContextItem
  case object LoopIteration extends ContextKey
  case class LoopIteration(alpha: Boolean) extends ContextItem

  // this is the context for all the nodes in the loop iterations
  case class LoopContext(l: CGNode, alphaIteration: Boolean) extends Context {
    def get(key: ContextKey): ContextItem = {
      key match {
        case LoopN => LoopN(l)
        case LoopIteration => LoopIteration(alphaIteration)
        case _ => null
      }
    }
  }

  // this is used for marking the entrance to the loop, so that it differentiated between loops at different locations
  // e.g. n is the method that calls the apply
  // site is the call site for the apply
  // can be obtained from LoopContext(n, _); n.getContext()
  case object LoopCallN extends ContextKey
  case class LoopCallN(n: N) extends ContextItem
  case object LoopCallSiteReference extends ContextKey
  case class LoopCallSiteReference(site: CallSiteReference) extends ContextItem
  case class LoopCallSiteContext(n: N, site: CallSiteReference) extends Context with PrettyPrintable {
    def get(key: ContextKey): ContextItem = {
      key match {
        case LoopCallN => LoopCallN(n)
        case LoopCallSiteReference => LoopCallSiteReference(site)
        case _ => null
      }
    }
    
    def prettyPrint = {
      printCodeLocation(n.getMethod(), site.getProgramCounter())
    }
  }

  // Describes how contexts are chosen
  def getCalleeTarget(caller: CGNode, site: CallSiteReference, callee: IMethod, actualParameters: Array[InstanceKey]): Context = {
    val opsPattern = ".*Ops.*".r
    caller.getContext() match {
      case LoopCallSiteContext(_, _) => 
        LoopContext(caller, site.getProgramCounter() == 5 || site.getProgramCounter() == 2);
      case LoopContext(_, _) => caller.getContext()
      case _ => {
        callee match {
          case M(C(_, "ParallelArray"), opsPattern()) => LoopCallSiteContext(caller, site)
          case _ => caller.getContext()
        }
      }
    }
  }
  def getRelevantParameters(caller: CGNode, site: CallSiteReference): IntSet = {
    EmptyIntSet.instance
  }
}