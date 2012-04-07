package iterace.util;

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
import org.junit.Assert._
import org.scalatest.FunSuite
import org.scalatest.matchers.MustMatchers
import iterace.RacePointerAnalysis
import iterace.pointeranalysis.AnalysisScopeBuilder

@RunWith(classOf[JUnitRunner])
class TestRacePointerAnalysis extends Spec with BeforeAndAfter with MustMatchers {

  val startClass = "Lkingdom/Dog"

  
  describe("instructionsReachableFrom") {
    
    def getReachableFrom(method: String) = {
      var analysisScope = new AnalysisScopeBuilder("/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar");
      analysisScope.addBinaryDependency("kingdom");
      
      val pa = new RacePointerAnalysis(startClass, method+"()V", analysisScope)
      val n = pa.findNode(method).get
      pa.statementsReachableFrom(n)
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