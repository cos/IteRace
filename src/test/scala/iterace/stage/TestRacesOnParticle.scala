package iterace.stage

import org.junit.Test

import iterace.IteRaceOption

/**
 * Tests Particle.class (the same as TestPossibleRaces) but after the may-alias lock filter
 */

class TestRacesOnParticle extends TestPotentialRaces {
  override val options = Set[IteRaceOption](IteRaceOption.TwoThreads, IteRaceOption.DeepSynchronized)
}