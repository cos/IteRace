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
import iterace.IteRace

@RunWith(classOf[JUnitRunner])
class TestFilterByMayAlias extends RaceAbstractTest("Lparticles/ParticleWithLocks") {

  analysisScope.addBinaryDependency("particles");

  override def result(iteRace: IteRace) = iteRace.races
  
  testNoRaces("vacuouslyNoRace")

  testResult("noLocks",
    """
Loop: particles.ParticleWithLocks.noLocks(ParticleWithLocks.java:29)

particles.ParticleWithLocks.noLocks(ParticleWithLocks.java:27)
 .xyz
   (a)  particles.ParticleWithLocks$2.op(ParticleWithLocks$2.java:32)
   (b)  particles.ParticleWithLocks$2.op(ParticleWithLocks$2.java:32)
""")

  testResult("oneSimpleLock",
    """
Loop: particles.ParticleWithLocks.oneSimpleLock(ParticleWithLocks.java:44)

particles.ParticleWithLocks.oneSimpleLock(ParticleWithLocks.java:42)
 .xyz
   (a)  particles.ParticleWithLocks$3.op(ParticleWithLocks$3.java:49)
   (b)  particles.ParticleWithLocks$3.op(ParticleWithLocks$3.java:49)
""")

  testNoRaces("oneSimpleSafeLock")

  testResult("imbricatedLocks","""
Loop: particles.ParticleWithLocks.imbricatedLocks(ParticleWithLocks.java:97)

particles.ParticleWithLocks.imbricatedLocks(ParticleWithLocks.java:95)
 .xyz
   (a)  particles.ParticleWithLocks$6.op(ParticleWithLocks$6.java:105)
   (b)  particles.ParticleWithLocks$6.op(ParticleWithLocks$6.java:105)
""")
}