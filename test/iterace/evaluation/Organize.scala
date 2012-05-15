package iterace.evaluation
import java.io.FileWriter
import dispatch.json.Js
import sjson.json._
import DefaultProtocol._
import JsonSerialization._
import scala.io._
import scala.sys.process._
import iterace.IteRaceOptions

object mergeJSonForSubject extends App {

  val subject = args.head

  val dir: String = "evaluation/" + subject

  val cmd = "ls -1a " + dir

  println(cmd)
  val bigJson = new FileWriter(dir + ".json")
  val newData: Map[String, Map[String, String]] = cmd.lines_! filter { _.contains("json") } map (s => {
    val file = dir + "/" + s
    val source = scala.io.Source.fromFile(file)
    val data = source.mkString
    val scenario = s.split('.').head
    val jsondata = Js(data)
    val results = fromjson[Map[String, String]](jsondata)
    (if (scenario == "") "NONE" else scenario, results)
  }) toMap

  println(newData.size)

  val newJson = tojson[Map[String, Map[String, String]]](newData)

  bigJson.write(newJson.toString())
  bigJson.close()
}

object mergeAll extends App {
  val dir: String = "evaluation"

  val cmd = "ls -1 " + dir

  println(cmd)
  val bigJson = new FileWriter(dir + ".json")
  val newData = cmd.lines_! filter { _.contains("json") } map (s => {
    //    "scala -cp bin:lib/* iterace.evaluation.mergeJSonForSubject "+s.!
    val file = dir + "/" + s
    val source = scala.io.Source.fromFile(file)
    val data = source.mkString
    println(s)
    val subject = s.split('.').head
    val jsondata = Js(data)
    println(jsondata)
    val results = fromjson[Map[String, Map[String, String]]](jsondata)
    (subject, results)
  }) toMap

  val newJson = tojson(newData)

  bigJson.write(newJson.toString())
  bigJson.close()
}

// result
case class R(s: S, racess: Map[String, Int], times: Map[String, Int]) {

  val races = racess("races")
  val time = times.values.sum

  override def toString = "R(" + s + ", " + races + ", " + time + ")"
}

// scenario
case class S(s: Set[String]) {

  private val allOnString = List("2-threads-model", "known-safe-filtering", "bubble-up", "deep-synchronized", "app-level-synchronized")
  private val allOnChar = allOnString map { shorten(_) }

  override def toString = sl.mkString
  val sl = allOnChar map (ss => { if (s.map(shorten(_)).contains(ss)) ss.toUpperCase else ss })

  def apply(feature: String) = s.contains(feature)

  def displayWithout(s: String) = sl.filter(c => !(c == shorten(s)))
  
  private def shorten(s: String): Char = {
    s match {
      case "2-threads-model" => 't'
      case "known-safe-filtering" => 'f'
      case "bubble-up" => 'b'
      case "deep-synchronized" => 's'
      case "app-level-synchronized" => 'a'
    }
  }
}

object S {
  val t = "2-threads-model"
  val f = "known-safe-filtering"
  val b = "bubble-up"
  val s = "deep-synchronized"
  val a = "app-level-synchronized"

  val full = S(Set(t, f, b, s, a))

  val powerset = IteRaceOptions.powerset(full.s).map(s => S(s.toSet)).toList.sortBy(_.toString).reverse

  def feature(feat: Char): String = feat match {
    case 't' => t
    case 'f' => f
    case 'b' => b
    case 's' => s
    case 'a' => a
  }

}

object readData extends App {
  def apply() = {
    val source = scala.io.Source.fromFile("evaluation.json")
    val stringData = source.mkString
    val jsonData = Js(stringData)
    val data = fromjson[Map[String, Map[String, Map[String, String]]]](jsonData)

    for ((projectName, projectData) <- data) yield {
      (projectName,
        (for ((scenario, result) <- projectData) yield {
          var r = result mapValues (_.toInt)
          var sName = scenario.split('_').toSet.filter(_ != "NONE")
          R(S(sName),
            r.filterKeys(!_.contains("time")),
            r.filterKeys(_.contains("time")))
        }) toList)
    }
  }

  apply()
}
