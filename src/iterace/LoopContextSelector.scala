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
import iterace.util.WALAConversions._
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ipa.callgraph.propagation.ContainerUtil
import com.ibm.wala.ipa.callgraph.impl.Everywhere

case object Loop extends ContextKey
case class Loop(n: N) extends ContextItem {
  def prettyPrint = toString
}
case object LoopIteration extends ContextKey
case class LoopIteration(alpha: Boolean) extends ContextItem
// this is the context for all the nodes in the loop iterations
case class LoopContext(l: CGNode, alphaIteration: Boolean) extends Context {
  def loop = Loop(l)
  def iteration = LoopIteration(alphaIteration)
  
  def get(key: ContextKey): ContextItem = {
    key match {
      case Loop => Loop(l)
      case LoopIteration => LoopIteration(alphaIteration)
      case _ => null
    }
  }
}

object LoopContextSelector extends ContextSelector {
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
//    if(!isInterestingForUs(callee))
//      Everywhere.EVERYWHERE
    val opsPattern = ".*Ops.*".r
    caller.getContext() match {
      case LoopCallSiteContext(_, _) => {
        val invocations = caller.getIR().getCalls(site);
        if (invocations.length != 1)
          throw new AnalysisException("There should be only invocation for an mock operation call site");

        val e0PutVals = caller.getIR().getInstructions().
          collect { case x: SSAPutInstruction if x.getDeclaredField.toString.contains("e0") => x.getVal() }

        val e0GetVals = caller.getIR().getInstructions().
          collect { case x: SSAGetInstruction if x.getDeclaredField.toString.contains("e0") => x.getDef() }

        val e0Vals = (e0PutVals ++ e0GetVals).toSet

        val invoke = invocations.head
        val defAndUses = (invoke.uses ++ Iterable(invoke.getDef())).toSet

        val alphaIteration = !(defAndUses & e0Vals).isEmpty

        LoopContext(caller, alphaIteration)

        // site.getProgramCounter() == 5 || site.getProgramCounter() == 2 || site.getProgramCounter() == 30);
      }
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
  def isInterestingForUs(callee: M) = 
    ContainerUtil.isContainer(callee.getDeclaringClass()) || callee.toString().contains("DateFormat");
}