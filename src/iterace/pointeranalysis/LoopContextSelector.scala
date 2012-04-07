package iterace.pointeranalysis
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
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallerSiteContextPair
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import iterace.stage.threadSafe
import iterace.AnalysisException

class LoopContextSelector(options: Set[String], instankeKeyFactory: ZeroXInstanceKeys) extends ContextSelector {

  // Describes how contexts are chosen
  def getCalleeTarget(caller: CGNode, site: CallSiteReference, callee: IMethod, actualParameters: Array[InstanceKey]): Context = {
//    if(!isInterestingForUs(callee))
//      Everywhere.EVERYWHERE
    
    if(threadSafe(callee) || caller.getContext() == THREAD_SAFE)
      return THREAD_SAFE
    
    if(!instankeKeyFactory.isInteresting(callee.getDeclaringClass()))
      return Everywhere.EVERYWHERE
    
    val opsPattern = ".*Ops.*".r
    val parallelArrayPattern = ".*ParallelArray.*".r
    
    caller.getContext() match {
      // we've entered the loop but not the iteration. callee is the iteration
      case c: LoopCallSiteContext => {
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

        new LoopContext(caller, alphaIteration)

        // site.getProgramCounter() == 5 || site.getProgramCounter() == 2 || site.getProgramCounter() == 30);
      }
      // we're inside the loop
      case c: LoopContext => c 
      // we're outside the loop
      case _ => callee match {
          case M(C(_, "ParallelArray"), opsPattern()) => new LoopCallSiteContext(caller, site)
          case M(_, parallelArrayPattern()) => new CallerSiteContextPair(caller, site, caller.getContext())
          case _ => { caller.getContext(); }
        }
    }
  }
  def getRelevantParameters(caller: CGNode, site: CallSiteReference): IntSet = {
    EmptyIntSet.instance
  }
  def isInterestingForUs(callee: M) = 
    ContainerUtil.isContainer(callee.getDeclaringClass()) || callee.toString().contains("DateFormat");
}

object THREAD_SAFE extends Context {
  def get(key: ContextKey) = null
}

case object Loop extends ContextKey
case class Loop(n: N) extends ContextItem {
  def prettyPrint = toString
}
case object LoopIteration extends ContextKey
case class LoopIteration(alpha: Boolean) extends ContextItem
// this is the context for all the nodes in the loop iterations
class LoopContext(val l: CGNode,val alphaIteration: Boolean) extends Context {
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

object LoopContext {
  def unapply(c: LoopContext) = Some(c.l, c.alphaIteration)
}

// this is used for marking the entrance to the loop, so that it differentiated between loops at different locations
// e.g. n is the method that calls the apply
// site is the call site for the apply
// can be obtained from LoopContext(n, _); n.getContext()
case object LoopCallN extends ContextKey
case class LoopCallN(n: N) extends ContextItem
case object LoopCallSiteReference extends ContextKey
case class LoopCallSiteReference(site: CallSiteReference) extends ContextItem
class LoopCallSiteContext(val n: N,val site: CallSiteReference) extends Context with PrettyPrintable {
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