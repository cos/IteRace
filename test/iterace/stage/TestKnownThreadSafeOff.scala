package iterace.stage

import org.junit.Test
import iterace.util.log
import iterace.util.debug
import iterace.IteRaceOptions
import iterace.IteRaceOption

class TestKnownThreadSafeOff extends RaceAbstractTest("Lparticles/ParticleWithKnownThreadSafe") {
  
  override val options = IteRaceOptions(IteRaceOption.TwoThreadModel)
  
  debug.activate

  analysisScope.addBinaryDependency("particles");

  @Test def simpleKnownThreadSafe = expect("""
Loop: particles.ParticleWithKnownThreadSafe.simpleKnownThreadSafe(ParticleWithKnownThreadSafe.java:20)

Static: particles.ParticleWithKnownThreadSafe$ThreadSafe
 .noRaceOnMe
   (a)  particles.ParticleWithKnownThreadSafe$ThreadSafe.raceInThreadSafe(ParticleWithKnownThreadSafe$ThreadSafe.java:126)
   (b)  particles.ParticleWithKnownThreadSafe$ThreadSafe.raceInThreadSafe(ParticleWithKnownThreadSafe$ThreadSafe.java:126)
""")
  
  @Test def racePastKnownThreadSafe = expect("""
Loop: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:35)

Static: particles.ParticleWithKnownThreadSafe$ThreadSafe
 .noRaceOnMe
   (a)  particles.ParticleWithKnownThreadSafe$ThreadSafe.moveParticle(ParticleWithKnownThreadSafe$ThreadSafe.java:131)
   (b)  particles.ParticleWithKnownThreadSafe$ThreadSafe.moveParticle(ParticleWithKnownThreadSafe$ThreadSafe.java:131)
particles.Particle: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:33)
 .x
   (a)  particles.Particle.moveTo(Particle.java:16)
   (b)  particles.Particle.moveTo(Particle.java:16)
 .y
   (a)  particles.Particle.moveTo(Particle.java:17)
   (b)  particles.Particle.moveTo(Particle.java:17)
""")

	@Test def noRaceOnTransitiveClosureVerySimple = expect("""
Loop: particles.ParticleWithKnownThreadSafe.noRaceOnTransitiveClosureVerySimple(ParticleWithKnownThreadSafe.java:48)

Static: particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure
 .noRaceOnMe
   (a)  particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure.raceInThreadSafe(ParticleWithKnownThreadSafe$ThreadSafeOnClosure.java:113)
   (b)  particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure.raceInThreadSafe(ParticleWithKnownThreadSafe$ThreadSafeOnClosure.java:113)
""")
	
	@Test def noRaceOnTransitiveClosure = expect("""
Loop: particles.ParticleWithKnownThreadSafe.noRaceOnTransitiveClosure(ParticleWithKnownThreadSafe.java:63)

Static: particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure
 .noRaceOnMe
   (a)  particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure.moveParticle(ParticleWithKnownThreadSafe$ThreadSafeOnClosure.java:117)
   (b)  particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure.moveParticle(ParticleWithKnownThreadSafe$ThreadSafeOnClosure.java:117)
particles.Particle: particles.ParticleWithKnownThreadSafe.noRaceOnTransitiveClosure(ParticleWithKnownThreadSafe.java:61)
 .x
   (a)  particles.Particle.moveTo(Particle.java:16)
   (b)  particles.Particle.moveTo(Particle.java:16)
 .y
   (a)  particles.Particle.moveTo(Particle.java:17)
   (b)  particles.Particle.moveTo(Particle.java:17)
""")
	
	@Test def noRaceOnSafeObject = expect("""
Loop: particles.ParticleWithKnownThreadSafe.noRaceOnSafeObject(ParticleWithKnownThreadSafe.java:78)

particles.Particle: particles.ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator.getSafeParticle(ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator.java:105)
 .x
   (a)  particles.Particle.moveTo(Particle.java:16)
   (b)  particles.Particle.moveTo(Particle.java:16)
 .y
   (a)  particles.Particle.moveTo(Particle.java:17)
   (b)  particles.Particle.moveTo(Particle.java:17)
""")
	
	@Test def raceOnSafeObjectAccessedDirectly = expect("""
Loop: particles.ParticleWithKnownThreadSafe.raceOnSafeObjectAccessedDirectly(ParticleWithKnownThreadSafe.java:93)

particles.Particle: particles.ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator.getSafeParticle(ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator.java:105)
 .x
   (a)  particles.ParticleWithKnownThreadSafe$6.op(ParticleWithKnownThreadSafe$6.java:96)
   (b)  particles.ParticleWithKnownThreadSafe$6.op(ParticleWithKnownThreadSafe$6.java:96) [2x]
""")
}