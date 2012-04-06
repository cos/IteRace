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
class TestFilterByMayAliasNow extends RaceTest("Lparticles/ParticleWithLocks") {
  
  analysisScope.addBinaryDependency("particles");
  
	testNoRaces("imbricatedLocks")
}