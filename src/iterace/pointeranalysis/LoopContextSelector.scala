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
import iterace.AnalysisException
import scala.collection._
import com.ibm.wala.ipa.callgraph.DelegatingContext
import iterace.datastructure.threadSafeOnClosure
import iterace.datastructure.generatesSafeObjects
import iterace.util.WALAConversions._
import iterace.datastructure.movesObjectsAround

class LoopContextSelector(options: Set[String], instankeKeyFactory: ZeroXInstanceKeys) extends ContextSelector {
  // this is the context for all the nodes in the loop iterations
  private class LoopContext(val l: CGNode, val parallel: Boolean, val alphaIteration: Boolean) extends Context {
    val loop = Loop(l, parallel)
    val iteration = if (alphaIteration) AlphaIteration else BetaIteration

    def get(key: ContextKey): ContextItem = key match {
      case Loop => loop
      case Iteration => iteration
      case _ => null
    }
    override def toString = loop.prettyPrint + "," + (if (alphaIteration) "alpha" else "beta")
  }

  private object ThreadSafeContext extends Context {
    override def get(key: ContextKey) = key match {
      case ThreadSafeOnClosure => ThreadSafeOnClosure
      case _ => null
    }
    override def toString = "ThreadSafe"
  }

  private object UninterestingContext extends Context {
    override def get(key: ContextKey) = key match {
      case Uninteresting => Uninteresting
      case _ => null
    }
    override def toString = "Uninteresting"
  }

  private object InterestingContext extends Context {
    override def get(key: ContextKey) = key match {
      case Interesting => Interesting
      case _ => null
    }
    override def toString = "Interesting"
  }

  // Describes how contexts are chosen
  def getCalleeTarget(caller: CGNode, site: CallSiteReference, callee: IMethod, actualParameters: Array[InstanceKey]): Context = {

    val opsPattern = ".*Ops.*".r
    val parallelArrayPattern = ".*ParallelArray.*".r

    //    if (!instankeKeyFactory.isInteresting(callee.getDeclaringClass()))
    //    	return UninterestingContext

    caller.getContext() match {

      case UninterestingContext => UninterestingContext

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

        val sequential = caller.getMethod().getSelector().toString().matches(".*Seq.*")

        new LoopContext(caller, !sequential, alphaIteration)

        // site.getProgramCounter() == 5 || site.getProgramCounter() == 2 || site.getProgramCounter() == 30);
      }

      // we're inside the loop (the objectKey test is to avoid recursion)
      case c: Context if c.get(Loop) != null => {
        val addThreadSafe =
          if (c.get(ThreadSafeOnClosure) == null && threadSafeOnClosure(caller, callee, actualParameters))
            new DelegatingContext(ThreadSafeContext, c)
          else
            c

        // we are not adding additional context when we know no races can happen from here on
        // and we also know all generated classes from here on are thread-safe
        // !!! 	question: "generatesSafeObjects" all generated objects from here on are thread-safe
        // 								or just the ones instantiated in callee (i.e., is it on transitive closure)
        //    
        // I'll make it more extreme... and simply remove all context from now on Uninteresting

        val addInteresting =
          if (addThreadSafe.get(Interesting) == null && (generatesSafeObjects(callee) || movesObjectsAround(callee)))
            new DelegatingContext(InterestingContext, addThreadSafe)
          else
            addThreadSafe

        if (addInteresting.get(ThreadSafeOnClosure) != null && addInteresting.get(Interesting) == null)
          return UninterestingContext

        val addMembrane =
          if (c.get(ObjectKey) == null && inApplicationScope(caller) && inPrimordialScope(callee) && actualParameters.size > 1)
            new DelegatingContext(ObjectContext(actualParameters(0)), addInteresting)
          else
            addInteresting
        addMembrane
      }

      // we're outside the loop
      case c: Context if c.get(Loop) == null =>
        callee match {
          case M(C(_, "ParallelArray"), opsPattern()) => new LoopCallSiteContext(caller, site)
          case M(_, parallelArrayPattern()) => new CallerSiteContextPair(caller, site, caller.getContext())
          case _ =>
            if (!instankeKeyFactory.isInteresting(callee.getDeclaringClass()))
              c
            //              return UninterestingContext
            else c
        }
    }
  }
  override def getRelevantParameters(caller: CGNode, site: CallSiteReference): IntSet = {
    EmptyIntSet.instance
  }
  def isInterestingForUs(callee: M) =
    ContainerUtil.isContainer(callee.getDeclaringClass()) || callee.toString().contains("DateFormat");
}

case class ObjectItem(o: O) extends ContextItem
case object ObjectKey extends ContextKey
case class ObjectContext(o: O) extends Context {
  def get(key: ContextKey): ContextItem = key match {
    case ObjectKey => ObjectItem(o)
    case _ => null
  }
}

// completely uninteresting context
case object Uninteresting extends ContextKey with ContextItem

// completely uninteresting context
case object Interesting extends ContextKey with ContextItem

// thread-safe on transitive closure
case object ThreadSafeOnClosure extends ContextKey with ContextItem

object Loop extends ContextKey
case class Loop(n: N, parallel: Boolean) extends ContextItem {
  def prettyPrint = "Loop: " + (if (parallel) "parallel" else "sequential")
}

object Iteration extends ContextKey
object AlphaIteration extends ContextItem
object BetaIteration extends ContextItem

// this is used for marking the entrance to the loop, so that it differentiated between loops at different locations
// e.g. n is the method that calls the apply
// site is the call site for the apply
// can be obtained from LoopContext(n, _); n.getContext()
case object LoopCallN extends ContextKey
case class LoopCallN(n: N) extends ContextItem
case object LoopCallSiteReference extends ContextKey
case class LoopCallSiteReference(site: CallSiteReference) extends ContextItem
class LoopCallSiteContext(val n: N, val site: CallSiteReference) extends Context with PrettyPrintable {
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