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
import iterace.IteRaceOption


class EvaluateBH extends Evaluate("LbarnesHut/ParallelBarneshut") {
  
  log.activate

  analysisScope.addBinaryDependency("../evaluation/barnesHut/bin");

  @Test def t = expectNoRaces("main([Ljava/lang/String;)V")
}