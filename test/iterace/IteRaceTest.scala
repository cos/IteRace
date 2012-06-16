package iterace
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TestName
import scala.reflect.BeanProperty
import iterace.pointeranalysis.AnalysisScopeBuilder
import iterace.util.log
import iterace.stage.StageConstructor
import iterace.util.debug
import iterace.evaluation.SubjectScope

abstract class IteRaceTest extends JavaTest with SubjectScope {

  analysisScope.addBinaryDependency("../lib/parallelArray.mock")

  def analyze(entryClass: String, entryMethod: String, options: Set[IteRaceOption]) = {
    debug("test: " + entryMethod)
    IteRace(entryClass, entryMethod, analysisScope, options)
  }
}