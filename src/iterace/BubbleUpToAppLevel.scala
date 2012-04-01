package iterace
import iterace.util.S
import iterace.util.WALAConversions._
import scala.collection.JavaConverters._
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.util.graph.impl.GraphInverter
import com.ibm.wala.util.collections.Filter
import iterace.util.crossProduct

class BubbleUpToAppLevel(pa: RacePointerAnalysis) extends Function1[Set[Race], Set[Race]] {
  import pa._

  def apply(races: Set[Race]): Set[Race] = {
    races map (r => {
      if (inPrimordialScope(r.a.n) || inPrimordialScope(r.b.n)) {
        val alphaAppLevelAccesses = bubbleUp(r.a)
        val betaAppLevelAccesses = bubbleUp(r.b)

        val pairs = crossProduct(alphaAppLevelAccesses, betaAppLevelAccesses)

        pairs collect {
          case (s1, s2) => {
            val objects =
              if (!s1.isStatic && !s2.isStatic)
                s1.refP.get.pt & s2.refP.get.pt
              else
                Set(unknownO)
            objects collect { case o => new ShallowRace(r.l, o, s1, s2) }
          }
        } flatten
      } else
        Set(r)
    }) flatten
  }

  def bubbleUp(s: S[I]): Set[S[I]] = {
    if (inApplicationScope(s.n))
      return Set(s)

    val nodesInPrimordial = DFS.getReachableNodes(GraphInverter.invert(callGraph), Seq(s.n).asJava,
      new Filter[N]() {
        def accepts(n: N) = inPrimordialScope(n)
      }) asScala

    val invokeSs = (for (
      n <- nodesInPrimordial;
      predN <- callGraph.getPredNodes(n).asScala if inApplicationScope(predN)
    ) yield {
      val callSites = callGraph.getPossibleSites(predN, n).asScala.toSet
      val invokeIs = callSites map { predN.getIR().getCalls(_) } flatten
      val invokeSs = invokeIs map { S(predN, _) }
      invokeSs
    })
    invokeSs.toSet.flatten
  }
}