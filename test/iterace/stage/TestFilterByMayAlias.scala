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
class TestFilterByMayAlias extends RaceAbstractTest("Lparticles/ParticleWithLocks") {

  analysisScope.addBinaryDependency("particles");

  override def result(iteRace: IteRace) = iteRace.races

  testNoRaces("vacuouslyNoRace")

  testResult("simpleRaceNoLocks",
    """
Loop: particles.ParticleWithLocks.simpleRaceNoLocks(ParticleWithLocks.java:27)

particles.ParticleWithLocks.simpleRaceNoLocks(ParticleWithLocks.java:25)
 .xyz
   (a)  particles.ParticleWithLocks$2.op(ParticleWithLocks$2.java:30)
   (b)  particles.ParticleWithLocks$2.op(ParticleWithLocks$2.java:30)
""")

  testResult("oneSimpleLock",
    """
Loop: particles.ParticleWithLocks.oneSimpleLock(ParticleWithLocks.java:42)

particles.ParticleWithLocks.oneSimpleLock(ParticleWithLocks.java:40)
 .xyz
   (a)  particles.ParticleWithLocks$3.op(ParticleWithLocks$3.java:47)
   (b)  particles.ParticleWithLocks$3.op(ParticleWithLocks$3.java:47)
""")

  testNoRaces("oneSimpleSafeLock")

  testNoRaces("imbricatedLocks")
}