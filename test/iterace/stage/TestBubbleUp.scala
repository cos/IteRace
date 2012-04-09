package iterace.stage

import org.junit.Test
import iterace.util.log

class TestBubbleUp extends RaceAbstractTest("Lparticles/ParticleUsingLibrary") {
  
  log.activate

  analysisScope.addBinaryDependency("particles");

  @Test def raceOnArrayList = expect("""
Loop: particles.ParticleUsingLibrary.raceOnArrayList(ParticleUsingLibrary.java:46)

java.util.ArrayList: particles.ParticleUsingLibrary.raceOnArrayList(ParticleUsingLibrary.java:44)
 application level
   (a)  particles.ParticleUsingLibrary$3.op(ParticleUsingLibrary$3.java:49)
   (b)  particles.ParticleUsingLibrary$3.op(ParticleUsingLibrary$3.java:49)
""")
}