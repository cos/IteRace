package iterace.stage

import org.junit.Test
import iterace.IteRaceOption
import sppa.util.debug

class TestKnownThreadSafeOff extends RaceAbstractTest {
  
  override val options = Set[IteRaceOption](IteRaceOption.TwoThreads)
  
  val entryClass = "Lparticles/ParticleWithKnownThreadSafe"
  
  debug.activate

  @Test def simpleKnownThreadSafe = expect("""
Loop: particles.ParticleWithKnownThreadSafe.simpleKnownThreadSafe(ParticleWithKnownThreadSafe.java:20)

Static: particles.ParticleWithKnownThreadSafe$ThreadSafe
 .noRaceOnMe
   (a)  particles.ParticleWithKnownThreadSafe$ThreadSafe.raceInThreadSafe(ParticleWithKnownThreadSafe.java:126)
   (b)  particles.ParticleWithKnownThreadSafe$ThreadSafe.raceInThreadSafe(ParticleWithKnownThreadSafe.java:126)
""")
  
  @Test def racePastKnownThreadSafe = expect("""
Loop: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:35)

Static: particles.ParticleWithKnownThreadSafe$ThreadSafe
 .noRaceOnMe
   (a)  particles.ParticleWithKnownThreadSafe$ThreadSafe.moveParticle(ParticleWithKnownThreadSafe.java:131)
   (b)  particles.ParticleWithKnownThreadSafe$ThreadSafe.moveParticle(ParticleWithKnownThreadSafe.java:131)
particles.Particle: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:33)
 .x
   (a)  particles.Particle.moveTo(Particle.java:13)
   (b)  particles.Particle.moveTo(Particle.java:13)
 .y
   (a)  particles.Particle.moveTo(Particle.java:14)
   (b)  particles.Particle.moveTo(Particle.java:14)
""")

	@Test def noRaceOnTransitiveClosureVerySimple = expect("""
Loop: particles.ParticleWithKnownThreadSafe.noRaceOnTransitiveClosureVerySimple(ParticleWithKnownThreadSafe.java:48)

Static: particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure
 .noRaceOnMe
   (a)  particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure.raceInThreadSafe(ParticleWithKnownThreadSafe.java:113)
   (b)  particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure.raceInThreadSafe(ParticleWithKnownThreadSafe.java:113)
""")
	
	@Test def noRaceOnTransitiveClosure = expect("""
Loop: particles.ParticleWithKnownThreadSafe.noRaceOnTransitiveClosure(ParticleWithKnownThreadSafe.java:63)

Static: particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure
 .noRaceOnMe
   (a)  particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure.moveParticle(ParticleWithKnownThreadSafe.java:117)
   (b)  particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure.moveParticle(ParticleWithKnownThreadSafe.java:117)
particles.Particle: particles.ParticleWithKnownThreadSafe.noRaceOnTransitiveClosure(ParticleWithKnownThreadSafe.java:61)
 .x
   (a)  particles.Particle.moveTo(Particle.java:13)
   (b)  particles.Particle.moveTo(Particle.java:13)
 .y
   (a)  particles.Particle.moveTo(Particle.java:14)
   (b)  particles.Particle.moveTo(Particle.java:14)
""")
	
	@Test def noRaceOnSafeObject = expect("""
Loop: particles.ParticleWithKnownThreadSafe.noRaceOnSafeObject(ParticleWithKnownThreadSafe.java:78)

particles.Particle: particles.ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator.getSafeParticle(ParticleWithKnownThreadSafe.java:105)
 .x
   (a)  particles.Particle.moveTo(Particle.java:13)
   (b)  particles.Particle.moveTo(Particle.java:13)
 .y
   (a)  particles.Particle.moveTo(Particle.java:14)
   (b)  particles.Particle.moveTo(Particle.java:14)
""")
	
	@Test def raceOnSafeObjectAccessedDirectly = expect("""
Loop: particles.ParticleWithKnownThreadSafe.raceOnSafeObjectAccessedDirectly(ParticleWithKnownThreadSafe.java:93)

particles.Particle: particles.ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator.getSafeParticle(ParticleWithKnownThreadSafe.java:105)
 .x
   (a)  particles.ParticleWithKnownThreadSafe$6.op(ParticleWithKnownThreadSafe.java:96)
   (b)  particles.ParticleWithKnownThreadSafe$6.op(ParticleWithKnownThreadSafe.java:96) [2x]
""")
}