package iterace

import iterace.util.Tracer
import wala.S
import wala.O
import wala.WALAConversions._
import scala.collection.JavaConverters._
import com.ibm.wala.ipa.callgraph.propagation.AbstractFieldPointerKey
import com.ibm.wala.ipa.callgraph.propagation.PointerKey

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
      case "o" => {
        val intToObjects = O.printRepo map { _.swap }
        val x = readLine.toInt
        println(intToObjects(x).prettyPrint)
      }
      case s: String if s.startsWith("opp") => {
        val x = s.split(" ")(1).toInt
        val intToObjects = O.printRepo map { _.swap }
        val heap = iteRace.pa.heap
        heap.getPredNodes(intToObjects(x)).asScala flatMap { heap.getPredNodes(_).asScala } foreach {
          case o: O => println(o.prettyPrint)
          case p => println(p.toString)
        }
      }
      case s: String if s.startsWith("op") => {
        val x = s.split(" ")(1).toInt
        val intToObjects = O.printRepo map { _.swap }
        val heap = iteRace.pa.heap
        heap.getPredNodes(intToObjects(x)).asScala foreach {
          case o: O => println(o.prettyPrint)
          case o: P =>
          case p: PointerKey => println(p.prettyPrint)
        }
      }
      case _ => println("unknown command")
    }
  } while (l != "q")

  println("bye!")
}