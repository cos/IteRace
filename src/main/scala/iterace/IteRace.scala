package iterace
import com.ibm.wala.analysis.pointers.HeapGraph
import scala.collection._
import scala.collection.JavaConversions._
import edu.illinois.wala.Facade._
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
import edu.illinois.wala.ipa.callgraph.AnalysisOptions
import edu.illinois.wala.ipa.callgraph.propagation.StaticClassObject
import edu.illinois.wala.S
import edu.illinois.wala.ipa.callgraph.propagation.P
import iterace.datastructure.BetterLockConstructor

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

  lazy val (lockSetMapping, filterByLockMayAlias) = {
    log.startTimer("locksets")
    val lockSetMapping = new LockSets(pa, new BetterLockConstructor(pa))
    log.endTimer
    (lockSetMapping, new FilterByLockMayAlias(pa, lockSetMapping))
  }

  if (iteRaceOptions(DeepSynchronized)) {
    val x = filterByLockMayAlias
    log.startTimer("deep-synchronized")
    currentRaces = x(currentRaces)
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
    val x = filterByLockMayAlias
    log.startTimer("app-level-synchronized")
    currentRaces = x(currentRaces)
    log.endTimer
    log("app-level-synchronized", currentRaces.size)
  }

  val races = currentRaces filter { x => true }

  val enumeratedRaces = races map { r => r.prettyPrint }

  log("cg-size", pa.cg.size)
  log("heap-size", pa.heap.size)
  log("methods-num", (pa.cg map { _.getMethod() } toSet).size)

  log("races", enumeratedRaces.size)

  debug(log.entries)
  try {
    val racesFile = config.getString("iterace.races-file")
    val fw = new FileWriter(racesFile)
    fw.write(races.prettyPrint())
    fw.close()

    val racesForJson = "[" +
      ((races map { r =>
        {
          val (ofile, oline) = r.o.s match {
            case Some(s) => (s.sourceFilePath, s.lineNo)
            case None => r.o match {
              case o: StaticClassObject => (o.klass.sourceFilePath, 1)
              case o => (r.o.getConcreteType.sourceFilePath, 1)
            }
          }
          val data = List(
            "is_true" -> "",
            "ofile" -> ofile,
            "oline" -> oline,
            "a1file" -> r.a.sourceFilePath,
            "a1line" -> r.a.lineNo,
            "a2file" -> r.b.sourceFilePath,
            "a2line" -> r.b.lineNo)

          "{" + (data map { case (k, v) => "\"" + k + "\": \"" + v + "\"" } mkString ", ") + "}"
        }
      }).toList.sorted mkString ", \n") +
      "]"

    val fwEnum = new FileWriter(racesFile.replace(".races", ".json.races"))
    fwEnum.write(racesForJson)
    fwEnum.close()
  } catch {
    case e: ConfigException.Missing => println(e)
  }
}

object IteRace extends App {

  val r = new IteRaceRunner(args.toList)
  Timer(r.config.getLong("iterace.timeout")) {
    log("timeout", true)
    r.end
    sys.exit()
  }

  r.run

  sys.exit()

  def apply(config: Config = ConfigFactory.load): IteRace = {
    new IteRace(AnalysisOptions()(config), IteRaceOption(config), config)
  }

  @deprecated("use apply(config) instead")
  def apply(options: AnalysisOptions, iteRaceoOptions: Set[IteRaceOption]) = new IteRace(options, iteRaceoOptions, ConfigFactory.load)

  @deprecated("use apply(config) instead")
  def apply(options: AnalysisOptions) = new IteRace(options, IteRaceOption.values, ConfigFactory.load)
}

class AnalysisException(m: String) extends Throwable