package iterace.stage

import org.junit.Test
import iterace.util.log
import iterace.IteRaceOption._
import iterace.IteRaceOptions
import iterace.IteRaceOption

class TestBubbleUp extends RaceAbstractTest("Lparticles/ParticleUsingLibrary") {
 
  override val options = IteRaceOptions(IteRaceOption.TwoThreadModel, IteRaceOption.BubbleUp)

  analysisScope.addBinaryDependency("particles");

  @Test def raceOnArrayList = expect("""
Loop: particles.ParticleUsingLibrary.raceOnArrayList(ParticleUsingLibrary.java:92)

java.util.ArrayList: particles.ParticleUsingLibrary.raceOnArrayList(ParticleUsingLibrary.java:90)
 application level
   (a)  particles.ParticleUsingLibrary$6.op(ParticleUsingLibrary$6.java:95)
   (b)  particles.ParticleUsingLibrary$6.op(ParticleUsingLibrary$6.java:95)
""")
}