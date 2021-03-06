package iterace.stage

import org.junit.Test
import org.junit.Ignore
import iterace.IteRaceOption

class TestPotentialRaces extends RaceAbstractTest {

  val entryClass = "Lparticles/Particle"

  override def options = Set(IteRaceOption.TwoThreads)

  //  log.activate
  //  override val options = IteRaceOptions.all

  @Test def vacuouslyNoRace = expectNoRaces

  /**
   * Is there a problem when the elements are initialized in another forall?
   */
  @Test def noRaceOnParameterInitializedBefore = expectNoRaces

  @Test def verySimpleRace = expect(
    """
Loop: particles.Particle.verySimpleRace(Particle.java:68)

particles.Particle: particles.Particle.verySimpleRace(Particle.java:66)
 .x
   (a)  particles.Particle$5.op(Particle.java:71)
   (b)  particles.Particle$5.op(Particle.java:71)
""")

  /**
   * an part of an element is tainted in another forall
   */
  @Test def raceOnParameterInitializedBefore = expect(
    """
Loop: particles.Particle.raceOnParameterInitializedBefore(Particle.java:92)

particles.Particle: particles.Particle.raceOnParameterInitializedBefore(Particle.java:81)
 .x
   (a)  particles.Particle$7.op(Particle.java:95)
   (b)  particles.Particle$7.op(Particle.java:95)
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

particles.Particle: particles.Particle.disambiguateFalseRace(Particle.java:186)
 .x
   (a)  particles.Particle.moveTo(Particle.java:13)
   (b)  particles.Particle.moveTo(Particle.java:13)
 .y
   (a)  particles.Particle.moveTo(Particle.java:14)
   (b)  particles.Particle.moveTo(Particle.java:14)
""")

  @Test def ignoreFalseRacesInSeqOp = expectNoRaces

  @Test def raceBecauseOfOutsideInterference = expect("""
Loop: particles.Particle.raceBecauseOfOutsideInterference(Particle.java:232)

particles.Particle: particles.Particle$15.op(Particle.java:235)
 .x
   (a)  particles.Particle$15.op(Particle.java:236)
   (b)  particles.Particle$15.op(Particle.java:236)
particles.Particle: particles.Particle.raceBecauseOfOutsideInterference(Particle.java:229)
 .origin
   (a)  particles.Particle$15.op(Particle.java:235)
   (b)  particles.Particle$15.op(Particle.java:235)
        particles.Particle$15.op(Particle.java:236)
""")

  @Test def raceOnSharedObjectCarriedByArray = expect("""
Loop: particles.Particle.raceOnSharedObjectCarriedByArray(Particle.java:259)

particles.Particle: particles.Particle$16.op(Particle.java:253)
 .x
   (a)  particles.Particle.moveTo(Particle.java:13)
   (b)  particles.Particle.moveTo(Particle.java:13)
 .y
   (a)  particles.Particle.moveTo(Particle.java:14)
   (b)  particles.Particle.moveTo(Particle.java:14)
""")

  @Test def raceBecauseOfDirectArrayLoad = expect("""
Loop: particles.Particle.raceBecauseOfDirectArrayLoad(Particle.java:274)

particles.Particle: particles.Particle$18.op(Particle.java:279)
 .x
   (a)  particles.Particle$18.op(Particle.java:278)
   (b)  particles.Particle$18.op(Particle.java:278)
particles.Particle: particles.Particle.raceBecauseOfDirectArrayLoad(Particle.java:271)
 .x
   (a)  particles.Particle$18.op(Particle.java:278)
   (b)  particles.Particle$18.op(Particle.java:278)
""")

  @Test def raceOnSharedReturnValue = expect("""
Loop: particles.Particle.raceOnSharedReturnValue(Particle.java:290)

particles.Particle: particles.Particle.raceOnSharedReturnValue(Particle.java:288)
 .x
   (a)  particles.Particle$19.op(Particle.java:293)
   (b)  particles.Particle$19.op(Particle.java:293)
""")

  @Test def raceOnDifferntArrayIteration = expect("""
Loop: particles.Particle.raceOnDifferntArrayIteration(Particle.java:317)

particles.Particle: particles.Particle$20.op(Particle.java:306)
 .x
   (a)  particles.Particle$22.op(Particle.java:320)
   (b)  particles.Particle$22.op(Particle.java:320)
""")

  @Test @Ignore def noRaceIfFlowSensitive = expectNoRaces

  @Test def raceOnDifferntArrayIterationOneLoop = expect("""
Loop: particles.Particle.raceOnDifferntArrayIterationOneLoop(Particle.java:367)

particles.Particle: particles.Particle$27.op(Particle.java:371)
 .x
   (a)  particles.Particle$27.op(Particle.java:370)
   (b)  particles.Particle$27.op(Particle.java:370)
particles.Particle: particles.Particle.raceOnDifferntArrayIterationOneLoop(Particle.java:365)
 .origin
   (a)  particles.Particle$27.op(Particle.java:371)
   (b)  particles.Particle$27.op(Particle.java:371)
        particles.Particle$27.op(Particle.java:372)
""")

  @Test def verySimpleRaceWithIndex = expect("""
Loop: particles.Particle.verySimpleRaceWithIndex(Particle.java:383)

particles.Particle: particles.Particle.verySimpleRaceWithIndex(Particle.java:381)
 .x
   (a)  particles.Particle$28.op(Particle.java:386)
   (b)  particles.Particle$28.op(Particle.java:386)
""")

  @Test def verySimpleRaceToStaticObject = expect("""
Loop: particles.Particle.verySimpleRaceToStaticObject(Particle.java:399)

particles.Particle: particles.Particle.<clinit>(Particle.java:392)
 .x
   (a)  particles.Particle$29.op(Particle.java:402)
   (b)  particles.Particle$29.op(Particle.java:402)
""")

  @Test def raceOnSharedFromStatic = expect("""
Loop: particles.Particle.raceOnSharedFromStatic(Particle.java:412)

particles.Particle: particles.Particle.<clinit>(Particle.java:392)
 .y
   (a)  particles.Particle$30.op(Particle.java:416)
   (b)  particles.Particle$30.op(Particle.java:416)
""")

  @Test def staticMethod = expect("""
Loop: particles.Particle.staticMethod(Particle.java:613)

particles.Particle: particles.Particle.<clinit>(Particle.java:392)
 .forceX
   (a)  particles.Particle.thisisstatic(Particle.java:623)
   (b)  particles.Particle.thisisstatic(Particle.java:623) [2x]
""");

  @Test def verySimpleRaceOnStaticField = expect("""
Loop: particles.Particle.verySimpleRaceOnStaticField(Particle.java:632)

Static: particles.Particle
 .staticX
   (a)  particles.Particle$46.op(Particle.java:635)
   (b)  particles.Particle$46.op(Particle.java:635)
""")

  @Test def raceOnArray = expect("""
Loop: particles.Particle.raceOnArray(Particle.java:462)

particles.Particle: particles.Particle.raceOnArray(Particle.java:460)
 .[*]
   (a)  particles.Particle$33.op(Particle.java:465)
   (b)  particles.Particle$33.op(Particle.java:465)
""")

  @Test def noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape = expect("""
Loop: particles.Particle.noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape(Particle.java:445)

particles.Particle: particles.Particle.noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape(Particle.java:443)
 .origin
   (a)  particles.Particle$32.op(Particle.java:449)
   (b)  particles.Particle$32.op(Particle.java:449)
""")

  //  @Test def example = expect("")
  /*
  @Test def raceInLibrary = expect("""
  ....
""")*/
}