package iterace
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import iterace.stage.RaceAbstractTest
import org.junit.Test

class TestKnownThreadSafeOption extends RaceAbstractTest {

  override def entryClass = "Lparticles/ParticleUsingLibrary"
    
  override def options = Set(IteRaceOption.TwoThreads, IteRaceOption.Filtering)

  @Test def noRaceOnStringConcatenation = expectNoRaces
  @Test def noRaceWhenPrintln = expectNoRaces;
}