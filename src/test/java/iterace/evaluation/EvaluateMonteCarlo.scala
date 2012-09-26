package iterace.evaluation;

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import wala.WALAConversions._
import org.junit.Assert._
import scala.collection._
import org.scalatest.FunSuite
import org.junit.Rule
import iterace.IteRace

import org.junit.Test

class EvaluateMonteCarlo extends Evaluate with MonteCarloScope {

  expect("""
Loop: montecarlo.parallel.AppDemo.runParallel(AppDemo.java:178)

Static: montecarlo.parallel.Universal
 .UNIVERSAL_DEBUG
   (a)  montecarlo.parallel.Universal.<init>(Universal.java:63)
   (b)  montecarlo.parallel.Universal.<init>(Universal.java:63)
""")
}