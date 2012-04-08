package iterace.stage

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
import org.junit.Assert._
import scala.collection._
import org.scalatest.FunSuite
import org.junit.Rule
import iterace.util.log
import org.junit.Test
import org.junit.Ignore

class TestPotentialRaces extends RaceAbstractTest("Lparticles/Particle") {

//  log.activate

  override val stages: Seq[StageConstructor] = Seq()

  analysisScope.addBinaryDependency("particles")

  @Test def vacuouslyNoRace = expectNoRaces

  @Test def noRaceOnParameter = expectNoRaces

  /**
   * Is there a problem when the elements are initialized in another forall?
   */
  @Test def noRaceOnParameterInitializedBefore = expectNoRaces

  @Test def verySimpleRace = expect(
    """
Loop: particles.Particle.verySimpleRace(Particle.java:68)

particles.Particle.verySimpleRace(Particle.java:66)
 .x
   (a)  particles.Particle$5.op(Particle$5.java:71)
   (b)  particles.Particle$5.op(Particle$5.java:71)
""")

  /**
   * an part of an element is tainted in another forall
   */
  @Test def raceOnParameterInitializedBefore = expect(
    """
Loop: particles.Particle.raceOnParameterInitializedBefore(Particle.java:92)

particles.Particle.raceOnParameterInitializedBefore(Particle.java:81)
 .x
   (a)  particles.Particle$7.op(Particle$7.java:95)
   (b)  particles.Particle$7.op(Particle$7.java:95)
""")

  /**
   * Is it field sensitive?
   */
  @Test def noRaceOnANonSharedField = expectNoRaces

  /**
   * How context sensitive is it? Fails on 0-CFA Works on 1-CFA (old remark)
   */
  // but we actually solve it from the loop context
  @Test def oneCFANeededForNoRaces = expectNoRaces

  /**
   * How context sensitive is it? Fails on 0-CFA and 1-CFA Works on 2-CFA (old remark)
   */
  // but we actually solve it from the loop context
  @Test def twoCFANeededForNoRaces = expectNoRaces

  /**
   * How context sensitive is it? Fails on any CFA due to recursivity Might
   * work on smarter analyses
   */
  @Test def recursive = expectNoRaces

  /**
   * Disambiguate the trace for a race. The trace should contain
   * "shared.moveTo(5, 7);" but not "particle.moveTo(2, 3);"
   */
  @Test def disambiguateFalseRace = expect("""
Loop: particles.Particle.disambiguateFalseRace(Particle.java:189)

particles.Particle.disambiguateFalseRace(Particle.java:186)
 .x
   (a)  particles.Particle.moveTo(Particle.java:16)
   (b)  particles.Particle.moveTo(Particle.java:16)
 .y
   (a)  particles.Particle.moveTo(Particle.java:17)
   (b)  particles.Particle.moveTo(Particle.java:17)
""")

  @Test def ignoreFalseRacesInSeqOp = expectNoRaces

  @Test def raceBecauseOfOutsideInterference = expect("""
Loop: particles.Particle.raceBecauseOfOutsideInterference(Particle.java:232)

particles.Particle$15.op(Particle$15.java:235)
 .x
   (a)  particles.Particle$15.op(Particle$15.java:236)
   (b)  particles.Particle$15.op(Particle$15.java:236)
particles.Particle.raceBecauseOfOutsideInterference(Particle.java:229)
 .origin
   (a)  particles.Particle$15.op(Particle$15.java:235)
   (b)  particles.Particle$15.op(Particle$15.java:235)
        particles.Particle$15.op(Particle$15.java:236)
""")

  @Test def raceOnSharedObjectCarriedByArray = expect("""
Loop: particles.Particle.raceOnSharedObjectCarriedByArray(Particle.java:259)

particles.Particle$16.op(Particle$16.java:253)
 .x
   (a)  particles.Particle.moveTo(Particle.java:16)
   (b)  particles.Particle.moveTo(Particle.java:16)
 .y
   (a)  particles.Particle.moveTo(Particle.java:17)
   (b)  particles.Particle.moveTo(Particle.java:17)
""")

  @Test def raceBecauseOfDirectArrayLoad = expect("""
Loop: particles.Particle.raceBecauseOfDirectArrayLoad(Particle.java:274)

particles.Particle$18.op(Particle$18.java:279)
 .x
   (a)  particles.Particle$18.op(Particle$18.java:278)
   (b)  particles.Particle$18.op(Particle$18.java:278)
particles.Particle.raceBecauseOfDirectArrayLoad(Particle.java:271)
 .x
   (a)  particles.Particle$18.op(Particle$18.java:278)
   (b)  particles.Particle$18.op(Particle$18.java:278)
""")

  @Test def raceOnSharedReturnValue = expect("""
Loop: particles.Particle.raceOnSharedReturnValue(Particle.java:290)

particles.Particle.raceOnSharedReturnValue(Particle.java:288)
 .x
   (a)  particles.Particle$19.op(Particle$19.java:293)
   (b)  particles.Particle$19.op(Particle$19.java:293)
""")

  @Test def raceOnDifferntArrayIteration = expect("""
Loop: particles.Particle.raceOnDifferntArrayIteration(Particle.java:317)

particles.Particle$20.op(Particle$20.java:306)
 .x
   (a)  particles.Particle$22.op(Particle$22.java:320)
   (b)  particles.Particle$22.op(Particle$22.java:320)
""")

  @Test @Ignore def noRaceIfFlowSensitive =  expectNoRaces 

  @Test def raceOnDifferntArrayIterationOneLoop = expect("""
Loop: particles.Particle.raceOnDifferntArrayIterationOneLoop(Particle.java:367)

particles.Particle$27.op(Particle$27.java:371)
 .x
   (a)  particles.Particle$27.op(Particle$27.java:370)
   (b)  particles.Particle$27.op(Particle$27.java:370)
particles.Particle.raceOnDifferntArrayIterationOneLoop(Particle.java:365)
 .origin
   (a)  particles.Particle$27.op(Particle$27.java:371)
   (b)  particles.Particle$27.op(Particle$27.java:371)
        particles.Particle$27.op(Particle$27.java:372)
""")

  @Test def verySimpleRaceWithIndex = expect("""
Loop: particles.Particle.verySimpleRaceWithIndex(Particle.java:383)

particles.Particle.verySimpleRaceWithIndex(Particle.java:381)
 .x
   (a)  particles.Particle$28.op(Particle$28.java:386)
   (b)  particles.Particle$28.op(Particle$28.java:386)
""")

  @Test def verySimpleRaceToStaticObject = expect("""
Loop: particles.Particle.verySimpleRaceToStaticObject(Particle.java:399)

particles.Particle.<clinit>(Particle.java:392)
 .x
   (a)  particles.Particle$29.op(Particle$29.java:402)
   (b)  particles.Particle$29.op(Particle$29.java:402)
""")

  @Test def raceOnSharedFromStatic = expect("""
Loop: particles.Particle.raceOnSharedFromStatic(Particle.java:412)

particles.Particle.<clinit>(Particle.java:392)
 .y
   (a)  particles.Particle$30.op(Particle$30.java:416)
   (b)  particles.Particle$30.op(Particle$30.java:416)
""")

  @Test def staticMethod = expect("""
Loop: particles.Particle.staticMethod(Particle.java:642)

particles.Particle.<clinit>(Particle.java:392)
 .forceX
   (a)  particles.Particle.thisisstatic(Particle.java:652)
   (b)  particles.Particle.thisisstatic(Particle.java:652) [2x]
""");

  @Test def verySimpleRaceOnStaticField = expect("""
Loop: particles.Particle.verySimpleRaceOnStaticField(Particle.java:661)

Static: particles.Particle
 .staticX
   (a)  particles.Particle$48.op(Particle$48.java:664)
   (b)  particles.Particle$48.op(Particle$48.java:664)
""")

  @Test def raceOnArray = expect("""
Loop: particles.Particle.raceOnArray(Particle.java:491)

particles.Particle.raceOnArray(Particle.java:489)
 .[*]
   (a)  particles.Particle$35.op(Particle$35.java:494)
   (b)  particles.Particle$35.op(Particle$35.java:494)
""")

  /*
  @Test def raceInLibrary = expect("""
  ....
""")*/
}