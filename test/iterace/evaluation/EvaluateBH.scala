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
import iterace.IteRace
import iterace.util.log
import iterace.stage.RaceAbstractTest

@RunWith(classOf[JUnitRunner])
class EvaluateBH extends RaceAbstractTest("LbarnesHut/ParallelBarneshut") {

  log.activate

  analysisScope.addBinaryDependency("../evaluation/barnesHut/bin");

  testNoRaces("main([Ljava/lang/String;)V")
}