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

object Evaluate extends App {

  val arguments = args.toList

  val subjectName = arguments.head
  val optionNames = arguments.tail
  val options = IteRaceOptions(optionNames map { IteRaceOption(_) })

  println(subjectName)
  println(options)

  val subjectsConfig = ConfigFactory.load(subjectName, ConfigParseOptions.defaults.setAllowMissing(false), ConfigResolveOptions.defaults)

  val resultsFile = "evaluation/" + subjectName + "/" + optionNames.mkString("_") + ".json"
  println((new File(resultsFile)).getAbsolutePath())
  val fw = new FileWriter(resultsFile)

  Timer(60000)({
    System.err.println("Timeout")
    fw.close()
    sys.exit()
  })

  val iteRace = IteRace(AnalysisOptions()(subjectsConfig), options)

  println(log.entries.toMap)
  fw.write("" + tojson(log.entries.toMap)); fw.close()
  sys.exit(0)
}