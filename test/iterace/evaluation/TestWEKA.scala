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
import iterace.RaceAbstractTest
import iterace.IteRace
import iterace.util.log

@RunWith(classOf[JUnitRunner])
class TestWEKA extends RaceAbstractTest("Lweka/clusterers/EM") {

  analysisScope.addJarDependency("../evaluation/weka/lib/java-cup.jar");
  analysisScope.addJarDependency("../evaluation/weka/lib/JFlex.jar");
  analysisScope.addJarDependency("../evaluation/weka/lib/junit.jar");
  analysisScope.addBinaryDependency("../evaluation/weka/bin");

  log.activeConsole = true
  log.activeTimer = true

  testResult("EM_Init(Lweka/core/Instances;)V", """

""")
}