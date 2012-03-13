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
import com.ibm.wala.ipa.callgraph.ContextKey
import com.ibm.wala.ipa.callgraph.ContextItem
import com.ibm.wala.dataflow.IFDS.PathEdge

class IteRace(startClass: String, startMethod: String, dependencies: List[String]) {
  val pa = new PointerAnalysis(startClass, startMethod, dependencies)
  val helpers = new PAHelpers(pa)
  import pa._

  val stagePossibleRaces = new PossibleRaces(pa, helpers)
  val races = stagePossibleRaces.races

  val stageLockSet = new LockSet(pa)

  for ((l, r) <- races) {
    val locks = stageLockSet.getLocks(l)
    val locksWithUniqueAbstractObjects = locks.filter({ pointsToUniqueAbstractObject(_) })
    stageLockSet.getLockSetMapping(l, locksWithUniqueAbstractObjects)
  }

  def pointsToUniqueAbstractObject(l: Lock): Boolean = {
    l.p.pt.size == 1
  }
}

class AnalysisException(m: String) extends Throwable {

}