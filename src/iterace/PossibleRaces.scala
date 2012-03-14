package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection._
import scala.collection.JavaConversions._
import conversions._
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import util._

class PossibleRaces (pa: PointerAnalysis, helpers: PAHelpers) {
  import pa._
  import helpers._
  
  val icfg = ExplodedInterproceduralCFG.make(callGraph)
  
  val races: Map[Loop, Map[O, Map[F, RSet]]] = Map.empty[Loop, Map[O, Map[F, RSet]]]
  
  // TODO: transform this to visit: first x second iteration of each loop
  // not: everything x everything of everything
  for (
    S(n1: N, i1: SSAPutInstruction) <- icfg if firstIteration(n1) && inParallel(n1);
    S(n2: N, i2: SSAFieldAccessInstruction) <- icfg if inLoop(n2) && !firstIteration(n2) &&
      i1.getDeclaredField() == i2.getDeclaredField()
  ) {
    if(S(n1,i1).irNo == -1) println("HERE")
    if(S(n2,i2).irNo == -1) println("HERE")
    
    val oS1 = P(n1, i1.getRef()).pt.toSet
    val oS2 = P(n2, i2.getRef()).pt.toSet

    val oS = (oS1 & oS2)

    if (!oS.isEmpty) {
      // we have races here

      def l = Loop(n1.getContext().asInstanceOf[LoopContext].l)
      def racesInLoop = races.getOrElseUpdate(l, Map.empty[O, Map[F, RSet]])
      def f = i1.getDeclaredField()
      for (oBla <- oS) {
        val o = oBla.asInstanceOf[O]
        // it is enough to consider object created outside and in the the first iteration
        // so, filter out the objects created in the second iteration. they are duplicates of the first iteration
        if (o match {
          case O(n, i) => !inLoop(n) || firstIteration(n)
          case _ => true
        }) {
          val racesInLoopOnObject = racesInLoop.getOrElseUpdate(o, Map.empty[F, RSet])
          val theRaceSet = racesInLoopOnObject.getOrElseUpdate(f, RSet())
          theRaceSet += R(l, o, f, S(n1, i1), S(n2, i2))
        }
      }
    }
  }
}

case class R(l: Loop, o: O, f: F, a: S[I], b: S[I]) extends PrettyPrintable {
  def prettyPrint() = {
    o.prettyPrint() + "   " + f.getName() + "\n" +
      " (a)  " + a.prettyPrint() + "\n" +
      " (b)  " + b.prettyPrint() + "\n"
  }
}
class RSet extends mutable.HashSet[R] with PrettyPrintable {
  def prettyPrint(): String = {
    def printSameSet(p: (String, mutable.HashSet[R])) = p._1 + (if(p._2.size > 1) " [" + p._2.size + "]" else "")
    
    val aAccesses = this.groupBy(r => r.a.prettyPrint()).toStringSorted.map(printSameSet).toStringSorted.reduce(_ + "\n        " + _)
    val bAccesses = this.groupBy(r => r.b.prettyPrint()).toStringSorted.map(printSameSet).toStringSorted.reduce(_ + "\n        " + _)
    "   (a)  " + aAccesses + "\n   (b)  " + bAccesses
  }
}

object RSet {
  def apply() = {
    new RSet()
  }
} 