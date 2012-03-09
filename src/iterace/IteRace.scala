package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.JavaConversions._
import WALAConversions._
import com.ibm.wala.util.graph.traverse.DFS
import iterace.LoopContextSelector.LoopN
import iterace.LoopContextSelector.LoopContext
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
 
class IteRace(startClass: String, startMethod: String, dependencies: List[String]) {
  val pa = new PointerAnalysis(startClass, startMethod, dependencies)
  import pa._

  //  val loopHeaders = DFS.iterateDiscoverTime(callGraph).filter(
  //    n => n match {
  //      case N(LoopContext(n1, _), _) if n1 == n => true
  //      case _ => false
  //    })

  def firstIteration(n: N): Boolean = {
    n.getContext() match {
      case LoopContext(_, true) => true
      case _ => false
    }
  }

  def inLoop(n: N): Boolean = {
    n.getContext() match {
      case LoopContext(_, _) => true
      case _ => false
    }
  }

  def inParallel(n: N): Boolean = {
    val seqPattern = ".*Seq.*".r
    n.getContext() match {
      case LoopContext(N(_, M(_, seqPattern())), _) => false
      case _ => true
    }
  }

  val icfg = ExplodedInterproceduralCFG.make(callGraph)

  val races: Map[N, Map[O, Map[F, RSet]]] = Map.empty[N, Map[O, Map[F, RSet]]]

  for (
    S(n1: N, i1: SSAPutInstruction) <- icfg if firstIteration(n1) && inParallel(n1);
    S(n2: N, i2: SSAFieldAccessInstruction) <- icfg if inLoop(n2) && !firstIteration(n2) &&
      i1.getDeclaredField() == i2.getDeclaredField()
  ) { 
    val oS1 = heap.getSuccNodes(P(n1, i1.getRef())).toSet
    val oS2 = heap.getSuccNodes(P(n2, i2.getRef())).toSet

    val oS = (oS1 & oS2)

    if (!oS.isEmpty) {
      // we have races here

      def l = n1.getContext().asInstanceOf[LoopContext].l
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

  // for each loop in the program
  // for each instruction the alpha iteration
  // if the instruction is a write
  // for each instruction in the beta iteration
  // if it writes to the same object and field
  // create a new race and add it to .races
}