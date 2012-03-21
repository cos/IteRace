package iterace
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import iterace.util.WALAConversions._
import iterace.LoopContextSelector.LoopCallSiteContext
import org.junit.Assert._
import scala.collection._
import scala.collection.immutable.TreeSet
import scala.collection.immutable.TreeMap
import iterace.util._
import scala.collection.JavaConversions._
import com.ibm.wala.properties.WalaProperties
import iterace.oldjava.AnalysisScopeBuilder

abstract class RaceTest(dependencies: List[String], startClass: String) extends FunSuite with BeforeAndAfter  {
  def analyze(method: String) = {
    var analysisScope = new AnalysisScopeBuilder("/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar");
    analysisScope.setExclusionsFile("walaExclusions.txt");
    for (d <- dependencies) { analysisScope.addBinaryDependency(d); }
    
    new IteRace(startClass, method,  analysisScope)
  }
  
  def printRaces(races: Set[Race]): String = {
    val s = new StringBuilder
    s ++= "\n"
    
    for ((l, lr) <- races.groupBy {_.l} toStringSorted ) {
      s ++= "Loop: "+l.n.getContext().asInstanceOf[LoopCallSiteContext].prettyPrint() + "\n\n"
      for ((o, fr) <- races.groupBy {_.o} toStringSorted ) {
        s ++= o.prettyPrint() + "\n"
        for ((f, rr) <- races.groupBy {_.f} toStringSorted ) {
          s ++= ((new FieldRaceSet(f, rr)).prettyPrint ) + "\n"
        }
      }
    }
    s.toString()
  }
  
  def result(iteRace: IteRace):Set[Race]
  
  def testResult(method: String, expectedResult: String) = {
    test(method) {
      val iterace = analyze(method+"()V")
      assertEquals(expectedResult, printRaces(result(iterace)))
    }
  }
  
  def testNoRaces(method: String) = testResult(method, "\n")
}