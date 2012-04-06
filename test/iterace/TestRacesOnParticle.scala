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

/**
 * Tests Particle.class (the same as TestPossibleRaces) but after the may-alias lock filter
 */

@RunWith(classOf[JUnitRunner])
class TestRacesOnParticle extends TestPossibleRaces {
}