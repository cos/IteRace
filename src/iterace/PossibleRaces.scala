package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection._
import scala.collection.JavaConverters._
import iterace.util.WALAConversions._
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import util._

class PossibleRaces(pa: PointerAnalysis, helpers: PAHelpers) extends Function0[immutable.Set[Race]] {

  override def apply():immutable.Set[Race] = races
  
  import pa._
  import helpers._
  
  private val icfg = ExplodedInterproceduralCFG.make(callGraph)

  private var races: immutable.Set[Race] = immutable.Set[Race]()

  // TODO: transform this to visit: first x second iteration of each loop
  // not: everything x everything of everything
  for (
    S(n1: N, i1: SSAPutInstruction) <- icfg.asScala if firstIteration(n1) && inParallel(n1);
    S(n2: N, i2: SSAFieldAccessInstruction) <- icfg.asScala if inLoop(n2) && !firstIteration(n2) &&
      i1.getDeclaredField() == i2.getDeclaredField()
  ) {
    if (S(n1, i1).irNo == -1) println("HERE")
    if (S(n2, i2).irNo == -1) println("HERE")

    val oS1 = P(n1, i1.getRef()).pt.toSet
    val oS2 = P(n2, i2.getRef()).pt.toSet

    val oS = (oS1 & oS2)

    if (!oS.isEmpty) {
      // we have races here

      def l = Loop(n1.getContext().asInstanceOf[LoopContext].l)
      def f = i1.getDeclaredField()
      for (oBla <- oS) {
        val o = oBla.asInstanceOf[O]
        // it is enough to consider object created outside and in the the first iteration
        // so, filter out the objects created in the second iteration. they are duplicates of the first iteration
        if (o match {
          case O(n, i) => !inLoop(n) || firstIteration(n)
          case _ => true
        }) {
          races = races + Race(l, o, f, S(n1, i1), S(n2, i2))
        }
      }
    }
  }
}

case class Race(l: Loop, o: O, f: F, a: S[I], b: S[I]) extends PrettyPrintable {
  def prettyPrint() = {
    o.prettyPrint() + "   " + f.getName() + "\n" +
      " (a)  " + a.prettyPrint() + "\n" +
      " (b)  " + b.prettyPrint() + "\n"
  }
}