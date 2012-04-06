package iterace.util

import scala.collection._

object log {
  var activeConsole = false
  var activeTimer = false
  
  val timers:mutable.LinkedHashMap[String, Long] = mutable.LinkedHashMap()

  def startTimer(name: String) = {
    if(activeConsole)
      println("START: "+name)
    if(activeTimer) {
    	timers(name) = System.currentTimeMillis();
    }
  }
  
  def endTimer:Unit = if(activeTimer) endTimer(timers.head._1)
  
  def endTimer(name:String):Unit = {
    if(activeConsole)
      print("DONE: "+name);
    
    if(activeTimer) {
      val endTime = System.currentTimeMillis - timers(name)
      if(activeConsole)
      	println(" at "+endTime)
    	timers.remove(name)
    }
    else
      if(activeConsole)
      	println()
  }
  
  def apply(note: Any) = {
    if(activeConsole)
      println(note)
  }
}