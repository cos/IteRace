package iterace.stage

import org.junit.Test
import iterace.util.log
import iterace.IteRaceOption

/**
 * Tests Particle.class (the same as TestPossibleRaces) but after the may-alias lock filter
 */


class TestRacesOnParticle extends TestPotentialRaces {
  override val options = Set[IteRaceOption](FilterByLockMayAlias, BubbleUp)
}