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
import org.junit.Test


class EvaluateLuSearch extends RaceAbstractTest("Lorg/dacapo/lusearch/Search") {

  analysisScope.addBinaryDependency("../evaluation/lusearch/bin");
	analysisScope.addBinaryDependency("../lib/parallelArray.mock");

  log.activate

  @Test def t = expectNoRaces("main([Ljava/lang/String;)V")
}