package iterace.stage

import org.junit.Test
import iterace.util.log
import iterace.util.debug
import iterace.IteRaceOption
import iterace.IteRaceOptions

class TestKnownThreadSafe extends RaceAbstractTest("Lparticles/ParticleWithKnownThreadSafe") {
  
  override val options = IteRaceOptions(IteRaceOption.TwoThreadModel, IteRaceOption.KnownSafeFiltering)
  
  debug.activate

  analysisScope.addBinaryDependency("particles");

  @Test def simpleKnownThreadSafe = expectNoRaces
  
  @Test def racePastKnownThreadSafe = expect("""
Loop: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:35)

particles.Particle: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:33)
 .x
   (a)  particles.Particle.moveTo(Particle.java:16)
   (b)  particles.Particle.moveTo(Particle.java:16)
 .y
   (a)  particles.Particle.moveTo(Particle.java:17)
   (b)  particles.Particle.moveTo(Particle.java:17)
""")

	@Test def noRaceOnTransitiveClosureVerySimple = expectNoRaces
	
	@Test def noRaceOnTransitiveClosure = expectNoRaces
	
	@Test def noRaceOnSafeObject = expectNoRaces
	
	@Test def raceOnSafeObjectAccessedDirectly = expect("""
Loop: particles.ParticleWithKnownThreadSafe.raceOnSafeObjectAccessedDirectly(ParticleWithKnownThreadSafe.java:93)

particles.Particle: particles.ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator.getSafeParticle(ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator.java:105)
 .x
   (a)  particles.ParticleWithKnownThreadSafe$6.op(ParticleWithKnownThreadSafe$6.java:96)
   (b)  particles.ParticleWithKnownThreadSafe$6.op(ParticleWithKnownThreadSafe$6.java:96) [2x]
""")
}