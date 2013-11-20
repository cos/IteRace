package iterace.stage

import org.junit.Test
import iterace.IteRaceOption._
import iterace.IteRaceOption
import org.junit.Ignore

class TestAsync extends RaceAbstractTest {
  import IteRaceOption._
  val entryClass = "Lasyncsubjects/AndroidTest"
  override val options = Set[IteRaceOption](Filtering, TwoThreads, BubbleUp)
  
  override lazy val entryMethod = "onCreate(Landroid/os/Bundle;)V"

  @Test def compute = expect("")
}