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

import org.junit.Test


class EvaluatejUnit extends Evaluate("Ljunit/tests/ParallelAllTests") {

  log.activate

  analysisScope.addBinaryDependency("../evaluation/junit/bin");

  @Test def t = expectNoRaces("main([Ljava/lang/String;)V")
}