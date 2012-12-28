package iterace.stage

import org.junit.Test
import iterace.IteRaceOption._
import iterace.IteRaceOption
import org.junit.Ignore

class TestBubbleUp extends RaceAbstractTest {
 
  val entryClass = "Lparticles/ParticleUsingLibrary"
  override val options = Set[IteRaceOption](IteRaceOption.TwoThreads, IteRaceOption.BubbleUp)

  
  @Test @Ignore("There is some new bug. See Issue #2") def raceOnArrayList = expect("""
Loop: particles.ParticleUsingLibrary.raceOnArrayList(ParticleUsingLibrary.java:92)

java.util.ArrayList: particles.ParticleUsingLibrary.raceOnArrayList(ParticleUsingLibrary.java:90)
 application level
   (a)  particles.ParticleUsingLibrary$6.op(ParticleUsingLibrary$6.java:95)
   (b)  particles.ParticleUsingLibrary$6.op(ParticleUsingLibrary$6.java:95)
""")
}