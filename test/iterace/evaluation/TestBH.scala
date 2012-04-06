package iterace.evaluation;

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import iterace.util.WALAConversions._
import org.junit.Assert._
import scala.collection._
import org.scalatest.FunSuite
import org.junit.Rule
import iterace.RaceTest
import iterace.IteRace

@RunWith(classOf[JUnitRunner])
class TestBH extends RaceTest("LbarnesHut/ParallelBarneshut") {
  
  analysisScope.addBinaryDependency("../evaluation/barnesHut/bin");
  
  override def result(iteRace: IteRace) = iteRace.possibleRaces
  
  testNoRaces("main([Ljava/lang/String;)V")
}