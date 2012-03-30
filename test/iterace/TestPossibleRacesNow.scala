package iterace
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestPossibleRacesNow extends RaceTest(List("particles", "../lib/parallelArray.mock"), "Lparticles/Particle") {
  override def result(iteRace: IteRace) = iteRace.possibleRaces
  
}