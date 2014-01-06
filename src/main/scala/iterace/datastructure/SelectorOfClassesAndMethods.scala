package iterace.datastructure

import scala.collection.Set

import edu.illinois.wala.Facade._

trait SelectorOfClassesAndMethods { // I should find a better name for this

  def classes: Set[String]
  def classPatterns: List[String]
  def methods: List[(String, String)]

  lazy val classesFullName = classes map { "L" + _.replace(".", "/") }
  lazy val classesPatterns = classPatterns map { "L" + _ }

  def apply(c: C): Boolean =
    classesFullName.contains(c.getName.toString) ||
      (classesPatterns exists { c.getName.toString matches _ })

  def apply(m: M): Boolean =
    apply(m.getDeclaringClass()) ||
      methods.exists({
        case (cPattern, mPattern) => m.getDeclaringClass.getName.toString.matches(cPattern) && m.getName.toString.matches(mPattern)
      })

  def apply(n: N): Boolean = apply(n.m)
}