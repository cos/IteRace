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

abstract class RaceTest(dependencies: List[String], startClass: String) extends FunSuite with BeforeAndAfter  {
  def analyze(method: String) = {
    new IteRace(startClass, method,  dependencies)
  }

  def printRaces(races: Map[Loop, Map[O, Map[F, RSet]]]): String = {
    val s = new StringBuilder
    s ++= "\n"
    
    for ((l, lr) <- races.toStringSorted ) {
      s ++= "Loop: "+l.n.getContext().asInstanceOf[LoopCallSiteContext].prettyPrint() + "\n\n"
      for ((o, fr) <- lr.toStringSorted ) {
        s ++= o.prettyPrint() + "\n"
        for ((f, rr) <- fr.toStringSorted ) {
          s ++= " ." + f.getName() + "\n"
          s ++= rr.prettyPrint() +"\n"
        }
      }
    }
    s.toString()
  }
  
  def testResult(method: String, result: String) = {
    test(method) {
      val iterace = analyze(method+"()V")
      assertEquals(result, printRaces(iterace.races.asInstanceOf[Map[Loop, Map[O, Map[F, RSet]]]]))
    }
  }
  
  def testNoRaces(method: String) = testResult(method, "\n")
}