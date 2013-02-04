package iterace.util

import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.util.graph.GraphSlicer
import com.ibm.wala.util.graph.impl.GraphInverter
import com.ibm.wala.ipa.callgraph.CallGraph
import wala.WALAConversions._
import wala.util.viz.DotUtil
import com.ibm.wala.ipa.slicer.Slicer
import com.ibm.wala.ipa.slicer.SDG
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis
import iterace.pointeranalysis.Loop
import iterace.pointeranalysis.Iteration
import iterace.pointeranalysis.AlphaIteration
import edu.illinois.wala.S
import scala.collection.JavaConverters._
import wala.util.viz.NodeDecorator
import com.ibm.wala.ipa.slicer.NormalStatement
import com.ibm.wala.ipa.slicer.Statement
import com.ibm.wala.util.graph.Graph
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex
import com.ibm.wala.util.graph.GraphUtil
import com.ibm.wala.util.graph.GraphPrint
import iterace.datastructure.isActuallyLibraryCode
import iterace.datastructure.isActuallyApplicationScope

class Tracer(callGraph: CallGraph, pa: PointerAnalysis) {
  print("Computing SDG... ")
  val sdg = new SDG(callGraph, pa, Slicer.DataDependenceOptions.NO_BASE_NO_EXCEPTIONS, Slicer.ControlDependenceOptions.NONE)
  println("DONE")

  def trace(a: S[I]) = {
    val beforeNodes = DFS.getReachableNodes(GraphInverter.invert(callGraph), Set(a.n).asJava)

    //    beforeNodes.asScala foreach { n => println("n: " + n) }

    val g = GraphSlicer.prune(callGraph, { n: N =>
      n.c(Iteration) == AlphaIteration && isActuallyApplicationScope(n)
    }) // todo: also check that it is the same loop

    DotUtil.dotGraph(g, "%x".format(a.hashCode), new NodeDecorator() {
      def getLabel(n: Object) = n.asInstanceOf[N].prettyPrint + " | " + n.hashCode
      def getDecoration(n: Object) = ""
      def shouldDisplay(n: Object) = true
      def getGroup(n: Object) = ""
    }, true)

    //    val slice = Slicer.computeBackwardSlice(sdg, new NormalStatement(a.n, a.irNo))
    //    println("DONE")

    //    println(slice.size())
    //    slice.asScala collect { case s: StatementWithInstructionIndex => s } foreach { s => println(s.prettyPrint) }
    //
    //    val g1 = GraphSlicer.prune(sdg, { s: Statement =>
    //      slice.contains(s) && s.n.c(Iteration) == AlphaIteration // todo: also check that it is the same loop, see: https://github.com/cos/privatization/blob/racefix/src/test/scala/racefix/evaluation/JMolLCDsOverRaces.scala#L122
    //    })
    //
    //    dotTrace(g1)
  }

  def dotHeapGraph() {
    DotUtil.dotGraph(pa.getHeapGraph(), "heapGraph", new NodeDecorator() {
      def getLabel(x: Object) = x match {
        case o: O => o.prettyPrint
        case p: P => p.prettyPrint
        case x => x.toString()
      }
      def getDecoration(n: Object) = ""
      def shouldDisplay(n: Object) = true
      def getGroup(n: Object) = ""
    }, true)
  }

  private def dotTrace(g: Graph[Statement], name: String = "testPlay") {
    import com.ibm.wala.ipa.slicer.HeapStatement
    import com.ibm.wala.ipa.slicer.HeapStatement._

    DotUtil.dotGraph(g, name, new NodeDecorator() {
      def getLabel(s: Object) = {
        val ss = s.asInstanceOf[Statement]
        "" + (ss match {
          case s: StatementWithInstructionIndex =>
            s.getNode().m.lineNoFromIRNo(S(s.getNode(), s.getInstruction()).irNo)
          //            S(s.getNode(), s.getInstruction()).prettyPrint // it is enclosed in the method as a cluster
          case s: HeapStatement =>
            (s match {
              case s: HeapParamCaller => "" + s.getNode().m.lineNoFromIRNo(S(s.getNode(), s.getCall()).irNo) + ":"
              case s: HeapReturnCaller => "" + s.getNode().m.lineNoFromIRNo(S(s.getNode(), s.getCall()).irNo) + ":"
              case _ => ""
            }) + s.getLocation.prettyPrint("\\n")

          case s: Statement =>
            s.getKind().toString
        }) + "\\n"
      }

      def getDecoration(s: Object) = {
        val nodeColor = ("%x".format(s.asInstanceOf[Statement].getNode().hashCode).substring(0, 6))
        (s match {
          case s: HeapParamCaller => "style = \"filled\", "
          case s: HeapReturnCaller => "style = \"filled, dashed\", "
          case s: HeapParamCallee => "style = \"filled, rounded\",  "
          case s: HeapReturnCallee => "style = \"filled, rounded, dashed\", "
          case _ => "shape=oval, style = \"filled\", "
        }) + "margin=\"0.1,0.1\", color=\"#" + nodeColor + "\" fillcolor=\"#" + nodeColor + "25\""
      }

      def shouldDisplay(s: Object) = true

      def getGroup(o: Object) = o.asInstanceOf[Statement].n.prettyPrint

    }, true)
  }
}