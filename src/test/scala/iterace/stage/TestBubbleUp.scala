package iterace.stage

import org.junit.Test
import iterace.IteRaceOption._
import iterace.IteRaceOption
import org.junit.Ignore

class TestBubbleUp extends RaceAbstractTest {
 
  val entryClass = "Lparticles/ParticleUsingLibrary"
  override val options = Set[IteRaceOption](IteRaceOption.TwoThreads, IteRaceOption.BubbleUp)

  @Test  def raceOnArrayList = expect("""
Loop: particles.ParticleUsingLibrary.raceOnArrayList(ParticleUsingLibrary.java:90)

java.util.ArrayList: particles.ParticleUsingLibrary.raceOnArrayList(ParticleUsingLibrary.java:88)
 application level
   (a)  particles.ParticleUsingLibrary$6.op(ParticleUsingLibrary.java:93)
   (b)  particles.ParticleUsingLibrary$6.op(ParticleUsingLibrary.java:93)
""")
}