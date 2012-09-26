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
import iterace.IteRaceOptions
import iterace.stage.FilterByLockMayAlias
import util.debug


class EvaluateBH extends Evaluate with BHScope {
  debug.activate  
  expectNoRaces
}