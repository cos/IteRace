package iterace;

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
import org.junit.Assert._
import iterace.LoopContextSelector.LoopCallSiteContext
import scala.collection._
import org.scalatest.FunSuite
import org.junit.Rule

@RunWith(classOf[JUnitRunner])
class TestBubbleUp extends RaceTest(List("particles", "../lib/parallelArray.mock"), "Lparticles/Particle") {
  
  override def result(iteRace: IteRace) = iteRace.shallowRaces

  testResult("raceOnArrayList", """
Loop: particles.Particle.raceOnArrayList(Particle.java:677)

particles.Particle.raceOnArrayList(Particle.java:675)
   (a)  particles.Particle$49.op(Particle$49.java:680) [12]
   (b)  particles.Particle$49.op(Particle$49.java:680) [12]
""")
}