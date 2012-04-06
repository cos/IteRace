package iterace;

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
import org.junit.Assert._
import scala.collection._
import org.scalatest.FunSuite
import org.junit.Rule

@RunWith(classOf[JUnitRunner])
class TestBubbleUp extends RaceAbstractTest("Lparticles/Particle") {
  
  analysisScope.addBinaryDependency("particles");
  
  testResult("raceOnArrayList", """
Loop: particles.Particle.raceOnArrayList(Particle.java:676)

particles.Particle.raceOnArrayList(Particle.java:674)
 application level
   (a)  particles.Particle$49.op(Particle$49.java:679)
   (b)  particles.Particle$49.op(Particle$49.java:679)
""")
}