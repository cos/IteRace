package iterace
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

import iterace.conversions._
import iterace.LoopContextSelector.LoopCallSiteContext
import org.junit.Assert._
import scala.collection._

abstract class LockSetTest(dependencies: List[String], startClass: String) extends FunSuite with BeforeAndAfter  {
  def analyze(method: String) = {
    val pa = new PointerAnalysis(startClass, method, dependencies)
    (new LockSet(pa), new Helpers(pa))
  }
  
  def testGetLocks(method: String, result: String) = {
    test(method) {
      val (locksetsolver, helpers) = analyze(method+"()V")
      val theLoop = helpers.getLoops().head
      val theLocks = locksetsolver.getLocks(theLoop)
      assertEquals(result, prettyPrint(theLocks))
    }
  }
  
  def prettyPrint(locks: Set[Lock]) = "{ "+(locks.map {_.prettyPrint}).reduceOption((x,y) => x+" , "+y).getOrElse("")+" }"
  
  def testGetLockSet(method: String, hint: String, result: String) = {
    test(method+" lockset") {
      val (locksetsolver, helpers) = analyze(method+"()V")
      import helpers._
      val theLoop = helpers.getLoops().head
      val lockSets = locksetsolver.getLockSetMapping(theLoop)
      val s = statementsReachableFrom(theLoop.n).find(x => {x!= null && x.toString().contains(hint)})
      val theLockSet = lockSets(s.get)
      assertEquals(result, prettyPrint(theLockSet))
    }
  }
}