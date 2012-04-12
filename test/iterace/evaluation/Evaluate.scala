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

abstract class Evaluate(
  entryClass: String,
  entryMethod: String = "main([Ljava/lang/String;)V") extends IteRaceTest {

  debug.activate
  debug(this.getClass().toString())

  var options: Set[IteRaceOption] = IteRaceOptions(TwoThreadModel, DeepSynchronized, BubbleUp, AppLevelSynchronized)

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

  val fw = new FileWriter("evaluation/" + subjectName + "/" + optionNames.mkString("_") + ".json")

  println(log.entries.toMap)
  fw.write("\n\n" + tojson(log.entries.toMap)); fw.close()
}

object Bla extends App {
  println(ManagementFactory.getRuntimeMXBean().getName())
}

object EvaluateAll extends App {
  import scala.sys.process._

  println("start")

  args.foreach(subject => {

    for (currentOptions <- IteRaceOptions.powerset(IteRaceOptions.allAsString)) {
//      val currentOptions = IteRaceOptions.allAsString

      println(subject + ": "+currentOptions.toList.sorted.mkString(" , "))
      
      val cmd = Seq("env", "JAVA_OPTS=-Xmx2g",
        "scala", "-cp", classPath, "iterace.evaluation.Evaluate", subject) ++ currentOptions

      val logFile = new FileWriter("evaluation/" + subject + "/" + currentOptions.mkString("_") + ".txt")
      val errorFile = new FileWriter("evaluation/" + subject + "/" + currentOptions.mkString("_") + ".error.txt")
      val logger = ProcessLogger(
          line => logFile.write(line + "\n"), 
          line => errorFile.write(line + "\n") )
      val f = future {
        cmd ! logger
      }
      

      println("in the run")
      awaitAll(600000, f)
      if (!f.isSet) {
        println(" => TIMEOUT")
        ("kill -9 " + pid).!
        logFile.write("\nTIMEOUT")
      } else {
        println(" => OK")
      }
      
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
    val eclipseRuntime = "/Applications/eclipse/plugins/org.eclipse.core.runtime_3.7.0.v20110110.jar"
    val eclipseEquinox = "/Applications/eclipse/plugins/org.eclipse.equinox.common_3.6.0.v20110523.jar"
    val eclipseOsgi = "/Applications/eclipse/plugins/org.eclipse.osgi_3.7.2.v20120110-1415.jar"

    "bin:lib/*.jar:lib/org.junit.jar:../wala/com.ibm.wala.util/bin:../wala/com.ibm.wala.shrike/bin:../wala/com.ibm.wala.core/bin:../lib/parallelArray.mock/bin" +
      ":" + eclipseRuntime +
      ":" + eclipseEquinox +
      ":" + eclipseOsgi +
      ":lib/sjson_2.9.1-0.17.jar" +
      ":lib/dispatch-json_2.9.1-0.8.8.jar"

  }
}


      //      if(timedRun(cmd, 1000))
      //        println("OK")
      //      else
      //        println("NOT OK")
      //      
      //      Thread.sleep(3000)
// code from www.kylecartmell.com - translated to Scala and adapted
//object timedRun {
//  def apply(cmd: Seq[String], time: Long): Boolean = {
//    println("1")
//    val timer = new Timer(true);
//    println("2")
//    val process = Runtime.getRuntime().exec(cmd.toArray);
//    println("3")
//    try {
//      println("4")
//      val interrupter: TimerTask = new TimerTask { override def run() { println("throw"); Thread.currentThread().interrupt(); } }
//      println("5")
//      timer.schedule(interrupter, time);
//      println("6")
//      process.waitFor()
//      println("7")
//      true
//    } catch {
//      case e: InterruptedException => {
//        println("10")
//        process.destroy();
//        println("11")
//        false
//      }
//    } finally {
//      println("8")
//      timer.cancel();
//      println("9")
//      // If the process returns within the timeout period, we have to stop the interrupter
//      // so that it does not unexpectedly interrupt some other code later.
//
//      Thread.interrupted(); // We need to clear the interrupt flag on the current thread just in case
//      // interrupter executed after waitFor had already returned but before timer.cancel
//      // took effect.
//      //
//      // Oh, and there's also Sun bug 6420270 to worry about here.
//      true
//    }
//  }
//}

  //  val uglyClassPath = "/Applications/eclipse/plugins/*.jar:/Applications/eclipse/plugins/org.eclipse.core.runtime_3.7.0.v20110110.jar:/Applications/eclipse/plugins/org.eclipse.osgi_3.7.2.v20120110-1415.jar:/Applications/eclipse/plugins/org.eclipse.equinox.weaving.hook_1.0.0.v20100503.jar:/Applications/eclipse/plugins/org.eclipse.equinox.common_3.6.0.v20110523.jar:/Applications/eclipse/plugins/org.eclipse.core.jobs_3.5.101.v20120113-1953.jar:/Applications/eclipse/plugins/org.eclipse.core.runtime.compatibility.registry_3.5.0.v20110505/runtime_registry_compatibility.jar:/Applications/eclipse/plugins/org.eclipse.equinox.registry_3.5.101.R37x_v20110810-1611.jar:/Applications/eclipse/plugins/org.eclipse.equinox.preferences_3.4.2.v20120111-2020.jar:/Applications/eclipse/plugins/org.eclipse.core.contenttype_3.4.100.v20110423-0524.jar:/Applications/eclipse/plugins/org.eclipse.equinox.app_1.3.100.v20110321.jar:/Applications/eclipse/plugins/org.eclipse.core.resources_3.7.101.v20120125-1505.jar"