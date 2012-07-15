package iterace.datastructure
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import wala.WALAConversions._
import org.junit.Assert._
import scala.collection._
import iterace.pointeranalysis.AnalysisScopeBuilder
import iterace.pointeranalysis.RacePointerAnalysis
import iterace.JavaTest
import iterace.IteRaceOptions
import iterace.IteRaceOption

abstract class LockSetAbstractTest(dependencies: List[String], startClass: String) extends JavaTest {
  def analyze(method: String) = {
    var analysisScope = AnalysisScopeBuilder("walaExclusions.txt");
    for (d <- dependencies) { analysisScope.addBinaryDependency(d); }

    val pa = new RacePointerAnalysis(startClass, method, analysisScope, IteRaceOptions(IteRaceOption.TwoThreadModel))
    (new LockSets(pa, new MayAliasLockConstructor(pa)), pa)
  }

  /**
   * Test set of all found locks
   */
  def assertAllLocks(result: String, entry:String = testName.getMethodName()): Unit = {
    val (lockSet, pa) = analyze(entry + "()V")
    import pa._
    val theLoop = parLoops.head
    val theLocks = lockSet.getLocks(theLoop)
    assertEquals(result, prettyPrint(theLocks))
  }

  def prettyPrint(locks: Set[Lock]) = "{ " + (locks.map { _.prettyPrint }).reduceOption((x, y) => x + " , " + y).getOrElse("") + " }"

  /**
   * Test lockset of "hinted" statement
   */
  def assertLockSet(hint: String, result: String): Unit = assertLockSet(testName.getMethodName(), hint, result)
  def assertLockSet(entry: String, hint: String, result: String): Unit = {
    val (locksetsolver, pa) = analyze(entry + "()V")
    import pa._
    val theLoop = loops.head
    val lockSets = locksetsolver.getLockSetMapping(theLoop)
    import iterace.util._
    val s = statementsReachableFrom(theLoop.n).toStringSorted.find(x => { x != null && x.toString().contains(hint) })
    val theLockSet = lockSets(s.get).toStringSorted
    assertEquals(result, prettyPrint(theLockSet))
  }
}