package iterace.datastructure

import scala.collection.Seq
import scala.collection.Set
import scala.collection.immutable

import com.ibm.wala.analysis.reflection.InstanceKeyWithNode
import com.ibm.wala.ipa.callgraph.ContextKey
import com.ibm.wala.ipa.callgraph.propagation.ContainerUtil
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys

import iterace.pointeranalysis.ThreadSafeOnClosure
import edu.illinois.wala.S
import wala.WALAConversions._

object generatesSafeObjects extends SelectorOfClassesAndMethods {
  /**
   * classes that generate thread-safe objects, i.e. the objects that
   * are instantiated inside this class are thread-safe
   */
  val classes = Set(
    // kind of coarse-grained
    "java.lang.System",
    "java.security.Security",

    "java.io.ObjectStreamClass", // not true

    // for testing
    "particles.ParticleWithKnownThreadSafe$ThreadSafeParticleGenerator")

  val classPatterns = List("net.sourceforge.cilib.type.types.container.Vector.*")
  
  val methods = List((".*java.util.Collections.*", "synchronized.*"))
}

object movesObjectsAround extends SelectorOfClassesAndMethods {
  val classes = Set(
    "java.util.regex.Pattern",
    // collections that are safe
    "java.util.Collections$SynchronizedCollection",
    "java.util.Collections$SynchronizedList",
    "java.util.Collections$SynchronizedSet",
    "java.util.Collections$SynchronizedSortedSet",
    "java.util.Vector",

    // other collections
    "java.util.HashMap",

    "java.io.ObjectStreamClass",

    // data structures used by WEKA
    //    "weka.core.SparseInstance",

    // soft and weak reference
    "java.lang.ref.SoftReference",
    "java.lang.ref.WeakReference")
    
  val classPatterns = List()
  
  val methods = List()

  override def apply(c: C): Boolean = super.apply(c) || ContainerUtil.isContainer(c)
}