package iterace
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestNow extends RaceTest(List("particles", "../lib/parallelArray.mock"), "Lparticles/Particle") {
  override def result(iteRace: IteRace) = iteRace.possibleRaces
  
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
        java.util.HashMap.put(HashMap.java:401)
        java.util.HashMap.resize(HashMap.java:503)
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