package iterace.util
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.ipa.callgraph.Context
import com.ibm.wala.ssa.IR
import scala.collection.JavaConversions._

trait WALAConversionsForN { self: WALAConversions =>

  object N {
    def unapply(n: N): Option[(Context, M)] = {
      Some(n.getContext(), n.getMethod())
    }
  }

  implicit def enhanceN(n: N) = new {
    def prettyPrint: String = 
      n.getMethod().prettyPrint
      
    def instructions = 
      n.getIR().getInstructions()
      
    def getV(name: String): V = 
      valuesForVariableName(name).head
      
    def valuesForVariableName(name: String): Iterable[V] = 
      instructions.map(i => S(n, i).valuesForVariableName(name).toSet).reduce(_ ++ _)
      
    def variableNames(value: V): Set[String] = 
      instructions.map(i => S(n, i).variableNames(value).toSet).reduce(_ ++ _)
      
    def ir: IR = 
      n.getIR()
  }
}