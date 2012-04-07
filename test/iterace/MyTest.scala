package iterace
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TestName
import scala.reflect.BeanProperty

class MyTest extends JavaTest {
 
	@Test def bla = {
	  println(testName.getMethodName())
	}
	@Test def blabla() = {}
}