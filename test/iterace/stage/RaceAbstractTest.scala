package iterace.stage
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import iterace.util.WALAConversions._
import org.junit.Assert._
import scala.collection._
import scala.collection.immutable.TreeSet
import scala.collection.immutable.TreeMap
import iterace.util._
import scala.collection.JavaConversions._
import com.ibm.wala.properties.WalaProperties
import iterace.pointeranalysis.AnalysisScopeBuilder
import iterace.IteRace
import iterace.datastructure.ProgramRaceSet
import iterace.JavaTest
import iterace.IteRaceTest

abstract class RaceAbstractTest(startClass: String) extends IteRaceTest {
  val stages: Seq[StageConstructor] = Seq(FilterByLockMayAlias, BubbleUp, FilterByLockMayAlias);

  def analyze(method: String) = super.analyze(startClass, method, stages)
  
  def printRaces(races: ProgramRaceSet): String = "\n" + races.prettyPrint + "\n"

  def result(iteRace: IteRace): ProgramRaceSet = iteRace.races

  def expect(entry: String, expectedResult: String) = assertEquals(expectedResult, printRaces(result(analyze(entry + "()V"))))
  def expect(expectedResult: String): Unit = expect(testName.getMethodName() + "()V", expectedResult)
  def expectNoRaces:Unit = expectNoRaces(testName.getMethodName() + "()V")
  def expectNoRaces(entry:String) = expect(entry, "\n\n")
}