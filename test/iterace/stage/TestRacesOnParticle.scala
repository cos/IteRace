package iterace.stage

import org.junit.Test
import iterace.util.log

/**
 * Tests Particle.class (the same as TestPossibleRaces) but after the may-alias lock filter
 */


class TestRacesOnParticle extends TestPotentialRaces {
  override val stages: Seq[StageConstructor] = Seq(FilterByLockMayAlias, BubbleUp);
}