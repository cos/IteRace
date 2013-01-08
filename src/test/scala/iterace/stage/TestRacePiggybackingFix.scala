package iterace.stage

import org.junit.Test
import iterace.IteRaceOption._
import iterace.IteRaceOption
import org.junit.Ignore

class TestRacePiggybackingFix extends RaceAbstractTest {

  val entryClass = "Lparticles/TestRacePiggybackingFix"
  override val options = Set[IteRaceOption](IteRaceOption.TwoThreads)

  @Test def testCrossWithMain = expectNoRaces
  @Test def testCrossWithTheOtherThread = expect("""
Loop: particles.TestRacePiggybackingFix.testCrossWithTheOtherThread(TestRacePiggybackingFix.java:31)

particles.Particle: particles.TestRacePiggybackingFix.testCrossWithTheOtherThread(TestRacePiggybackingFix.java:29)
 .origin
   (a)  particles.TestRacePiggybackingFix$2.op(TestRacePiggybackingFix.java:35)
   (b)  particles.TestRacePiggybackingFix$2.op(TestRacePiggybackingFix.java:35)
        particles.TestRacePiggybackingFix$2.op(TestRacePiggybackingFix.java:36)
""")
}