package iterace
import iterace.util.S
import com.ibm.wala.classLoader.IMethod

class FilterByKnownThreadSafe extends Function1[Set[Race], Set[Race]] {

  val threadSafe = Seq[String](
    "java/lang/AbstractStringBuilder, ",
    "java/lang/String, ",
    "java/lang/Integer, ",
    "java/lang/Long, ",
    "java/lang/Throwable, ",
    "java/lang/SecurityManager, ",
    "java/lang/ClassLoader, initSystemClassLoader",
    "java.lang.Class, ",
    "java/io/PrintStream, ",
    "java/io/PrintWriter, ",
    "java/io/BufferedWriter, ",
    "java/io/StringReader, ",
    "java/security/AccessControlContext, getDebug", // not relevant
    "java/util/Vector, ",
    "java/util/Properties, ",
    "java/util/Random, <init>", "java/lang/Integer, <init>",
    "java/util/regex/Pattern", "java/lang/System, exit");

  def apply(races: Set[Race]): Set[Race] =
    races filter (r => {
      !threadSafe.exists(pattern =>
        (r.a.n.getMethod().toString() contains pattern) &&
          (r.b.n.getMethod().toString() contains pattern))
    })

}

object threadSafe {
  val threadSafe = Seq[String](
    // used for ignoring string operations, including concatenation
    "java/lang/String, ",
    "java/lang/StringBuilder, ",
    "java/lang/StringBuffer, ",
    // used for ignoring System.out stuff. but it is thread-safe in general
    "java/io/PrintStream, ",
    "java/lang/Throwable, ",

    // kind of coarse-grained
    "java/lang/System, ")

  def apply(m: IMethod): Boolean = {
    val isIt = threadSafe.exists { m.toString().contains(_) }
    
//    if (m.toString().contains("initializeSystemClass")) println(m.toString() + isIt);
    isIt
  }

}