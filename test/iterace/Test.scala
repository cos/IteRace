package iterace;
import scala.collection.JavaConversions._
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.Spec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Test extends Spec with BeforeAndAfter {
  describe("A test test") {
    
    class Bla(x: Int) {      
    }
    val a = new Bla(10)
    
    object CaseBla {
      def unapply(b: Bla): Option[Bla] = Some(b)
    }
    
    a match {
      case CaseBla(x) => null
    }
    
    case class MyString(s: String) {}

    implicit def string2mystring(x: String): MyString = new MyString(x)
    implicit def mystring2string(x: MyString) = x.s
//
//    object Apply {
//      def unapply(s: MyString): Option[String] = Some(s)
//    }

    object Apply {
      def unapply[S <% MyString](s: S): Option[String] = Some(s.s)
    }

    val Apply(z) = "a"
  }
}