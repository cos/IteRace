package iterace.stage

import org.junit.Test
import iterace.IteRaceOption._
import iterace.IteRaceOption
import org.junit.Ignore

class TestPaperExample extends RaceAbstractTest {
  import IteRaceOption._
  val entryClass = "Lparticles/NBodySimulation"
  override val options = Set[IteRaceOption](Filtering, TwoThreads, BubbleUp, Synchronized)

  @Test def compute = expect("""

""")
}