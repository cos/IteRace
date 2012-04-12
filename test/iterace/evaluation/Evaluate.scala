package iterace.evaluation
import iterace.stage.RaceAbstractTest
import org.junit.Test
import org.junit.Assert._
import iterace.IteRaceOption._
import iterace.IteRaceOption
import iterace.IteRaceTest
import iterace.IteRaceOptions
import iterace.IteRaceOption._
import iterace.IteRace
import scala.io._
import java.io.FileWriter
import iterace.util.log

import sjson.json._
import DefaultProtocol._
import JsonSerialization._

abstract class Evaluate(
  entryClass: String,
  entryMethod: String = "main([Ljava/lang/String;)V") extends IteRaceTest {

  var options: Set[IteRaceOption] = IteRaceOptions(TwoThreadModel, DeepSynchronized, BubbleUp, AppLevelSynchronized)

  def analyze(options: Set[IteRaceOption]): IteRace = analyze(entryClass, entryMethod, options)

  def result: String = analyze(entryClass, entryMethod, options).races.prettyPrint

  def expectNoRaces = toRun = () => { assertEquals("", result) }
  def expectSomeRaces = toRun = () => { assertNotSame("", result) }
  def expect(printedRaces: String) = toRun = () => { assertEquals(printedRaces, result) }

  var toRun: () => Unit = expectNoRaces _

  @Test def t = toRun()
}

object Evaluate extends App {
  val subjectMap = Map(
    "BH" -> new EvaluateBH,
    "EM3D" -> new EvaluateEM3D,
    "jUnit" -> new EvaluatejUnit,
    "LuSearch" -> new EvaluateLuSearch,
    "MonteCarlo" -> new EvaluateMonteCarlo,
    "Coref" -> new EvaluateOldCoref,
    "WEKA" -> new EvaluateWEKA)

  val arguments = List.fromArray(args)

  val subjectName = arguments.head
  val subject = subjectMap(subjectName)
  val optionNames = arguments.tail
  val options = IteRaceOptions(optionNames map { IteRaceOption(_) })

  println(subject)
  println(options)

  val iteRace = subject.analyze(options)
  
  val fw = new FileWriter("evaluation/"+subjectName+"_"+optionNames.mkString("-")+".json")
  
  println(log.entries.toMap)
  fw.write("\n\n"+tojson(log.entries.toMap)) ; fw.close()
}

object Bla extends App {
  println("bl1a")
}

object EvaluateAll extends App {
  import scala.sys.process._

  println("start")

  args.foreach(subject => {

    val cmd = Seq("env", "JAVA_OPTS=-Xmx2g",
      "scala", "-cp", classPath, "iterace.evaluation.Evaluate", subject, "2-threads-model")
    
    val countLogger = ProcessLogger(line => println("normal: "+line), line => println("err: " + line))
    
    println(cmd run(countLogger, false))
  })

  def classPath = {
    val eclipseRuntime = "/Applications/eclipse/plugins/org.eclipse.core.runtime_3.7.0.v20110110.jar"
    val eclipseEquinox = "/Applications/eclipse/plugins/org.eclipse.equinox.common_3.6.0.v20110523.jar"
    val eclipseOsgi = "/Applications/eclipse/plugins/org.eclipse.osgi_3.7.2.v20120110-1415.jar"

    "bin:lib/*.jar:lib/org.junit.jar:../wala/com.ibm.wala.util/bin:../wala/com.ibm.wala.shrike/bin:../wala/com.ibm.wala.core/bin:../lib/parallelArray.mock/bin" +
      ":" + eclipseRuntime +
      ":" + eclipseEquinox +
      ":" + eclipseOsgi
  }
}

  //  val uglyClassPath = "/Applications/eclipse/plugins/*.jar:/Applications/eclipse/plugins/org.eclipse.core.runtime_3.7.0.v20110110.jar:/Applications/eclipse/plugins/org.eclipse.osgi_3.7.2.v20120110-1415.jar:/Applications/eclipse/plugins/org.eclipse.equinox.weaving.hook_1.0.0.v20100503.jar:/Applications/eclipse/plugins/org.eclipse.equinox.common_3.6.0.v20110523.jar:/Applications/eclipse/plugins/org.eclipse.core.jobs_3.5.101.v20120113-1953.jar:/Applications/eclipse/plugins/org.eclipse.core.runtime.compatibility.registry_3.5.0.v20110505/runtime_registry_compatibility.jar:/Applications/eclipse/plugins/org.eclipse.equinox.registry_3.5.101.R37x_v20110810-1611.jar:/Applications/eclipse/plugins/org.eclipse.equinox.preferences_3.4.2.v20120111-2020.jar:/Applications/eclipse/plugins/org.eclipse.core.contenttype_3.4.100.v20110423-0524.jar:/Applications/eclipse/plugins/org.eclipse.equinox.app_1.3.100.v20110321.jar:/Applications/eclipse/plugins/org.eclipse.core.resources_3.7.101.v20120125-1505.jar"