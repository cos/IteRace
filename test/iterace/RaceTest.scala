package iterace
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

import iterace.WALAConversions._
import iterace.LoopContextSelector.LoopCallSiteContext
import org.junit.Assert._
import scala.collection._

abstract class RaceTest extends FunSuite with BeforeAndAfter  {
  val dependencies = List("particles", "../lib/parallelArray.mock")
  val startClass = "Lparticles/Particle"

  def analyze(method: String) = new IteRace(startClass, method, dependencies)

  def printRaces(races: Map[N, Map[O, Map[F, RSet]]]): String = {
    val s = new StringBuilder
    s ++= "\n"
    for ((l, lr) <- races) {
      s ++= "Loop: "+l.getContext().asInstanceOf[LoopCallSiteContext].prettyPrint() + "\n\n"
      for ((o, fr) <- lr) {
        s ++= o.prettyPrint() + "\n"
        for ((f, rr) <- fr) {
          s ++= " ." + f.getName() + "\n"
          s ++= rr.prettyPrint() +"\n"
        }
      }
    }
    s.toString()
  }
  
  def testResult(method: String, result: String) = {
    test(method) {
      val iterace = analyze(method+"()V")
      assertEquals(result, printRaces(iterace.races))
    }
  }
  
  def testNoRaces(method: String) = testResult(method, "\n")
}