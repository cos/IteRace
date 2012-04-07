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


class EvaluateEM3D extends RaceAbstractTest("Lem3d/parallelArray/Em3d") {

  log.activate

  analysisScope.addBinaryDependency("../evaluation/em3d/bin");

  @Test def t = expectNoRaces("main([Ljava/lang/String;)V")
}