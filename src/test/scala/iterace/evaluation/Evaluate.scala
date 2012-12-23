package iterace.evaluation
import iterace.stage.RaceAbstractTest
import org.junit.Test
import org.junit.Assert._
import iterace.IteRaceOption._
import iterace.IteRaceOption
import iterace.IteRaceOptions
import iterace.IteRaceOption._
import iterace.IteRace
import scala.io._
import java.io.FileWriter
import sppa.util.log
import sjson.json._
import DefaultProtocol._
import JsonSerialization._
import scala.actors.Futures._
import scala.collection.JavaConverters._
import java.lang.management.ManagementFactory
import java.io.File
import sppa.util.debug
import wala.AnalysisOptions
import com.typesafe.config.ConfigFactory
import sppa.util.JavaTest
import sppa.util.Timer
import com.typesafe.config.Config
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigResolveOptions

class Evaluate extends JavaTest {

  debug.activate
  debug(this.getClass().toString())

  def result: String = {
    val subjectsConfig = ConfigFactory.load("subjects")
    implicit val config = subjectsConfig.getConfig("evaluation." + testName.getMethodName()) withFallback
      subjectsConfig withFallback
      ConfigFactory.load
    println(config.root.render)
    IteRace(AnalysisOptions()).races.prettyPrint()
  }

  def expectNoRaces { assertEquals("", result) }
  def expectSomeRaces { assertNotSame("", result) }
  def expect(printedRaces: String) { assertEquals(printedRaces, "\n" + result + "\n") }

  @Test def BH { expectNoRaces }
  @Test def EM3D { expectNoRaces }
  @Test def jUnit { expectNoRaces }
  @Test def LuSearch { expectSomeRaces }
  @Test def MonteCarlo {
    expect("""
Loop: montecarlo.parallel.AppDemo.runParallel(AppDemo.java:178)

Static: montecarlo.parallel.Universal
 .UNIVERSAL_DEBUG
   (a)  montecarlo.parallel.Universal.<init>(Universal.java:63)
   (b)  montecarlo.parallel.Universal.<init>(Universal.java:63)
""")
  }

  @Test def OldCoref { expectNoRaces }
  @Test def WEKA { expectNoRaces }
}

object Evaluate extends App {

  val arguments = args.toList

  val subjectName = arguments.head
  val optionNames = arguments.tail
  val options = IteRaceOptions(optionNames map { IteRaceOption(_) })

  println(subjectName)
  println(options)

  val subjectsConfig = ConfigFactory.load("subjects", ConfigParseOptions.defaults.setAllowMissing(false), ConfigResolveOptions.defaults)

  val resultsFile = "IteRace/" + EvalUtil.fileName(subjectName, optionNames) + ".json"
  println((new File(resultsFile)).getAbsolutePath())
  val fw = new FileWriter(resultsFile)

  Timer(60000)({
    System.err.println("Timeout")
    fw.close()
    sys.exit()
  })

  val iteRace = IteRace(AnalysisOptions()(
    subjectsConfig.getConfig("evaluation." + subjectName) withFallback
      subjectsConfig.getConfig("evaluation") withFallback subjectsConfig), options)

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