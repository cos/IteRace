package iterace

import java.io.FileWriter
import sppa.util.log
import java.io.File
import com.typesafe.config.ConfigFactory
import sjson.json._
import DefaultProtocol._
import JsonSerialization._
import com.typesafe.config._
import sppa.util.Timer

class IteRaceRunner(args: List[String]) {
  println("Configuration: "+args.mkString("\n"))
  val config = loadConfig
  val fw = new FileWriter(config.getString("iterace.log-file"))

  def run = {
    val iteRace = IteRace(config)
    end
    iteRace
  }

  def end {
    printLog
  }

  private def printLog {
    println(log.entries.toMap)
    fw.write("" + tojson(log.entries.toMap)); fw.close()
  }

  private def loadConfig = {
    val IncludeFileRegex = "configFile=(.*)".r
    val configFiles = args collect {
      case arg if IncludeFileRegex.findFirstIn(arg).isDefined => {
        val IncludeFileRegex(file) = arg.trim
        file
      }
    } map { new File(_) }
    val includedFileConfigs = configFiles map { ConfigFactory.parseFile(_, ConfigParseOptions.defaults.setAllowMissing(false)) }

    val IncludeRegex = "config=(.*)".r
    val configs = args collect {
      case arg if IncludeRegex.findFirstIn(arg).isDefined => {
        val IncludeRegex(file) = arg.trim
        file
      }
    }
    val includedConfigs = configs map { ConfigFactory.load(_, ConfigParseOptions.defaults.setAllowMissing(false), ConfigResolveOptions.defaults()) }

    // latter ones take precedence
    val foldedConfigs = (includedFileConfigs ++ includedConfigs).foldLeft(ConfigFactory.load) { (c1: Config, c2: Config) =>
      c2 withFallback c1
    }
    val commandLineConfig = args collect { case arg if !IncludeRegex.findFirstIn(arg).isDefined => arg } mkString ","
    ConfigFactory.parseString(commandLineConfig) withFallback foldedConfigs resolve
  }
}