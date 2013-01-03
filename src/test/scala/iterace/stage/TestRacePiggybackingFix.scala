package iterace.stage

import org.junit.Test
import iterace.IteRaceOption._
import iterace.IteRaceOption
import org.junit.Ignore

class TestRacePiggybackingFix extends RaceAbstractTest {
 
  val entryClass = "Lparticles/TestRacePiggybackingFix"
  override val options = Set[IteRaceOption](IteRaceOption.TwoThreads)

  @Test  def testCross = expect("""
Loop: particles.TestRacePiggybackingFix.testCross(TestRacePiggybackingFix.java:16)

particles.Particle: particles.TestRacePiggybackingFix.testCross(TestRacePiggybackingFix.java:14)
 .x
   (a)  particles.TestRacePiggybackingFix$1.op(TestRacePiggybackingFix$1.java:20)
   (b)  particles.TestRacePiggybackingFix$1.op(TestRacePiggybackingFix$1.java:20)
""")
}