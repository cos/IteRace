package iterace

import iterace.oldjava.WalaAnalysisStart;
import scala.util.matching.Regex
import scala.collection.JavaConversions._
import WALAConversions._

class PointerAnalysis(startClass: String, startMethod: String, dependencies: List[String]) {
  private var walaAnalysis = new WalaAnalysisStart;
  for (d <- dependencies) { walaAnalysis.addBinaryDependency(d); }
  walaAnalysis.setup(startClass, startMethod)

  val callGraph = walaAnalysis.callGraph
  val heap = walaAnalysis.pointerAnalysis.getHeapGraph()
  val analysisCache = walaAnalysis.cache

  def findNode(pattern: String): Option[N] = {
    val p = (".*" + pattern + ".*").r
    callGraph.find(n => n.getMethod.toString() match {
      case p() => true
      case _ => false
    })
  }
  
  implicit def pWithPointerObjects(p: P) = new {
    def pt:Iterable[O] = (for(o <- heap.getSuccNodes(p)) yield o.asInstanceOf[O]).toIterable
  }
  def localPtTo(o: O):Iterable[P] = {
    (for(p <- heap.getPredNodes(o) if p.isInstanceOf[P]) yield p.asInstanceOf[P]).toIterable
  }
}