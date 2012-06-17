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
import iterace.util.debug
import scala.actors.Futures._
import java.util.Timer
import java.util.TimerTask
import scala.collection.JavaConverters._
import java.lang.management.ManagementFactory
import java.io.File

abstract class Evaluate extends IteRaceTest {

  debug.activate
  debug(this.getClass().toString())

  var options: Set[IteRaceOption] = IteRaceOptions.all

  def analyze(options: Set[IteRaceOption]): IteRace = analyze(entryClass, entryMethod, options)

  def result: String = analyze(entryClass, entryMethod, options).races.prettyPrint

  def expectNoRaces { toRun = () => { assertEquals("", result) } }
  def expectSomeRaces { toRun = () => { assertNotSame("", result) } }
  def expect(printedRaces: String) { toRun = () => { assertEquals(printedRaces, "\n" + result + "\n") } }

  var toRun: () => Unit = expectNoRaces _

  @Test def t { toRun() }
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

  val pidWrite = new FileWriter("theprocessid.txt")
  println(ManagementFactory.getRuntimeMXBean().getName())
  pidWrite.write(ManagementFactory.getRuntimeMXBean().getName().split('@').head)
  pidWrite.close()

  val iteRace = subject.analyze(options)

  val fw = new FileWriter(EvalUtil.fileName(subjectName, optionNames) + ".json")

  println(log.entries.toMap)
  fw.write("" + tojson(log.entries.toMap)); fw.close()
}

object EvalUtil {
  def fileName(subjectName: String, optionNames: List[String]) =
    "evaluation/" + subjectName + "/" + optionNames.mkString("_")
}

object Bla extends App {
  println(ManagementFactory.getRuntimeMXBean().getName())
  println(System.getProperty("os.name"))
}

object EvaluateAll extends App {
  import scala.sys.process._

  println("start")

  args.foreach(subject => {

    var count = 0
    for (currentOptions <- IteRaceOptions.powerset(IteRaceOptions.allAsString)) {
      //      val currentOptions = IteRaceOptions.allAsString
      count += 1
      println(subject + " " + count + ": " + currentOptions.toList.sorted.mkString(" , "))

      val cmd = Seq("env", "JAVA_OPTS=-Xmx2g",
        "scala", "-cp", classPath, "iterace.evaluation.Evaluate", subject) ++ currentOptions

      val fileName = EvalUtil.fileName(subject, currentOptions.toList.sorted)

      val logFile = new FileWriter(fileName + ".txt")
      val errorFile = new FileWriter(fileName + ".error.txt")
      val logger = ProcessLogger(
        line => logFile.write(line + "\n"),
        line => errorFile.write(line + "\n"))
      val f = future {
        cmd ! logger
      }
      awaitAll(600000, f)
      if (!f.isSet) {
        println(" => TIMEOUT")
        ("kill -9 " + pid).!
        logFile.write("\nTIMEOUT")
      } else if (f.apply() != 1)
        println(" => OK")
      else
        println(" => ERROR")

      logFile.close(); errorFile.close();

      Thread.sleep(2000)
    }
  })

  def pid = {
    val source = scala.io.Source.fromFile("theprocessid.txt")
    val pid = source.mkString.toLong
    source.close()
    pid
  }

  def classPath = {
    val eclipseRuntime = "lib/org.eclipse.core.runtime.jar" // "/Applications/eclipse/plugins/org.eclipse.core.runtime_3.7.0.v20110110.jar"
    val eclipseEquinox = "lib/org.eclipse.equinox.common.jar" //"/Applications/eclipse/plugins/org.eclipse.equinox.common_3.6.0.v20110523.jar"
    val eclipseOsgi = "lib/org.eclipse.osgi.jar" //"/Applications/eclipse/plugins/org.eclipse.osgi_3.7.2.v20120110-1415.jar"

    "bin:lib/*.jar:lib/org.junit.jar:../wala/com.ibm.wala.util/bin:../wala/com.ibm.wala.shrike/bin:../wala/com.ibm.wala.core/bin:../lib/parallelArray.mock/bin" +
      ":" + eclipseRuntime +
      ":" + eclipseEquinox +
      ":" + eclipseOsgi +
      ":lib/sjson.jar" +
      ":lib/dispatch-json_2.9.1-0.8.8.jar"

  }
}
  //  val uglyClassPath = "/Applications/eclipse/plugins/*.jar:/Applications/eclipse/plugins/org.eclipse.core.runtime_3.7.0.v20110110.jar:/Applications/eclipse/plugins/org.eclipse.osgi_3.7.2.v20120110-1415.jar:/Applications/eclipse/plugins/org.eclipse.equinox.weaving.hook_1.0.0.v20100503.jar:/Applications/eclipse/plugins/org.eclipse.equinox.common_3.6.0.v20110523.jar:/Applications/eclipse/plugins/org.eclipse.core.jobs_3.5.101.v20120113-1953.jar:/Applications/eclipse/plugins/org.eclipse.core.runtime.compatibility.registry_3.5.0.v20110505/runtime_registry_compatibility.jar:/Applications/eclipse/plugins/org.eclipse.equinox.registry_3.5.101.R37x_v20110810-1611.jar:/Applications/eclipse/plugins/org.eclipse.equinox.preferences_3.4.2.v20120111-2020.jar:/Applications/eclipse/plugins/org.eclipse.core.contenttype_3.4.100.v20110423-0524.jar:/Applications/eclipse/plugins/org.eclipse.equinox.app_1.3.100.v20110321.jar:/Applications/eclipse/plugins/org.eclipse.core.resources_3.7.101.v20120125-1505.jar"