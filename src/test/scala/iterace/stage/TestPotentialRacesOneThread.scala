package iterace.stage

import org.junit.Test

import iterace.IteRaceOption

/**
 * Tests Particle.class (the same as TestPotentialRaces) without the two-thread-model 
 */

class TestRacesOnParticleOneThread extends RaceAbstractTest {

  val entryClass = "Lparticles/Particle"
  override val options = Set[IteRaceOption]()

  @Test def vacuouslyNoRace = expectNoRaces

  /**
   * Is there a problem when the elements are initialized in another forall?
   */
  @Test def noRaceOnParameterInitializedBefore = expect("""
Loop: particles.Particle.noRaceOnParameterInitializedBefore(Particle.java:54)

particles.Particle: particles.Particle$3.op(Particle$3.java:50)
 .x
   (a)  particles.Particle$4.op(Particle$4.java:57)
   (b)  particles.Particle$4.op(Particle$4.java:57)
""")

  @Test def verySimpleRace = expect("""
Loop: particles.Particle.verySimpleRace(Particle.java:68)

particles.Particle: particles.Particle$5.op(Particle$5.java:72)
 .c
   (a)  particles.Particle.<init>(Particle.java:566)
   (b)  particles.Particle.<init>(Particle.java:566)
particles.Particle: particles.Particle.<init>(Particle.java:566)
 .c
   (a)  particles.Particle.<init>(Particle.java:566)
   (b)  particles.Particle.<init>(Particle.java:566)
particles.Particle: particles.Particle.verySimpleRace(Particle.java:66)
 .x
   (a)  particles.Particle$5.op(Particle$5.java:71)
   (b)  particles.Particle$5.op(Particle$5.java:71)
""")

  /**
   * an part of an element is tainted in another forall
   */
  @Test def raceOnParameterInitializedBefore = expect(
    """
Loop: particles.Particle.raceOnParameterInitializedBefore(Particle.java:92)

particles.Particle: particles.Particle.raceOnParameterInitializedBefore(Particle.java:81)
 .x
   (a)  particles.Particle$7.op(Particle$7.java:95)
   (b)  particles.Particle$7.op(Particle$7.java:95)
""")

  /**
   * Is it field sensitive?
   */
  @Test def noRaceOnANonSharedField = expect("""
Loop: particles.Particle.noRaceOnANonSharedField(Particle.java:106)

particles.Particle: particles.Particle$8.op(Particle$8.java:109)
 .c
   (a)  particles.Particle.<init>(Particle.java:566)
   (b)  particles.Particle.<init>(Particle.java:566)
 .origin
   (a)  particles.Particle$8.op(Particle$8.java:110)
   (b)  particles.Particle$8.op(Particle$8.java:110)
 .origin1
   (a)  particles.Particle$8.op(Particle$8.java:111)
   (b)  particles.Particle$8.op(Particle$8.java:111)
        particles.Particle$8.op(Particle$8.java:112)
particles.Particle: particles.Particle$8.op(Particle$8.java:111)
 .c
   (a)  particles.Particle.<init>(Particle.java:566)
   (b)  particles.Particle.<init>(Particle.java:566)
 .x
   (a)  particles.Particle$8.op(Particle$8.java:112)
   (b)  particles.Particle$8.op(Particle$8.java:112)
particles.Particle: particles.Particle.<init>(Particle.java:566)
 .c
   (a)  particles.Particle.<init>(Particle.java:566)
   (b)  particles.Particle.<init>(Particle.java:566)
""")
}