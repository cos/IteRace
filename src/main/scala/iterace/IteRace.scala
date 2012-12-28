package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection._
import scala.collection.JavaConversions._
import wala.WALAConversions._
import com.ibm.wala.util.graph.traverse.DFS
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import com.ibm.wala.ipa.callgraph.ContextKey
import com.ibm.wala.ipa.callgraph.ContextItem
import com.ibm.wala.dataflow.IFDS.PathEdge
import com.ibm.wala.properties.WalaProperties
import iterace.stage._
import iterace.pointeranalysis._
import iterace.datastructure.LockSets
import iterace.datastructure.MayAliasLockConstructor
import iterace.IteRaceOption._
import sppa.util._
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import wala.AnalysisOptions
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigResolveOptions
import java.io.FileWriter
import sjson.json._
import DefaultProtocol._
import JsonSerialization._
import java.io.File
import com.typesafe.config.ConfigException

class IteRace private (
  options: AnalysisOptions,
  iteRaceOptions: Set[IteRaceOption], config: Config) {

  def this(options: AnalysisOptions, config: Config) =
    this(options, Set(DeepSynchronized, IteRaceOption.BubbleUp), config)

  debug("Options: " + iteRaceOptions.mkString(", "))

  log.startTimer("pointer-analysis");

  val pa = new RacePointerAnalysis(options, iteRaceOptions)
  import pa._
  log.endTimer
  log.startTimer("potential-races")
  private val potentialRaces = new PotentialRaces(pa)()
  log.endTimer
  log("potential-races", potentialRaces.size)
  //  potentialRaces.children.foreach { _.children.foreach(set => debug(set.prettyPrint)) }
  private var currentRaces = potentialRaces

  log.startTimer("locksets")
  val lockSetMapping = new LockSets(pa, new MayAliasLockConstructor(pa))
  log.endTimer
  //  log("locks",)

  val filterByLockMayAlias = new FilterByLockMayAlias(pa, lockSetMapping)

  if (iteRaceOptions(DeepSynchronized)) {
    log.startTimer("deep-synchronized")
    currentRaces = filterByLockMayAlias(currentRaces)
    log.endTimer
    log("deep-synchronized", currentRaces.size)
  }

  if (iteRaceOptions(IteRaceOption.BubbleUp)) {
    log.startTimer("bubble-up")
    currentRaces = stage.BubbleUp(pa)(currentRaces)
    log.endTimer
    log("bubble-up", currentRaces.size)
  }

  if (iteRaceOptions(Synchronized)) {
    log.startTimer("app-level-synchronized")
    currentRaces = filterByLockMayAlias(currentRaces)
    log.endTimer
    log("app-level-synchronized", currentRaces.size)
  }

  val races = currentRaces
  log("races", races.size)

  debug(log.entries)
  try {
    val racesFile = config.getString("iterace.races-file")
    val fw = new FileWriter(racesFile)
    fw.write(races.prettyPrint())
    fw.close()
  } catch {
    case e: ConfigException.Missing => println(e)
  }
}

object IteRace extends App {

  println((new File(".")).getAbsolutePath())

  val config = loadConfig
  val fw = new FileWriter(config.getString("iterace.log-file"))
  initTimeout
  val iteRace = apply(config)
  printLog

  def printLog {
    println(log.entries.toMap)
    fw.write("" + tojson(log.entries.toMap)); fw.close()
    sys.exit(0)
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

  private def initTimeout = Timer(config.getLong("iterace.timeout")) {
    log("timeout", true)
    printLog
  }

  def apply(config: Config = ConfigFactory.load): IteRace = {
    new IteRace(AnalysisOptions()(config), IteRaceOption(config), config)
  }

  @deprecated("use apply(config) instead")
  def apply(options: AnalysisOptions, iteRaceoOptions: Set[IteRaceOption]) = new IteRace(options, iteRaceoOptions, ConfigFactory.load)

  @deprecated("use apply(config) instead")
  def apply(options: AnalysisOptions) = new IteRace(options, IteRaceOption.values, ConfigFactory.load)
}

class AnalysisException(m: String) extends Throwable