package iterace.stage

import org.junit.Test
import iterace.IteRaceOption
import sppa.util.debug

class TestKnownThreadSafe extends RaceAbstractTest {
  
  val entryClass = "Lparticles/ParticleWithKnownThreadSafe"
  
  override val options = Set[IteRaceOption](IteRaceOption.TwoThreads, IteRaceOption.Filtering)
  
  debug.activate

  @Test def simpleKnownThreadSafe = expectNoRaces
  
  @Test def racePastKnownThreadSafe = expect("""
Loop: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:35)

particles.Particle: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:33)
 .x
   (a)  particles.Particle.moveTo(Particle.java:13)
   (b)  particles.Particle.moveTo(Particle.java:13)
 .y
   (a)  particles.Particle.moveTo(Particle.java:14)
   (b)  particles.Particle.moveTo(Particle.java:14)
""")

	@Test def noRaceOnTransitiveClosureVerySimple = expectNoRaces
	
	@Test def noRaceOnTransitiveClosure = expectNoRaces
	
	@Test def noRaceOnSafeObject = expectNoRaces
	
	@Test def raceOnSafeObjectAccessedDirectly = expect("""
Loop: particles.ParticleWithKnownThreadSafe.raceOnSafeObjectAccessedDirectly(ParticleWithKnownThreadSafe.java:93)

particles.Particle: particles.ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator.getSafeParticle(ParticleWithKnownThreadSafe.java:105)
 .x
   (a)  particles.ParticleWithKnownThreadSafe$6.op(ParticleWithKnownThreadSafe.java:96)
   (b)  particles.ParticleWithKnownThreadSafe$6.op(ParticleWithKnownThreadSafe.java:96) [2x]
""")
}