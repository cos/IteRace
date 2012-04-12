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
import com.ibm.wala.ipa.callgraph.propagation.ContainerUtil

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
    "Ljava/io/ObjectStreamClass",

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

    // lock classes
    "Ljava/util/concurrent/locks/ReentrantLock",
    "Ljava/util/concurrent/ConcurrentHashMap$Segment",
    "Ljava/util/concurrent/locks/AbstractQueuedSynchronizer",

    // date-time classes
    "Ljava/util/Date",
    "Ljava/util/Locale",
    "Ljava/util/Timezone",
    "Ljava/util/TimeZone",
    "Ljava/util/GregorianCalendar",

    "Ljava/util/Properties",

    // math
    "Ljava/lang/Long",
    "Ljava/lang/Integer",
    "Ljava/lang/Character",
    "Ljava/math/BigDecimal",
    "Ljava/math/BigInteger",
    "Ljava/lang/Boolean",

    // beans
    "Ljava/beans/PropertyDescriptor",
    "Ljava/beans/IndexedPropertyDescriptor",

    // regex Pattern class - it is thread-safe
    "Ljava/util/regex/Pattern",

    "Ljava/net/URL",

    // kind of coarse-grained
    "Ljava/lang/System",

    // soft and weak reference
    "Ljava/lang/ref/SoftReference",
    "Ljava/lang/ref/WeakReference",

    // not very sure about this one
    "Ljava/text/AttributedString",

    // for testing
    "Lparticles/ParticleWithKnownThreadSafe$ThreadSafeOnClosure",

    "the end of the list")

  val classAndMethod = Set(

    // this shouldn't be here but there is a bug in wala:
    // "found a bug in the wala framework - it sees as the same weka.core.Attribute and 
    // java.text.NumberFormat$Field; the latter is of class AttributedCharacterIterator$Attribute - that might be the reason

    ("Lweka/core/Attribute", "equalsMsg"))

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
      classAndMethod.exists({ case (c, m) => callee.getDeclaringClass().getName().toString() == c && callee.getName().toString() == m }) ||
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

    "Ljava/io/ObjectStreamClass", // not true

    // for testing
    "Lparticles/ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator")

  def apply(c: C): Boolean = classes.contains(c.getName.toString)

  def apply(m: M): Boolean = apply(m.getDeclaringClass())

  def apply(n: N): Boolean = apply(n.m)
}

object isActuallyLibraryCode {
  /**
   * this are classes that are marked as "application" by WALA by I want them to be library
   * quick hack, could be made more elegantly
   */
  val classes = immutable.HashSet[String](

    // for WEKA
    "Lweka/core/SparseInstance",
    "Lweka/core/DenseInstance",
    "Lweka/core/AbstractInstance")

  def apply(c: C): Boolean = classes.contains(c.getName.toString)

  def apply(m: M): Boolean = apply(m.getDeclaringClass())

  def apply(n: N): Boolean = apply(n.m)
}

object movesObjectsAround {
  val classes = immutable.HashSet[String](
    "Ljava/util/regex/Pattern",
    // collections that are safe
    "Ljava/util/Collections$SynchronizedCollection",
    "Ljava/util/Collections$SynchronizedList",
    "Ljava/util/Collections$SynchronizedSet",
    "Ljava/util/Collections$SynchronizedSortedSet",
    "Ljava/util/Vector",

    // other collections
    "Ljava/util/HashMap",

    "Ljava/io/ObjectStreamClass",

    // data structures used by WEKA
    //    "Lweka/core/SparseInstance",

    // soft and weak reference
    "Ljava/lang/ref/SoftReference",
    "Ljava/lang/ref/WeakReference")

  def apply(c: C): Boolean = classes.contains(c.getName.toString) || ContainerUtil.isContainer(c)

  def apply(m: M): Boolean = apply(m.getDeclaringClass())

  def apply(n: N): Boolean = apply(n.m)
}

object doesntMoveObjects {
  val classes = immutable.HashSet[String]()

  def apply(c: C): Boolean = classes.contains(c.getName.toString)

  def apply(m: M): Boolean = apply(m.getDeclaringClass())

  def apply(n: N): Boolean = apply(n.m)
}

object threadSafe {
  val classes = Seq[String](

    // for testing
    "Lparticles/ParticleWithKnownThreadSafe$ThreadSafe",

    // collections
    "Ljava/util/Collections$SynchronizedCollection", // not true
    "Ljava/util/Vector", // not true
    "Ljava/util/concurrent/ConcurrentHashMap",

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

  /**
   * is the node,i.e., invocation, thread-safe?
   */
  def apply(s: S[I]): Boolean = apply(s.n) || writeInConstructorOnThis(s)

  def writeInConstructorOnThis(s: S[I]): Boolean = s.n.getMethod().isInit() &&
    (s.i match {
      case i: PutI => i.getRef() == 1
      case i: ArrayStoreI => i.getArrayRef() == 1
      case _ => false
    })
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