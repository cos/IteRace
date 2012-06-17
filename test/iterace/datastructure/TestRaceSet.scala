package iterace.datastructure

import iterace.IteRaceTest
import iterace.stage.StageConstructor
import org.junit.Test
import org.junit.Assert._

class TestRaceSet extends IteRaceTest {
  val entryClass = "Lparticles/Particle"
  analysisScope.addBinaryDependency("particles")
  
  @Test def bla:Unit = {
    val iterace = analyze("Lparticles/Particle","raceBecauseOfOutsideInterference()V", Set())
    assertEquals(iterace.races, ProgramRaceSet.fromRaceSets(iterace.races.getLowLevelRaceSets))
  }
}