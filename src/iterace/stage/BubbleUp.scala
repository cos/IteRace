package iterace
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.util.graph.impl.GraphInverter
import com.ibm.wala.util.collections.Filter
import iterace.util.S
import iterace.util.WALAConversions._
import iterace.util.crossProduct
import scala.collection.JavaConverters._
import scala.collection._

class BubbleUp(pa: RacePointerAnalysis) extends Stage {
  import pa._

  def apply(races: ProgramRaceSet): ProgramRaceSet = {
    var i = 0
    ProgramRaceSet(
    races map (r => {
      if (inPrimordialScope(r.a.n) || inPrimordialScope(r.b.n)) {
        val alphaAppLevelAccesses = bubbleUp(r.a)
        val betaAppLevelAccesses = bubbleUp(r.b)

        val pairs = crossProduct(alphaAppLevelAccesses, betaAppLevelAccesses)
        
//        i += 1; println(i + "  " + pairs.size + "   :    "+r.a+"   X   "+r.b)

        pairs collect {
          case (s1, s2) => {
            val objects =
              if (!s1.isStatic && !s2.isStatic)
                s1.refP.get.pt & s2.refP.get.pt
              else
                Seq(unknownO)
            objects collect { case o => new ShallowRace(r.l, o, s1, s2) }
          }
        } flatten
      } else
        Set(r)
    }) flatten)
  }

  private val bubbledUp: mutable.Map[S[I], Set[S[I]]] = mutable.Map()

  def bubbleUp(s: S[I]): Set[S[I]] = {
    if (inApplicationScope(s.n))
      return Set(s)

    bubbledUp.getOrElseUpdate(s, {
      val nodesInPrimordial = DFS.getReachableNodes(GraphInverter.invert(callGraph), Seq(s.n).asJava,
        new Filter[N]() {
          def accepts(n: N) = inPrimordialScope(n)
        }) asScala

      val invokeSs = (for (
        n <- nodesInPrimordial;
        predN <- callGraph.getPredNodes(n).asScala if inApplicationScope(predN)
      ) yield {
        val callSites = callGraph.getPossibleSites(predN, n).asScala
        val invokeIs = callSites map { predN.getIR().getCalls(_) } flatten
        val invokeSs = invokeIs map { S(predN, _) }
        invokeSs
      })
      return invokeSs.toSet.flatten
    })
  }
}

object BubbleUp extends StageConstructor {
  def apply(pa: RacePointerAnalysis) = {
    new BubbleUp(pa)
  }
}