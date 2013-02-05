package iterace

import iterace.util.Tracer
import edu.illinois.wala.S
import edu.illinois.wala.Facade._
import scala.collection.JavaConverters._
import com.ibm.wala.ipa.callgraph.propagation.AbstractFieldPointerKey
import com.ibm.wala.ipa.callgraph.propagation.PointerKey
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey

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
      case s: String if s.startsWith("list-vars") => { // list the variables in a cg node
//        val name = s.split(" ").tail mkString " "
//        (iteRace.pa.cg.asScala.find(_.toString.contains(name))) match {
//          case Some(n) => {
//            println(n.toString)
//            Range(1, n.getIR().getSymbolTable().getMaxValueNumber()) foreach { x =>
//              println(iteRace.pa.heap.asScala.find(_ == P(n, x))
//                map { pk => x + " : \n" + (iteRace.pa.heap.getSuccNodes(pk).asScala map { _.asInstanceOf[O].prettyPrint } mkString "\n") }
//                mkString "\n")
//            }
//          }
//          case None => println("node not found");
//        }
      }
      case "o" => {
//        val intToObjects = O.printRepo map { _.swap }
//        val x = readLine.toInt
//        println(intToObjects(x).prettyPrint)
      }
      case s: String if s.startsWith("opp") => {
//        val x = s.split(" ")(1).toInt
//        val intToObjects = O.printRepo map { _.swap }
//        val heap = iteRace.pa.heap
//        heap.getPredNodes(intToObjects(x)).asScala flatMap { heap.getPredNodes(_).asScala } foreach {
//          case o: O => println(o.prettyPrint)
//          case p => println(p.toString)
//        }
      }
      case s: String if s.startsWith("op") => {
        val x = s.split(" ")(1).toInt
//        val intToObjects = O.printRepo map { _.swap }
//        val heap = iteRace.pa.heap
//        heap.getPredNodes(intToObjects(x)).asScala foreach {
//          case o: O => println(o.prettyPrint)
//          case o: P =>
//          case p: PointerKey => println(p.prettyPrint)
//        }
      }
      case s: String if s.startsWith("cha") => println(iteRace.pa.cha.asScala.find({ case c => c.toString.contains(s.substring(4)) }))
      case s: String if s.startsWith("cg") => println(iteRace.pa.cg.asScala.filter({ case c => c.toString.contains(s.substring(3)) }) mkString "\n")
      case _ => println("unknown command")
    }
  } while (l != "q")

  println("bye!")
}