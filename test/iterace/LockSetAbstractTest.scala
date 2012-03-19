package iterace
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import iterace.util.WALAConversions._
import iterace.LoopContextSelector.LoopCallSiteContext
import org.junit.Assert._
import scala.collection._
import iterace.oldjava.AnalysisScopeBuilder

abstract class LockSetTest(dependencies: List[String], startClass: String) extends FunSuite with BeforeAndAfter  {
  def analyze(method: String) = {
    var analysisScope = new AnalysisScopeBuilder("/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar");
    for (d <- dependencies) { analysisScope.addBinaryDependency(d); }
    
    val pa = new RacePointerAnalysis(startClass, method, analysisScope)
    (new LockSet(pa), pa)
  }
  
  def testGetLocks(method: String, result: String) = {
    test(method) {
      val (lockSet, pa) = analyze(method+"()V")
      import pa._
      val theLoop = getLoops().head
      val theLocks = lockSet.getLocks(theLoop)
      assertEquals(result, prettyPrint(theLocks))
    }
  }
  
  def prettyPrint(locks: Set[Lock]) = "{ "+(locks.map {_.prettyPrint}).reduceOption((x,y) => x+" , "+y).getOrElse("")+" }"
  
  def testGetLockSet(method: String, hint: String, result: String) = {
    test(method+" lockset") {
      val (locksetsolver, pa) = analyze(method+"()V")
      import pa._
      val theLoop = getLoops().head
      val lockSets = locksetsolver.getLockSetMapping(theLoop)
      import iterace.util._
      val s = statementsReachableFrom(theLoop.n).toStringSorted.find(x => {x!= null && x.toString().contains(hint)})
      val theLockSet = lockSets(s.get).toStringSorted
      assertEquals(result, prettyPrint(theLockSet))
    }
  }
}