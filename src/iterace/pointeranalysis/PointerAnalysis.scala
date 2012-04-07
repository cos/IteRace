package iterace.pointeranalysis

import scala.util.matching.Regex
import scala.collection.JavaConverters._
import scala.collection._
import iterace.util.WALAConversions._
import com.ibm.wala.properties.WalaProperties
import iterace.pointeranalysis.AnalysisScopeBuilder
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.callgraph.Entrypoint
import com.ibm.wala.types.MethodReference
import com.ibm.wala.types.TypeReference
import com.ibm.wala.types.TypeName
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import com.ibm.wala.ipa.callgraph.AnalysisCache
import com.ibm.wala.ipa.cha.ClassHierarchy

class PointerAnalysis(entryClass: String, entryMethod: String, analysisScopeBuilder: AnalysisScopeBuilder) {
  
  /*
   * Below is the analysis initialization
   */
  val scope: AnalysisScope = analysisScopeBuilder.getAnalysisScope();
  val cha = ClassHierarchy.make(scope)
  private val options: AnalysisOptions = new AnalysisOptions(scope, entrypoints.asJava)
  val analysisCache = new AnalysisCache()
  private val builder = new CallGraphBuilder(options, analysisCache, cha, scope)
  val callGraph = builder.makeCallGraph(options);
  private val pointerAnalysis = builder.getPointerAnalysis();
  val heap = pointerAnalysis.getHeapGraph()

  private lazy val entrypoints = {
    val typeReference: TypeReference = TypeReference.findOrCreate(scope.getLoader(AnalysisScope.APPLICATION),
      TypeName.string2TypeName(entryClass))
    val methodReference: MethodReference = MethodReference.findOrCreate(typeReference,
      entryMethod.substring(0, entryMethod.indexOf('(')), entryMethod.substring(entryMethod.indexOf('(')))
    val entrypoint = new DefaultEntrypoint(methodReference, cha)
    Set(entrypoint)
  }

  /*
   * And these are helper methods - might be a good ideea to put them in a separate file someday
   */
  
  /**
   * find a call graph node that matches .*pattern.*
   */
  def findNode(pattern: String): Option[N] = {
    val p = (".*" + pattern + ".*")
    callGraph.asScala.find(n => n.getMethod.toString().matches(p))
  }

  /**
   * p.pt is the set of abstract objects pointed by p
   */
  implicit def pWithPointerObjects(p: P) = new {
    def pt: Set[O] = (for (o <- heap.getSuccNodes(p).asScala) yield o.asInstanceOf[O]).toSet
  }

  /**
   * localPtTo(o) is the local pointers to o
   */
  def localPtTo(o: O): Iterable[P] = {
    (for (p <- heap.getPredNodes(o).asScala if p.isInstanceOf[P]) yield p.asInstanceOf[P]).toIterable
  }
}