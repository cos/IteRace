package iterace.datastructure

import scala.collection.Set

import wala.WALAConversions._

trait SelectorOfClassesAndMethods { // I should find a better name for this
  def classes: Set[String]
  def classPatterns: List[String]

  lazy val classesFullName = classes map { "L" + _.replace(".", "/") }
  lazy val classesPatterns = classPatterns map { "L" + _ }

  def apply(c: C): Boolean = classesFullName.contains(c.getName.toString) || (classesPatterns exists { c.getName.toString matches _ })

  def apply(m: M): Boolean = apply(m.getDeclaringClass())

  def apply(n: N): Boolean = apply(n.m)
}