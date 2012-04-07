package iterace.stage
import iterace.util.S
import com.ibm.wala.classLoader.IMethod
import iterace.datastructure.Race

// not used anywhere anymore but might in the future
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
    "Ljava/lang/String",
    "Ljava/lang/StringBuilder",
    "Ljava/lang/StringBuffer",
    // used for ignoring System.out stuff. but it is thread-safe in general
    "Ljava/io/PrintStream",
    "Ljava/lang/Throwable",

    // kind of coarse-grained
    "Ljava/lang/System",
    
    // for reflection
    "Ljava/lang/Class",
    "Ljava/lang/Method"
  )

  def apply(m: IMethod): Boolean = {
    val isIt = threadSafe.exists { m.getDeclaringClass().getName().toString().equals(_) }
    
//    if (m.toString().contains("Throwable")) println(m.toString() + isIt);
    isIt
  }
}