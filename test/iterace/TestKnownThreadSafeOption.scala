package iterace
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import iterace.stage.RaceAbstractTest
import org.junit.Test


class TestKnownThreadSafeOption extends RaceAbstractTest {
  
  val entryClass = "Lparticles/Particle"

  analysisScope.addBinaryDependency("particles");

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