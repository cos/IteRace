package iterace

import iterace.util.Tracer
import wala.S

object Interactive extends App {
  val r = new IteRaceRunner(args.toList)
  val iteRace = r run

  val t = new Tracer(iteRace.pa.getCallGraph(), iteRace.pa.getPointerAnalysis())

  var l = ""
  object IsInt {
    def unapply(s: String): Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _: java.lang.NumberFormatException => None
    }
  }

  val intToAccess = S.accessesRepo map { _.swap }

  do {
    print("\n>")
    l = readLine
    l match {
      case "s" => println("\n" + scala.io.Source.fromFile(r.config.getString("iterace.races-file")).mkString) // show races
      case "q" => // quit
      case IsInt(n) => {
        val s = intToAccess(n)
        println(s.prettyPrint)
        t.trace(s)
      }
    }
  } while (l != "q")

  println("bye!")
}