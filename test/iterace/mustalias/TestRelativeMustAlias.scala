package iterace;

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
import org.junit.Assert._
import scala.collection.mutable._
import org.scalatest.FunSuite
import org.junit.rules.TestName
import org.junit.Rule
import iterace.pointeranalysis.AnalysisScopeBuilder
import iterace.pointeranalysis.PointerAnalysis

@RunWith(classOf[JUnitRunner])
class TestRelativeMustAlias extends FunSuite with BeforeAndAfter {

  @Rule val testName = new TestName();
  val startClass = "Lkingdom/Animals"

  var analysisScope = new AnalysisScopeBuilder("/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar");
  analysisScope.addBinaryDependency("kingdom");

  def analyze(method: String) = new PointerAnalysis(startClass, method, analysisScope)
  val pa = analyze("live()V")

  def testMA(expected: Boolean)(message: String, relativeTo: String, p1: (String, String), p2: (String, String)): Unit = {
    test(message) {
      println("TEST:" + message)
      val ma = new RelativeMustAliasLevels(pa)
      val n = pa.findNode(relativeTo).get
      val n1 = pa.findNode(p1._1).get
      val v1 = n1.getV(p1._2)
      val n2 = pa.findNode(p2._1).get
      val v2 = n2.getV(p2._2)
      val result = ma.mustAlias(n)(P(n, v1), P(n, v2))
      assertEquals(expected, result)
    }
  }

  val test = testMA(true) _
  val testN = testMA(false) _
  def test(message: String, method: String, variable1: String, variable2: String): Unit =
    test(message, method, (method, variable1), (method, variable2))
  def testN(message: String, method: String, variable1: String, variable2: String): Unit =
    testN(message, method, (method, variable1), (method, variable2))

  test("n's arguments must-alias themselves",
    "rexIsRex", "rex", "rex")

  testN("n's argument must not alias something unrelated",
    "rexIsNotLaica", "rex", "laica")

  test("ma by reading the same field of the same object",
    "rexsFatherIsAlwaysTheSame", "cine1", "cine2")

  testN("not ma because of write to field",
    "sometimesDogsGetSold", "cine1", "cine2")

  test("ma even if there is a field write",
    "sometimesDogsGetSoldToTheSameOwner", "cine1", "cine2")
}