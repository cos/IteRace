package iterace
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TestName
import scala.reflect.BeanProperty
import iterace.pointeranalysis.AnalysisScopeBuilder
import iterace.util.log
import iterace.stage.StageConstructor

class IteRaceTest extends JavaTest {
  val analysisScope = new AnalysisScopeBuilder("/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar")
  analysisScope.setExclusionsFile("walaExclusions.txt")
  analysisScope.addBinaryDependency("../lib/parallelArray.mock")
  
  def analyze(entryClass: String, entryMethod: String, stages: Seq[StageConstructor]) = {
    log("test: " + entryMethod)
    IteRace(entryClass, entryMethod, analysisScope, stages)
  }
}