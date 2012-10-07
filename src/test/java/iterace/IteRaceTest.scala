package iterace
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TestName
import scala.reflect.BeanProperty
import util._
import iterace.stage.StageConstructor
import iterace.evaluation.SubjectScope
import sppa.util.debug
import sppa.util.JavaTest
import wala.AnalysisOptions

abstract class IteRaceTest extends JavaTest with SubjectScope {
  def analyze(entryClass: String, entryMethod: String, options: Set[IteRaceOption]) = {
    debug("test: " + entryMethod)
    IteRace(AnalysisOptions(
      entrypoints = Seq((entryClass, entryMethod)),
      dependencies = this.dependencies),
      options)
  }
}