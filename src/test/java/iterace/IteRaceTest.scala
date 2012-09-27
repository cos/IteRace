package iterace
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TestName
import scala.reflect.BeanProperty
import wala.AnalysisScopeBuilder
import util._
import iterace.stage.StageConstructor
import iterace.evaluation.SubjectScope
import sppa.util.debug
import sppa.util.JavaTest

abstract class IteRaceTest extends JavaTest with SubjectScope {

  analysisScope.addBinaryDependency("../lib/parallelArray.mock")

  def analyze(entryClass: String, entryMethod: String, options: Set[IteRaceOption]) = {
    debug("test: " + entryMethod)
    IteRace(entryClass, entryMethod, analysisScope, options)
  }
}