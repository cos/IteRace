package iterace;

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import iterace.conversions._
import org.junit.Assert._
import iterace.LoopContextSelector.LoopCallSiteContext
import scala.collection._
import org.scalatest.FunSuite
import org.junit.Rule

@RunWith(classOf[JUnitRunner])
class TestParticle extends RaceTest(List("particles", "../lib/parallelArray.mock"), "Lparticles/Particle") {
  
  testNoRaces("vacuouslyNoRace")
  
  testNoRaces("noRaceOnParameter")
  
  testNoRaces("noRaceOnParameterInitializedBefore")
  
  testResult("verySimpleRace", 
      """
Loop: particles.Particle.verySimpleRace(Particle.java:68)

particles.Particle.verySimpleRace(Particle.java:66)
 .x
   (a)  particles.Particle$5.op(Particle$5.java:71)
   (b)  particles.Particle$5.op(Particle$5.java:71)
""")  

	testResult("raceOnParameterInitializedBefore", 
	    """
Loop: particles.Particle.raceOnParameterInitializedBefore(Particle.java:92)

particles.Particle.raceOnParameterInitializedBefore(Particle.java:81)
 .x
   (a)  particles.Particle$7.op(Particle$7.java:95)
   (b)  particles.Particle$7.op(Particle$7.java:95)
""")

	testNoRaces("noRaceOnANonSharedField")

	// but we actually solve it from the loop context
	testNoRaces("oneCFANeededForNoRaces")

	// but we actually solve it from the loop context
	testNoRaces("twoCFANeededForNoRaces")

	testNoRaces("recursive")
	
	testResult("disambiguateFalseRace", """
Loop: particles.Particle.disambiguateFalseRace(Particle.java:189)

particles.Particle.disambiguateFalseRace(Particle.java:186)
 .y
   (a)  particles.Particle.moveTo(Particle.java:17)
   (b)  particles.Particle.moveTo(Particle.java:17)
 .x
   (a)  particles.Particle.moveTo(Particle.java:16)
   (b)  particles.Particle.moveTo(Particle.java:16)
""")

	testNoRaces("ignoreFalseRacesInSeqOp")
	
	testResult("raceBecauseOfOutsideInterference","""
Loop: particles.Particle.raceBecauseOfOutsideInterference(Particle.java:232)

particles.Particle.raceBecauseOfOutsideInterference(Particle.java:229)
 .origin
   (a)  particles.Particle$15.op(Particle$15.java:235) [2]
   (b)  particles.Particle$15.op(Particle$15.java:235)
        particles.Particle$15.op(Particle$15.java:236)
particles.Particle$15.op(Particle$15.java:235)
 .x
   (a)  particles.Particle$15.op(Particle$15.java:236)
   (b)  particles.Particle$15.op(Particle$15.java:236)
""")

	testResult("raceOnSharedObjectCarriedByArray","""
Loop: particles.Particle.raceOnSharedObjectCarriedByArray(Particle.java:259)

particles.Particle$16.op(Particle$16.java:253)
 .y
   (a)  particles.Particle.moveTo(Particle.java:17)
   (b)  particles.Particle.moveTo(Particle.java:17)
 .x
   (a)  particles.Particle.moveTo(Particle.java:16)
   (b)  particles.Particle.moveTo(Particle.java:16)
""")

testResult("raceBecauseOfDirectArrayLoad","""
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

testResult("raceOnSharedReturnValue", """
Loop: particles.Particle.raceOnSharedReturnValue(Particle.java:290)

particles.Particle.raceOnSharedReturnValue(Particle.java:288)
 .x
   (a)  particles.Particle$19.op(Particle$19.java:293)
   (b)  particles.Particle$19.op(Particle$19.java:293)
""")

testResult("raceOnDifferntArrayIteration", """
Loop: particles.Particle.raceOnDifferntArrayIteration(Particle.java:317)

particles.Particle$20.op(Particle$20.java:306)
 .x
   (a)  particles.Particle$22.op(Particle$22.java:320)
   (b)  particles.Particle$22.op(Particle$22.java:320)
""")

ignore("noRaceIfFlowSensitive") { } // should return no races

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

  testResult("verySimpleRaceWithIndex","""
Loop: particles.Particle.verySimpleRaceWithIndex(Particle.java:383)

particles.Particle.verySimpleRaceWithIndex(Particle.java:381)
 .x
   (a)  particles.Particle$28.op(Particle$28.java:386)
   (b)  particles.Particle$28.op(Particle$28.java:386)
""")

  testResult("verySimpleRaceToStatic","""
Loop: particles.Particle.verySimpleRaceToStatic(Particle.java:399)

particles.Particle.<clinit>(Particle.java:392)
 .x
   (a)  particles.Particle$29.op(Particle$29.java:402)
   (b)  particles.Particle$29.op(Particle$29.java:402)
""")

  testResult("raceOnSharedFromStatic","""
Loop: particles.Particle.raceOnSharedFromStatic(Particle.java:412)

particles.Particle.<clinit>(Particle.java:392)
 .y
   (a)  particles.Particle$30.op(Particle$30.java:416)
   (b)  particles.Particle$30.op(Particle$30.java:416)
""")

  testResult("raceInLibrary","""
Loop: particles.Particle.raceInLibrary(Particle.java:428)

java.util.HashMap.createEntry(HashMap.java:823)
 .next
   (a)  java.util.HashMap.transfer(HashMap.java:524) [4]
   (b)  java.util.HashMap.transfer(HashMap.java:522)
        java.util.HashMap.put(HashMap.java:405)
        java.util.HashMap.transfer(HashMap.java:524)
        java.util.HashMap.putForNullKey(HashMap.java:424)
 .value
   (a)  java.util.HashMap.putForNullKey(HashMap.java:427) [4]
        java.util.HashMap.put(HashMap.java:409) [4]
   (b)  java.util.HashMap.putForNullKey(HashMap.java:427) [2]
        java.util.HashMap.putForNullKey(HashMap.java:426) [2]
        java.util.HashMap.put(HashMap.java:409) [2]
        java.util.HashMap.put(HashMap.java:408) [2]
java.util.HashMap.resize(HashMap.java:502)
 .cache
   (a)  java.util.HashMap$FrontCache.<init>(HashMap$FrontCache.java:1063) [3]
   (b)  java.util.HashMap$FrontCache.transfer(HashMap$FrontCache.java:1145) [2]
        java.util.HashMap$FrontCache.put(HashMap$FrontCache.java:1113)
 .bitMask
   (a)  java.util.HashMap$FrontCache.<init>(HashMap$FrontCache.java:1064)
   (b)  java.util.HashMap$FrontCache.inRange(HashMap$FrontCache.java:1090)
java.util.HashMap.addEntry(HashMap.java:808)
 .hash
   (a)  java.util.HashMap$Entry.<init>(HashMap$Entry.java:739) [2]
   (b)  java.util.HashMap.transfer(HashMap.java:523)
        java.util.HashMap.put(HashMap.java:407)
 .next
   (a)  java.util.HashMap$Entry.<init>(HashMap$Entry.java:737) [4]
        java.util.HashMap.transfer(HashMap.java:524) [4]
   (b)  java.util.HashMap.transfer(HashMap.java:522) [2]
        java.util.HashMap.put(HashMap.java:405) [2]
        java.util.HashMap.transfer(HashMap.java:524) [2]
        java.util.HashMap.putForNullKey(HashMap.java:424) [2]
 .key
   (a)  java.util.HashMap$Entry.<init>(HashMap$Entry.java:738) [2]
   (b)  java.util.HashMap.putForNullKey(HashMap.java:425)
        java.util.HashMap.put(HashMap.java:407)
 .value
   (a)  java.util.HashMap.putForNullKey(HashMap.java:427) [4]
        java.util.HashMap.put(HashMap.java:409) [4]
        java.util.HashMap$Entry.<init>(HashMap$Entry.java:736) [4]
   (b)  java.util.HashMap.putForNullKey(HashMap.java:427) [3]
        java.util.HashMap.putForNullKey(HashMap.java:426) [3]
        java.util.HashMap.put(HashMap.java:409) [3]
        java.util.HashMap.put(HashMap.java:408) [3]
java.util.HashSet.<init>(HashSet.java:86)
 .size
   (a)  java.util.HashMap.addEntry(HashMap.java:809) [3]
   (b)  java.util.HashMap.size(HashMap.java:288)
        java.util.HashMap.addEntry(HashMap.java:809) [2]
 .threshold
   (a)  java.util.HashMap.resize(HashMap.java:492) [3]
        java.util.HashMap.resize(HashMap.java:508) [3]
   (b)  java.util.HashMap.resize(HashMap.java:492) [2]
        java.util.HashMap.addEntry(HashMap.java:809) [2]
        java.util.HashMap.resize(HashMap.java:508) [2]
 .modCount
   (a)  java.util.HashMap.put(HashMap.java:415) [4]
        java.util.HashMap.putForNullKey(HashMap.java:432) [4]
   (b)  java.util.HashMap.put(HashMap.java:415) [4]
        java.util.HashMap.putForNullKey(HashMap.java:432) [4]
 .table
   (a)  java.util.HashMap.resize(HashMap.java:507) [9]
   (b)  java.util.HashMap.transfer(HashMap.java:515)
        java.util.HashMap.addEntry(HashMap.java:808)
        java.util.HashMap.resize(HashMap.java:507)
        java.util.HashMap.putForNullKey(HashMap.java:424)
        java.util.HashMap.addEntry(HashMap.java:807)
        java.util.HashMap.addEntry(HashMap.java:810)
        java.util.HashMap.resize(HashMap.java:489)
        java.util.HashMap.put(HashMap.java:404)
        java.util.HashMap.put(HashMap.java:405)
 .frontCache
   (a)  java.util.HashMap.resize(HashMap.java:504) [3]
   (b)  java.util.HashMap.resize(HashMap.java:504)
        java.util.HashMap.resize(HashMap.java:503)
        java.util.HashMap.put(HashMap.java:401)
java.util.HashMap.addEntry(HashMap.java:808)
 .next
   (a)  java.util.HashMap.transfer(HashMap.java:524) [4]
   (b)  java.util.HashMap.transfer(HashMap.java:522)
        java.util.HashMap.put(HashMap.java:405)
        java.util.HashMap.transfer(HashMap.java:524)
        java.util.HashMap.putForNullKey(HashMap.java:424)
 .value
   (a)  java.util.HashMap.putForNullKey(HashMap.java:427) [4]
        java.util.HashMap.put(HashMap.java:409) [4]
   (b)  java.util.HashMap.putForNullKey(HashMap.java:427) [2]
        java.util.HashMap.putForNullKey(HashMap.java:426) [2]
        java.util.HashMap.put(HashMap.java:409) [2]
        java.util.HashMap.put(HashMap.java:408) [2]
""")
}