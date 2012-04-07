package iterace.stage

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
import org.junit.Assert._
import scala.collection._
import org.scalatest.FunSuite
import org.junit.Rule
import org.junit.Test


class TestBubbleUp extends RaceAbstractTest("Lparticles/Particle") {

  analysisScope.addBinaryDependency("particles");

  @Test def raceOnArrayList = expect("""
Loop: particles.Particle.raceOnArrayList(Particle.java:676)

particles.Particle.raceOnArrayList(Particle.java:674)
 application level
   (a)  particles.Particle$49.op(Particle$49.java:679)
   (b)  particles.Particle$49.op(Particle$49.java:679)
""")
}