package iterace.stage

import org.junit.Test
import iterace.util.log
import iterace.IteRaceTest
import iterace.datastructure.ProgramRaceSet
import org.junit.Assert._
import iterace.IteRace
import iterace.IteRaceOption

abstract class RaceAbstractTest(startClass: String) extends IteRaceTest {
  
  val options: Set[IteRaceOption] = Set()
  
  def analyze(method: String) = super.analyze(startClass, method, options)
  
  def printRaces(races: ProgramRaceSet): String = "\n" + races.prettyPrint + "\n"

  def result(iteRace: IteRace): ProgramRaceSet = iteRace.races

  def expect(entry: String, expectedResult: String) = assertEquals(expectedResult, printRaces(result(analyze(entry + "()V"))))
  def expect(expectedResult: String): Unit = expect(testName.getMethodName() + "()V", expectedResult)
  def expectNoRaces:Unit = expectNoRaces(testName.getMethodName() + "()V")
  def expectNoRaces(entry:String) = expect(entry, "\n\n")
  def expectSomeRaces = assertNotSame("\n\n",printRaces(result(analyze(testName.getMethodName() + "()V"))))
}