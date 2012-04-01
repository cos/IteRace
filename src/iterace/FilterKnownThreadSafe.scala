package iterace
import iterace.util.S

class FilterByKnownThreadSafe extends Function1[Set[Race], Set[Race]] {

  val threadSafe = Seq[String](
    "java/util/regex/Pattern", "java/lang/System, exit",
    "java/io/PrintStream, ",
    "java/io/PrintWriter, ",
    "java/lang/AbstractStringBuilder, ",
    "java/io/BufferedWriter, ",
    "java/lang/String, ",
    "java/io/StringReader, ",
    "java/util/Vector, ",
    "java/lang/Integer, ",
    "java/lang/Long, ",
    "java/lang/Throwable, ",
    "java/security/AccessControlContext, getDebug", // not relevant
    "java/util/Random, <init>", "java/lang/Integer, <init>",
    "java/lang/SecurityManager, ",
    "java/lang/ClassLoader, initSystemClassLoader",
    "java/util/Properties, ");


  def apply(races: Set[Race]): Set[Race] = {
    
    races filter (r =>{ ! threadSafe.exists( pattern =>
    		 (r.a.n.getMethod().toString() contains pattern) && (r.a.n.getMethod().toString() contains pattern) 
    )})
  }
}