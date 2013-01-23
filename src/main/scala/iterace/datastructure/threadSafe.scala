package iterace.datastructure

import scala.collection.Set
import scala.collection.immutable
import com.ibm.wala.analysis.reflection.InstanceKeyWithNode
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import iterace.pointeranalysis.ThreadSafeOnClosure
import wala.WALAConversions._
import wala.S

object threadSafe extends SelectorOfClassesAndMethods {
  val classes = Set(

    // for testing
    "particles.ParticleWithKnownThreadSafe$ThreadSafe",

    // collections
    "java.util.Collections$SynchronizedCollection", // not true
    "java.util.Vector", // not true
    "somethig at the end")

  val classPatterns = List(
    "java.util.concurrent.ConcurrentHashMap.*",
    "java.util.Collections.SingletonList.*",
    "java.util.Collections.SynchronizedList.*",
    "java.util.Collections.SynchronizedRandomAccessList.*",
    "java.util.Collections.UnmodifiableList.*",
    "java.util.Collections.UnmodifiableRandomAccessList.*")
    
    
  val methods = List()
  /**
   * is the node,i.e., invocation, thread-safe?
   */
  override def apply(n: N): Boolean = threadSafeOnClosure(n) || super.apply(n)

  /**
   * is any invocation of this method thread-safe?
   */
  override def apply(m: M): Boolean = threadSafeOnClosure(m) || super.apply(m)

  /**
   * is any method of this class thread-safe?
   */
  override def apply(c: C): Boolean = threadSafeOnClosure(c) || super.apply(c)

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

object threadSafeOnClosure extends SelectorOfClassesAndMethods {

  val classes = Set(

    // used for ignoring string operations, including concatenation
    "java.lang.String",
    "java.lang.StringBuilder",
    "java.lang.StringBuffer",
    "java.lang.AbstractStringBuilder",

    "java.io.BufferedInputStream",
    "java.io.FilterInputStream",
    "java.io.BufferedWriter",
    "java.io.PrintStream",
    "java.io.File",
    "java.io.FileDescriptor",
    "java.io.FileInputStream",
    "java.io.FilePermission",
    "java.io.ObjectStreamClass",

    "java.io.FilePermission",

    "java.lang.Throwable",
    "java.lang.Shutdown",

    "java.security.AccessControlContext",
    "java.security.AccessControlException",
    "java.security.Permission",
    "java.security.Security",
    "java.security.BasicPermission",

    "java.util.Currency",
    "java.lang.SecurityManager",

    // for reflection
    "java.lang.Class",
    "java.lang.Class$EnclosingMethodInfo",
    "java.lang.Method",
    "java.lang.ClassLoader",

    // lock classes
    "java.util.concurrent.locks.ReentrantLock",
    "java.util.concurrent.ConcurrentHashMap$Segment",
    "java.util.concurrent.locks.AbstractQueuedSynchronizer",

    // date-time classes
    "java.util.Date",
    "java.util.Locale",
    "java.util.Timezone",
    "java.util.TimeZone",
    "java.util.GregorianCalendar",

    "java.util.Properties",

    // math
    "java.lang.Long",
    "java.lang.Integer",
    "java.lang.Character",
    "java.math.BigDecimal",
    "java.math.BigInteger",
    "java.lang.Boolean",

    // beans
    "java.beans.PropertyDescriptor",
    "java.beans.IndexedPropertyDescriptor",

    // regex Pattern class - it is thread-safe
    "java.util.regex.Pattern",

    "java.net.URL",

    // kind of coarse-grained
    "java.lang.System",

    // soft and weak reference
    "java.lang.ref.SoftReference",
    "java.lang.ref.WeakReference",

    // not very sure about this one
    "java.text.AttributedString",
    
    "javax.xml.parsers.SAXParserFactory",

    // for testing
    "particles.ParticleWithKnownThreadSafe$ThreadSafeOnClosure",

    // for coref
    "LBJ2.nlp.coref.CoreferenceTester",
    "LBJ2.classify.TestDiscrete",

    // for lucene4
    "org.apache.lucene.index.CompositeReader",

    "the end of the list")

  val classPatterns = List()

  val methods = List(

    // this shouldn't be here but there is a bug in wala:
    // "found a bug in the wala framework - it sees as the same weka.core.Attribute and 
    // java.text.NumberFormat$Field; the latter is of class AttributedCharacterIterator$Attribute - that might be the reason

    ("weka/core/Attribute", "equalsMsg"))

  /**
   * is it thread-safe on closure when called from caller
   */
  override def apply(caller: N): Boolean =
    caller.c(ThreadSafeOnClosure) != null || super.apply(caller.m)

  override def apply(c: C) = ZeroXInstanceKeys.isThrowable(c) || super.apply(c)

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