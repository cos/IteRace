package iterace.datastructure
import iterace.util.S
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import com.ibm.wala.ipa.callgraph.CGNode
import iterace.pointeranalysis.ThreadSafeOnClosure
import iterace.util.WALAConversions._
import com.ibm.wala.analysis.reflection.InstanceKeyWithNode
import scala.collection._
import com.ibm.wala.ipa.callgraph.ContextKey
import iterace.pointeranalysis.Uninteresting

object threadSafeOnClosure extends ContextKey {

  val classes = immutable.HashSet[String](

    // used for ignoring string operations, including concatenation
    "Ljava/lang/String",
    "Ljava/lang/StringBuilder",
    "Ljava/lang/StringBuffer",
    "Ljava/lang/AbstractStringBuilder",

    "Ljava/io/BufferedInputStream",
    "Ljava/io/FilterInputStream",
    "Ljava/io/BufferedWriter",
    "Ljava/io/PrintStream",
    "Ljava/io/File",
    "Ljava/io/FileDescriptor",
    "Ljava/io/FileInputStream",
    "Ljava/io/FilePermission",

    "Ljava/io/FilePermission",

    "Ljava/lang/Throwable",
    "Ljava/lang/Shutdown",

    "Ljava/security/AccessControlContext",
    "Ljava/security/AccessControlException",
    "Ljava/security/Permission",
    "Ljava/security/Security",
    "Ljava/security/BasicPermission",

    "Ljava/util/Currency",
    "Ljava/lang/SecurityManager",

    // for reflection
    "Ljava/lang/Class",
    "Ljava/lang/Class$EnclosingMethodInfo",
    "Ljava/lang/Method",
    "Ljava/lang/ClassLoader",

    "Ljava/util/Date",
    "Ljava/util/Locale",
    "Ljava/util/Timezone",
    "Ljava/util/TimeZone",
    "Ljava/util/GregorianCalendar",

    "Ljava/util/Properties",

    "Ljava/lang/Long",
    "Ljava/lang/Integer",
    "Ljava/lang/Character",
    "Ljava/math/BigDecimal",
    "Ljava/math/BigInteger",

    // regex Pattern class - it is thread-safe
    "Ljava/util/regex/Pattern",

    "Ljava/net/URL",

    // kind of coarse-grained
    "Ljava/lang/System",

    // for testing
    "Lparticles/ParticleWithKnownThreadSafe$ThreadSafeOnClosure",

    "the end of the list")

  /**
   * is it thread-safe on closure when called from caller
   */
  def apply(caller: N): Boolean =
    caller.c(Uninteresting) != null ||
      caller.c(ThreadSafeOnClosure) != null ||
      apply(caller.m)

  /**
   * is any method of this class thread-safe on closure
   */
  def apply(c: C) =
    classes.contains(c.getName().toString())

  /**
   * is any method callee thread-safe on closure
   */
  def apply(callee: M): Boolean =
    apply(callee.getDeclaringClass()) ||
      ZeroXInstanceKeys.isThrowable(callee.getDeclaringClass())

  /**
   * is this particular object thread-safe on closure
   */
  def apply(o: O): Boolean =
    o match {
      case o: InstanceKeyWithNode =>
        generatesSafeObjects(o.getNode) ||
          apply(o.getConcreteType())
      case _ => false
    }

  /**
   * is it thread-safe on closure
   */
  def apply(caller: N, callee: M, params: Array[O]): Boolean =
    apply(caller) ||
      apply(callee) ||
      // receiver is thread-safe
      (params.headOption match {
        case Some(o) => apply(o)
        case None => false
      })
}

object generatesSafeObjects {
  /**
   * classes that generate thread-safe objects, i.e. the objects that
   * are instantiated inside this class are thread-safe
   */
  val classes = immutable.HashSet[String](
    // kind of coarse-grained
    "Ljava/lang/System",

    "Ljava/security/Security",

    // for testing
    "Lparticles/ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator")

  def apply(c: C): Boolean = classes.contains(c.getName.toString)

  def apply(m: M): Boolean = apply(m.getDeclaringClass())

  def apply(n: N): Boolean = apply(n.m)
}

object movesObjectsAround {
  val classes = immutable.HashSet[String](
    "Ljava/util/regex/Pattern",
    "Ljava/util/Vector")

  def apply(c: C): Boolean = classes.contains(c.getName.toString)

  def apply(m: M): Boolean = apply(m.getDeclaringClass())

  def apply(n: N): Boolean = apply(n.m)
}

object doesntMoveObjects {
  val classes = immutable.HashSet[String](
    )

  def apply(c: C): Boolean = classes.contains(c.getName.toString)

  def apply(m: M): Boolean = apply(m.getDeclaringClass())

  def apply(n: N): Boolean = apply(n.m)
}

object threadSafe {
  val classes = Seq[String](

    "Ljava/util/Vector",

    // lock classes
    "Ljava/util/concurrent/locks/ReentrantLock",

    // for testing
    "Lparticles/ParticleWithKnownThreadSafe$ThreadSafe",

    "somethig at the end")

  /**
   * is the node,i.e., invocation, thread-safe?
   */
  def apply(n: N): Boolean = threadSafeOnClosure(n) || apply(n.getMethod())

  /**
   * is any invocation of this method thread-safe?
   */
  def apply(m: M): Boolean = threadSafeOnClosure(m) || apply(m.getDeclaringClass())

  /**
   * is any method of this class thread-safe?
   */
  def apply(c: C): Boolean =
    threadSafeOnClosure(c) || classes.contains(c.getName().toString())

  /**
   * is any invocation of callee thread-safe when called from caller?
   */
  def apply(caller: N, callee: M): Boolean = threadSafeOnClosure(caller) || apply(callee)
}


// not used anywhere anymore but might in the future
//class FilterByKnownThreadSafe extends Function1[Set[Race], Set[Race]] {
//  def apply(races: Set[Race]): Set[Race] =
//    races filter (r => {
//      !threadSafe.exists(pattern =>
//        (r.a.n.getMethod().toString() contains pattern) &&
//          (r.b.n.getMethod().toString() contains pattern))
//    })
//
//}