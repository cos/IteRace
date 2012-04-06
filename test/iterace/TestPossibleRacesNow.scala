package iterace
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestPossibleRacesNow extends RaceTest("Lparticles/Particle") {
  
  analysisScope.addBinaryDependency("particles");
  override def result(iteRace: IteRace) = iteRace.possibleRaces
  
}