package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection._
import scala.collection.JavaConversions._
import iterace.WALAConversions._
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import com.ibm.wala.ipa.callgraph.ContextKey
import com.ibm.wala.ipa.callgraph.ContextItem
import com.ibm.wala.dataflow.IFDS.PathEdge

class IteRace(startClass: String, startMethod: String, dependencies: java.util.List[String]) {
  val pa = new PointerAnalysis(startClass, startMethod, dependencies.toList)
  val helpers = new PAHelpers(pa)
  import pa._

  val stagePossibleRaces = new PossibleRaces(pa, helpers)
  val possibleRaces = stagePossibleRaces.races

  val stageLockSet = new LockSet(pa)

  val races = possibleRaces collect {
    case (loop, races) => {
      val locks = stageLockSet.getLocks(loop)
      val locksWithUniqueAbstractObjects = locks.filter({ pointsToUniqueAbstractObject(_) })
      val lockMap = stageLockSet.getLockSetMapping(loop, locksWithUniqueAbstractObjects)

      def filterSafe(rs: RSet) = {
        val newRS = new RSet()
        rs filter { ! isSafe(_) } foreach { newRS.add(_) }
        if(newRS isEmpty) None else Option(newRS)
      }

      def isSafe(r: R) = {
        val lockObjectsA = lockMap(r.a) map {_.p.pt} flatten
        val lockObjectsB = lockMap(r.b) map {_.p.pt} flatten

        lockObjectsA.size == 1 && lockObjectsB.size == 1 && (lockObjectsA & lockObjectsB).size == 1 
      }

      (loop, races collect {
        case (o, races) => (o, races map {
          case (f, rset) => (f, filterSafe(rset))
        } collect {case (ff, Some(ss)) => (ff, ss)} toMap ) 
      } filter {case (o, races) => !races.isEmpty} )
    }
  }
  
  def pointsToUniqueAbstractObject(l: Lock): Boolean = {
    l.p.pt.size == 1
  }
}

class AnalysisException(m: String) extends Throwable {

}