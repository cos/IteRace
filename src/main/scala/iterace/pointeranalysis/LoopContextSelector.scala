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
import edu.illinois.wala.Facade._
import scala.collection._
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ipa.callgraph.propagation.ContainerUtil
import com.ibm.wala.ipa.callgraph.impl.Everywhere
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallerSiteContextPair
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import iterace.AnalysisException
import com.ibm.wala.ipa.callgraph.DelegatingContext
import iterace.datastructure.threadSafeOnClosure
import iterace.datastructure.generatesSafeObjects
import edu.illinois.wala.Facade._
import iterace.datastructure.movesObjectsAround
import iterace.IteRaceOption
import iterace.datastructure.isActuallyLibraryCode
import iterace.datastructure.isActuallyApplicationScope
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import com.ibm.wala.ipa.callgraph.propagation.cfa.ContainerContextSelector
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.util.intset.BitVectorIntSetFactory
import com.ibm.wala.util.intset.IntSetUtil
import edu.illinois.wala.PrettyPrintable
import edu.illinois.wala.classLoader.M
import edu.illinois.wala.classLoader.C
import edu.illinois.wala.ssa.V
import edu.illinois.wala.classLoader.CodeLocation
import edu.illinois.wala.classLoader.ProgramCounter

class LoopContextSelector(options: Set[IteRaceOption], cha: IClassHierarchy, instankeKeyFactory: ZeroXInstanceKeys) extends ContextSelector {
  // this is the context for all the nodes in the loop iterations
  private case class LoopContext(
    l: CGNode,
    parallel: Boolean,
    alphaIteration: Boolean,
    arguments: List[Boolean] = List(),
    threadSafeOnClosure: Boolean = false,
    interesting: Boolean = false,
    container: Option[Context] = None) extends Context {
    val loop = Loop(l, parallel)
    val iteration = if (alphaIteration) AlphaIteration else BetaIteration

    def apply(key: ContextKey) = get(key)

    def get(key: ContextKey): ContextItem = key match {
      case Loop => loop
      case Iteration => iteration
      case ThreadSafeOnClosure if threadSafeOnClosure => ThreadSafeOnClosure
      case Interesting if interesting => Interesting
      case _ => null
    }
    override def toString =
      (if (parallel) "P" else "S") + "-" +
        (if (alphaIteration) "a" else "b") +
        " (" + (arguments map { if (_) "s" else "-" } mkString (",")) + ") " +
        (if (threadSafeOnClosure) "TSC" else "") +
        (if (interesting) "ITT" else "")
  }

  private val opsPattern = ".*Ops.*".r
  private val parallelArrayPattern = ".*ParallelArray.*".r

  val containerContextSelector = new ContainerContextSelector(cha, instankeKeyFactory)

  // Describes how contexts are chosen
  def getCalleeTarget(caller: CGNode, site: CallSiteReference, callee: IMethod, actualParameters: Array[InstanceKey]): Context = {

    caller.getContext() match {

      // we've entered the loop but not the iteration. callee is the iteration
      case c: LoopCallSiteContext => {
        val invocations = caller.getIR().getCalls(site);
        if (invocations.length != 1)
          throw new AnalysisException("There should be only invocation for an mock operation call site");

        val e0PutVals = caller.getIR().getInstructions().
          collect { case x: SSAPutInstruction if x.getDeclaredField.toString.contains("e0") => x.v }

        val e0GetVals = caller.getIR().getInstructions().
          collect { case x: SSAGetInstruction if x.getDeclaredField.toString.contains("e0") => x.d }

        val e0Vals = (e0PutVals ++ e0GetVals).toSet

        val invoke = invocations.head
        
        val defAndUses = (invoke.uses :+ V(invoke.getDef())) toSet

        val alphaIteration =
          if (options.contains(IteRaceOption.TwoThreads))
            !(defAndUses & e0Vals).isEmpty
          else
            true

        val sequential = caller.getMethod().getSelector().toString().matches(".*Seq.*")

        new LoopContext(caller, !sequential, alphaIteration)

        // site.getProgramCounter() == 5 || site.getProgramCounter() == 2 || site.getProgramCounter() == 30);
      }

      // we're inside the loop (the objectKey test is to avoid recursion)
      case c: LoopContext if c.get(Loop) != null => {
        var newC: LoopContext = c

        if (options(IteRaceOption.Filtering)) {
          newC = if (!c.is(ThreadSafeOnClosure) && threadSafeOnClosure(caller, callee, actualParameters))
            newC.copy(threadSafeOnClosure = true)
          else
            newC

          // we are not adding additional context when we know no races can happen from here on
          // and we also know all generated classes from here on are thread-safe
          // !!! 	question: "generatesSafeObjects" all generated objects from here on are thread-safe
          // 								or just the ones instantiated in callee (i.e., is it on transitive closure)
          //    

          newC = if (!newC.is(Interesting) && (generatesSafeObjects(callee) || movesObjectsAround(callee)))
            newC.copy(interesting = true)
          else
            newC
        }

        newC = newC.copy(arguments = actualParameters map {
          case o: AllocationSiteInNode if o.getNode().c(Iteration) != c(Iteration) => true
          case _ => false
        } toList)

        newC = newC.copy(container = Option(containerContextSelector.getCalleeTarget(caller, site, callee, actualParameters)))

        newC
      }

      // we're outside the loop
      case c: Context =>
        callee match {
          case M(C(_, "ParallelArray"), opsPattern()) => new LoopCallSiteContext(caller, site)
          case M(_, parallelArrayPattern()) => new CallerSiteContextPair(caller, site, caller.getContext())
          case _ => c
        }
    }
  }

  override def getRelevantParameters(caller: CGNode, site: CallSiteReference): IntSet = EmptyIntSet.instance
  
  def isInterestingForUs(callee: M) =
    ContainerUtil.isContainer(callee.getDeclaringClass()) || callee.toString().contains("DateFormat");
}

// completely uninteresting context
case object Interesting extends ContextKey with ContextItem

// thread-safe on transitive closure
case object ThreadSafeOnClosure extends ContextKey with ContextItem


trait MayRunInParallel {
  def prettyPrintDetail: String
}

object Loop extends ContextKey
case class Loop(n: N, parallel: Boolean) extends ContextItem with MayRunInParallel {
  def prettyPrint = "Loop: " + (if (parallel) "parallel" else "sequential")
  def prettyPrintDetail = n.getContext().asInstanceOf[LoopCallSiteContext].prettyPrint + "\n\n" 
}

object Iteration extends ContextKey
object AlphaIteration extends ContextItem {
  override def toString = "alpha"
}
object BetaIteration extends ContextItem {
  override def toString = "beta"
}

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

  def prettyPrint = ProgramCounter(site.getProgramCounter()) map { new CodeLocation(n.m, _) } map { _.toString } getOrElse ""
}