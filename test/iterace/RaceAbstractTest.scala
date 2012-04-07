package iterace
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

abstract class RaceAbstractTest(startClass: String) extends FunSuite with BeforeAndAfter {
  val analysisScope = new AnalysisScopeBuilder("/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar");
  analysisScope.setExclusionsFile("walaExclusions.txt");
  analysisScope.addBinaryDependency("../lib/parallelArray.mock");

  val stages: Seq[StageConstructor] = Seq(FilterByLockMayAlias, BubbleUp, FilterByLockMayAlias);

  var focusOnMethod: Option[String] = None
  def focus(method: String) = focusOnMethod = Some(method)

  def analyze(method: String) = {
    log("test: " + method)
    IteRace(startClass, method, analysisScope, stages)
  }

  def printRaces(races: ProgramRaceSet): String = "\n" + races.prettyPrint + "\n"

  //    for ((l, lRaces) <- races.groupBy { _.l } toStringSorted) {
  //      s ++= "Loop: " + l.n.getContext().asInstanceOf[LoopCallSiteContext].prettyPrint() + "\n\n"
  //      for ((o, oRaces) <- lRaces.groupBy { _.o } toStringSorted) {
  //        s ++= o.prettyPrint() + "\n"
  //        val racesOnFields = oRaces collect { case r: RaceOnField => r }
  //        for ((f, fRaces) <- racesOnFields.groupBy { _.f } toStringSorted) {
  //          s ++= ((new FieldRaceSet(f, fRaces)).prettyPrint) + "\n"
  //        }
  //
  //        val shallowRaces = oRaces collect {case r: ShallowRace => r}
  //        if(!shallowRaces.isEmpty)
  //        s ++= " on object: \n" + new ShallowRaceSet(shallowRaces).prettyPrint() + "\n"
  //        
  //      }
  //    }

  def result(iteRace: IteRace): ProgramRaceSet = iteRace.races

  def testResult(method: String, expectedResult: String): Unit = {
    focusOnMethod match {
      case Some(m) if m != method => // do nothing
      case _ => test(method) {
        val iterace = analyze(method + "()V")
        assertEquals(expectedResult, printRaces(result(iterace)))
      }
    }
  }

  def testNoRaces(method: String): Unit = testResult(method, "\n\n")
}