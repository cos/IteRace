// used http://michid.wordpress.com/2009/02/23/function_mem/
package iterace

object memoize {
  def apply[A, B](f: A => B) = new MemoizedFunction(f)

  class MemoizedFunction[-A, +B](f: A => B) extends (A => B) {
    import scala.collection.mutable
    private[this] val vals = mutable.Map.empty[A, B]
    def apply(x: A): B = {
      println(vals)
      vals.getOrElseUpdate(x, f(x))
    }
  }
}

object memoizeRec {
  def apply[A, B](f: (A => B) => (A => B)): A => B = {  
    lazy val g: A => B = memoize(f(g)(_))
    g
  }
}

//  some testing code
//    def fact(f: Int => Int) = {
//      (n: Int) => { println(n) ; if (n == 0) 1 else n * f(n-1) } 
//    }
//    
//    def main(args: Array[String]) {
//      val smth=memoizeRec(fact)
//      println(smth(3))
//      println(smth(10))
//    }