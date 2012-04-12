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
import iterace.IteRaceOption

import org.junit.Test


class EvaluateEM3D extends Evaluate("Lem3d/parallelArray/Em3d") {

  analysisScope.addBinaryDependency("../evaluation/em3d/bin");
  
  expectNoRaces
}