package iterace.datastructure
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import iterace.util.WALAConversions._
import org.junit.Assert._
import scala.collection._
import iterace.pointeranalysis.AnalysisScopeBuilder
import iterace.pointeranalysis.RacePointerAnalysis
import iterace.JavaTest


abstract class LockSetAbstractTest(dependencies: List[String], startClass: String) extends JavaTest {
  def analyze(method: String) = {
    var analysisScope = new AnalysisScopeBuilder("/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar");
    analysisScope.setExclusionsFile("walaExclusions.txt");
    for (d <- dependencies) { analysisScope.addBinaryDependency(d); }

    val pa = new RacePointerAnalysis(startClass, method, analysisScope)
    (new LockSet(pa), pa)
  }
  
  /**
   * Test set of all found locks
   */
  def assertAllLocks(result: String):Unit = {
      val (lockSet, pa) = analyze(testName.getMethodName() + "()V")
      import pa._
      val theLoop = loops.head
      val theLocks = lockSet.getLocks(theLoop)
      assertEquals(result, prettyPrint(theLocks))
  }

  def prettyPrint(locks: Set[Lock]) = "{ " + (locks.map { _.prettyPrint }).reduceOption((x, y) => x + " , " + y).getOrElse("") + " }"

  /**
   * Test lockset of "hinted" statement
   */
  def assertLockSet(hint: String, result: String):Unit = {
      val (locksetsolver, pa) = analyze(testName.getMethodName() + "()V")
      import pa._
      val theLoop = loops.head
      val lockSets = locksetsolver.getLockSetMapping(theLoop)
      import iterace.util._
      val s = statementsReachableFrom(theLoop.n).toStringSorted.find(x => { x != null && x.toString().contains(hint) })
      val theLockSet = lockSets(s.get).toStringSorted
      assertEquals(result, prettyPrint(theLockSet))
  }
}