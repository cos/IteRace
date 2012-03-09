package iterace;

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import iterace.WALAConversions._
import org.junit.Assert._
import org.scalatest.FunSuite
import org.scalatest.matchers.MustMatchers

@RunWith(classOf[JUnitRunner])
class TestHelpersForWALA extends Spec with BeforeAndAfter with MustMatchers {

  val dependencies = List("kingdom")
  val startClass = "Lkingdom/Dog"

  def analyze(method: String) = new PointerAnalysis(startClass, method, dependencies)
  
  describe("instructionsReachableFrom") {
    
    def getReachableFrom(method: String) = {
      val pa = analyze(method+"()V")
      val h = new HelpersForWALA(pa)
      val n = pa.findNode(method).get
      h.statementsReachableFrom(n)
    }
    
    it("must return only the 'return' instruction when given an empty method"){
      getReachableFrom("doesNothing") must have size 1
    }
    it("must return the set of instruction in a single method") {
      getReachableFrom("age") must have size 4
    }
    it("must return the set of instructions in a called method") {
      getReachableFrom("ageAlias") must have size 6
    }
    it("must return the set of instructions in called methods, with memoization") {
      getReachableFrom("ageage") must have size 6
    }
    it("must recurse gracefully") {
      getReachableFrom("bark") must have size 7
    }
  }
}