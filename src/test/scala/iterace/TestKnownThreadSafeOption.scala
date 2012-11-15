package iterace
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import iterace.stage.RaceAbstractTest
import org.junit.Test
import wala.Dependency

class TestKnownThreadSafeOption extends RaceAbstractTest {

  override def entryClass = "Lparticles/Particle"

  override def options = Set(IteRaceOption.TwoThreadModel)

  @Test def noRaceOnStringConcatenation = expectNoRaces
  @Test def noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape = expect("""
Loop: particles.Particle.noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape(Particle.java:461)

particles.Particle.noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape(Particle.java:459)
 .origin
   (a)  particles.Particle$33.op(Particle$33.java:465)
   (b)  particles.Particle$33.op(Particle$33.java:465)
""")
  @Test def noRaceWhenPrintln = expectNoRaces;
}