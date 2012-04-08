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


class EvaluateMonteCarlo extends RaceAbstractTest("Lmontecarlo/parallel/JGFMonteCarloBench") {

  analysisScope.addBinaryDependency("../evaluation/montecarlo/bin");

  override def result(iteRace: IteRace) = iteRace.races

  log.activate

  @Test def bla = expect("JGFrun(I)V","""
Loop: montecarlo.parallel.AppDemo.runParallel(AppDemo.java:178)

Static: montecarlo.parallel.Universal
 .UNIVERSAL_DEBUG
   (a)  montecarlo.parallel.Universal.<init>(Universal.java:63)
   (b)  montecarlo.parallel.Universal.<init>(Universal.java:63)
""")
}