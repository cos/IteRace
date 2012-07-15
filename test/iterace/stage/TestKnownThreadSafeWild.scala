package iterace.stage

import org.junit.Test
import iterace.IteRaceOptions
import iterace.IteRaceOption
import util.debug

class TestKnownThreadSafeWild extends RaceAbstractTest {
  
  val entryClass = "Lparticles/ParticleUsingLibrary"
 
  override val options = IteRaceOptions(IteRaceOption.TwoThreadModel, IteRaceOption.KnownSafeFiltering)
  
  debug.activate

  analysisScope.addBinaryDependency("particles");

  @Test def noRaceWhenPrintln = expectNoRaces
  @Test def noRaceOnPattern = expectNoRaces
  @Test def noRaceOnSafeMatcher = expectNoRaces
  @Test def raceOnUnsafeMatcher = expectSomeRaces
  
//  @Test def racePastKnownThreadSafe = expect("""
//Loop: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:35)
//
//particles.Particle: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:33)
// .x
//   (a)  particles.Particle.moveTo(Particle.java:16)
//   (b)  particles.Particle.moveTo(Particle.java:16)
// .y
//   (a)  particles.Particle.moveTo(Particle.java:17)
//   (b)  particles.Particle.moveTo(Particle.java:17)
//""")

}