package iterace.datastructure

import iterace.stage.StageConstructor
import org.junit.Test
import org.junit.Assert._
import wala.Dependency
import iterace.IteRace
import wala.AnalysisOptions
import com.typesafe.config.ConfigFactory
import sppa.util.JavaTest

class TestRaceSet extends JavaTest {
  @Test def bla: Unit = {
    val iterace = IteRace(AnalysisOptions("Lparticles/Particle", "raceBecauseOfOutsideInterference()V")(ConfigFactory.load), Set())
    assertEquals(iterace.races, ProgramRaceSet.fromRaceSets(iterace.races.getLowLevelRaceSets))
  }
}