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
import org.junit.Test
import iterace.util.log
import iterace.util.debug
import iterace.IteRaceOption


class EvaluateOldCoref extends Evaluate("LLBJ2/nlp/coref/ClusterMerger") with OldCorefScope {


  
  
  
  expectNoRaces
}