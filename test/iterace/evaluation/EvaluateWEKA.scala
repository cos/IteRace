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


class EvaluateWEKA extends Evaluate("Lweka/clusterers/EM") {

  analysisScope.addJarDependency("../evaluation/weka/lib/java-cup.jar");
  analysisScope.addJarDependency("../evaluation/weka/lib/JFlex.jar");
  analysisScope.addJarDependency("../evaluation/weka/lib/junit.jar");
  analysisScope.addBinaryDependency("../evaluation/weka/bin");

  expectNoRaces
}