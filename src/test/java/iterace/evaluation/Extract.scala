package iterace.evaluation
import java.io.FileWriter
import dispatch.json.Js
import sjson.json._
import DefaultProtocol._
import JsonSerialization._
import scala.io._
import scala.sys.process._
import iterace.IteRaceOption

object showData extends App {
  import helpful._

  val data = readData()

  for ((projectName, projectData) <- data) {
    println(projectName + ":" + projectData.size)

    S.powerset foreach (s => {
      val r = find(projectData, s)
      println(r getOrElse s + " - ")
    })

    //    projectData.sortBy(_.finalRaces) foreach {
    //      println(_)
    //    }
  }
}

object helpful {
  import scala.math.pow

  def find(pd: Iterable[R], s: S) = pd.find {
    case R(ss, _, _) if ss == s => true
    case _ => false
  }
  def find(pd: Iterable[R], f: S => Boolean) = pd.filter {
    case R(s, _, _) if f(s) => true
    case _ => false
  }

  def ga(l: Iterable[Double]) = pow(
    l.map(v => if (v < 0.5) 0.5 else v).product,
    1 / (l.size.toDouble))

  val apps = List("EM3D", "BH", "MonteCarlo", "jUnit", "Coref", "LuSearch", "WEKA")

  def hline { println("\\hline") }
  def textbf(s: String) { print("\\textbf{" + s + "}") }
}

object tableAllRaces extends App {
  import scala.math._
  import S._
  import helpful._

  val data = readData()

  if (args.size == 2)
    textbf(S.feature(args.tail.head.head))

  for (app <- apps) {
    print("&" + (app match {
      case "MonteCarlo" => "MC"
      case _ => app
    }))
  }
  print("& Mean")
  print("\\\\  \n"); hline

  def racess(app: String) = data(app) map { _.races }
  def times(app: String) = data(app) map { _.time }

  def formatRaceInt(r: Int) = (r match {
    case r if r > 1000000 => "" + r / 1000000 + "." + r % 1000000 / 100000 + "M"
    case _ => r
  }).toString

  def formatTimeInt(t: Int) = t / 1000 + "." + t % 1000 / 100

  def maxOfRaces(app: String) = racess(app).max

  args.head match {
    case "races" => printStuff(
      r => { r.races }, formatRaceInt, maxOfRaces)
    case "time" =>
      printStuff(r => { r.time }, formatTimeInt, app => { 600000 })
    case "races-feature" =>
      printStuffByFeature(feature(args.tail.head.head), r => { r.races }, formatRaceInt, maxOfRaces)
    case "time-feature" =>
      printStuffByFeature(feature(args.tail.head.head), r => { r.time }, formatTimeInt, app => { 600000 })
  }

  def printStuff(f: R => Int, format: Int => String, max: String => Int) = {
    var count = 0

    powerset foreach (s => {

      textbf(s.toString)
      val allNormalizedForScenario = for (app <- apps) yield {
        val r = find(data(app), s)

        val str = r map (rr => format(f(rr)).toString) getOrElse "-"

        print(" & " + str)

        r map (r => f(r).toDouble) getOrElse max(app).toDouble
      }
      print(" & " + format(ga(allNormalizedForScenario).toInt))
      println("\\\\")

      count += 1
      if (count % 4 == 0)
        hline
      if (count % 16 == 0)
        hline
    })
  }

  def printStuffByFeature(feature: String, func: R => Int, format: Int => String, max: String => Int) = {
    var count = 0

    // !!! reversed - positive is native and the other way around
    val bigResults = negative(feature).zip(positive(feature)) map {
      case (sp, sn) => {
        println("\\texttt{" + sp + "-" + sn + "}")
        val allNormalizedForScenario = for (app <- apps) yield {
          val rp = find(data(app), sp)
          val rn = find(data(app), sn)

          val str = if (rp.isDefined && rn.isDefined)
            format(func(rp.get) - func(rn.get))
          else
            "-"
          print(" & " + str)

          def normalize(r: Option[R]) = r map (r => func(r).toDouble) getOrElse max(app).toDouble

          (normalize(rp), normalize(rn))
        }
        val (pos, neg) = allNormalizedForScenario unzip

        val themean = (ga(pos) - ga(neg)).toInt
        print(" & " + format(themean))
        println(" \\\\")
        (pos, neg, themean.toDouble)
      }
    }

    hline
    val (pos, neg, theOtherMeans) = bigResults.unzip3
    val fullTranspose = (pos.transpose zip neg.transpose)
    val means = fullTranspose map {
      case (pos, neg) => {
        val themean = (ga(pos) - ga(neg)).toInt
        print(" & "); textbf(format(themean))
        themean.toDouble
      }
    }
    val theothermean = ga(theOtherMeans).toInt
    println(" & "); textbf(ga(means).toInt + " | " + format(theothermean))
    println(" \\\\")
  }

  def positive(feature: String) = powerset.filter {
    case s: S if s(feature) => true
    case _ => false
  }

  def negative(feature: String) = powerset -- positive(feature)
}

object differences extends App {
  import scala.math._

  val data = readData()

  val feature = args.head

  for ((projectName, projectData) <- data) {
    println(projectName + ":" + projectData.size)
    //    val ds = positive(projectData, t) map (rp => {
    //      val rn = negative(projectData, t).find {case R(s, _,_ ) if s == rp.s - t => true; case _ => false}
    //      val d = rp.totalTime - rn.get.totalTime
    //      println(d)
    //      d
    //    })
    println(projectData)
    val pvals = positive(projectData, S.t) map { _.races } map (_.toDouble + 0.9)
    val nvals = negative(projectData, S.t) map { _.races } map (_.toDouble + 0.9)
    println(pvals)
    println(pow(pvals.product, 1 / pvals.size.toDouble) - pow(nvals.product, 1 / nvals.size.toDouble))
  }

  def positive(results: List[R], feature: String) = results.filter {
    case R(s, _, _) if s(feature) => true
    case _ => false
  }

  def negative(results: List[R], feature: String) = results.filter {
    case R(s, _, _) if !s(feature) => true
    case _ => false
  }
}