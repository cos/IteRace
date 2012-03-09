package iterace
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestNow extends RaceTest {
testResult("raceOnDifferntArrayIterationOneLoop","""
Loop: particles.Particle.raceOnDifferntArrayIterationOneLoop(Particle.java:367)

particles.Particle.raceOnDifferntArrayIterationOneLoop(Particle.java:365)
 .origin
   (a)  particles.Particle$27.op(Particle$27.java:371) [2]
   (b)  particles.Particle$27.op(Particle$27.java:371)
        particles.Particle$27.op(Particle$27.java:372)
particles.Particle$27.op(Particle$27.java:371)
 .x
   (a)  particles.Particle$27.op(Particle$27.java:370)
   (b)  particles.Particle$27.op(Particle$27.java:370)
""")
}