package iterace

import scala.collection.immutable.TreeMap
import scala.collection.immutable.TreeSet
import scala.collection._

package object util {
  def toStringOrdering[K] = Ordering.by[K, String] { _.toString() }

  def toStringSortedMap[K, V](m: Map[K, V]) = TreeMap.empty(toStringOrdering[K]) ++ m
  implicit def mapWithToStringSoring[K, V](m: Map[K, V]) = new { def toStringSorted(): Map[K, V] = util.toStringSortedMap(m) }

  def toStringSortedSet[V](m: Iterable[V]) = TreeSet.empty(toStringOrdering[V]) ++ m
  implicit def setWithToStringSoring[V](m: Iterable[V]) = new { def toStringSorted(): Set[V] = util.toStringSortedSet(m) }
  
  def crossProduct[U,V](a: Traversable[U], b:Traversable[V]) = {
    a map (e => b.map {(e,_)}) flatten
  }
  
  def crossProduct[U,V](a: Set[U], b:Set[V]) = {
     a map (e => b.map {(e,_)}) flatten
  }
}