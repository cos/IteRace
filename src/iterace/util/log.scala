package iterace.util

import scala.collection._
import util.debug

object log {

  val timers: mutable.LinkedHashMap[String, Long] = mutable.LinkedHashMap()

  def startTimer(name: String) {
    debug("START: " + name)
    timers(name) = System.currentTimeMillis();
  }

  def endTimer: Unit = endTimer(timers.head._1)

  def endTimer(name: String): Unit = {
    val runtime = System.currentTimeMillis - timers(name)
    debug("DONE: " + name + " in " + runtime / 1000 + "." + runtime % 1000 + "s")
    timers.remove(name)
    entries += (name+"-time" -> runtime.toString())
  }

  val entries: mutable.Map[String, String] = mutable.ListMap()

  def apply(entry: String, value:Any) = {
     debug(entry +": "+value)
     entries += (entry -> value.toString)
  }
}