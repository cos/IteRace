package iterace
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.classLoader.IField
import com.ibm.wala.ssa.SSAInstruction
import com.ibm.wala.ipa.callgraph.CallGraph
import scala.collection.JavaConversions._
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey
import com.ibm.wala.ipa.callgraph.propagation.PointerKey
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey
import com.ibm.wala.ipa.callgraph.Context
import com.ibm.wala.ipa.callgraph.Context
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.util.strings.Atom
import com.ibm.wala.types.Descriptor
import iterace.LoopContextSelector.LoopContext
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.types.FieldReference
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.classLoader.ShrikeBTMethod
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import com.ibm.wala.ssa.SSAInvokeInstruction
import com.ibm.wala.ssa.SSAPhiInstruction
import com.ibm.wala.ssa.IR

object WALAConversions {
  trait Named {
    def name(): String
  }

  implicit def imethod2named(m: IMethod): Named = new Named {
    def name() = m.getSelector().getName().toString()
  }
  implicit def n(n: CGNode): Named = new Named {
    def name() = n.getMethod().name
  }
  implicit def c2named(c: IClass): Named = new Named {
    def name() = c.getName().getClassName().toString()
  }

  trait PrettyPrintable {
    def prettyPrint(): String
  }
  implicit def o2prettyprintable(o: O): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = {
      o match {
        case o: AllocationSiteInNode => printCodeLocation(o.getNode(), o.getSite().getProgramCounter())
        case _ => o.toString()
      }
    }
  }

  implicit def n2prettyprintable(n: CGNode): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = {
      n.getMethod().prettyPrint
    }
  }

  implicit def m2prettyprintable(m: IMethod): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = {
      val packageName = m.getDeclaringClass().getName().getPackage().toString().replace('/', '.')
      packageName + "." + m.getDeclaringClass().name + "." + m.name
    }
  }

  // call graph node
  object N {
    def unapply(n: N): Option[(Context, M)] = {
      Some(n.getContext(), n.getMethod())
    }
  }
  implicit def nWithValuesForVariableNames(n: N) = new {
    def valuesForVariableName(name: String): Iterable[V] = {
      n.getIR().getInstructions().map(i => S(n, i).valuesForVariableName(name).toSet).reduce(_ ++ _)
    }
    def getV(name: String): V = valuesForVariableName(name).head
    def variableNames(value: V): Set[String] = {
      n.getIR().getInstructions().map(i => S(n, i).variableNames(value).toSet).reduce(_ ++ _)
    }
  }
  implicit def nWithIR(n: N) = new {
    def ir(): IR = {
      n.getIR()
    }
  }

  // method
  object M {
    def unapply(m: M): Option[(IClass, String)] = {
      Some(m.getDeclaringClass(), m.getSelector().toString())
    }
  }

  // class, iclass
  object C {
    def unapply(c: C): Option[(String, String)] = {
      Some(c.getName().getPackage().toString(), c.getName().getClassName().toString())
    }
  }

  // "Pointer Local" - equivalent of LocalPointerKey
  object P {
    def apply(n: N, v: Int) = {
      new P(n, v)
    }
    def unapply(p: P): Option[(CGNode, Int)] = {
      Some(p.getNode(), p.getValueNumber())
    }
  }
  implicit def p2nv(p: P) = new {
    def n = p.getNode()
    def v = p.getValueNumber()
  }
  implicit def p2def(p: P) = new {
    def getDef() = p.n.getDU().getDef(p.v)
  }
  implicit def pWithUses(p: P) = new {
    def getUses(): Iterable[I] = {
      p.n.getDU().getUses(p.v).toIterable
    }
    /**
     * Gets all uses of this pointer that write to a field of it
     */
    def getPuts(): Iterable[SSAPutInstruction] = {
      (for(i <- getUses() if i.isInstanceOf[SSAPutInstruction] && i.asInstanceOf[SSAPutInstruction].getRef() == p.v) 
        yield i.asInstanceOf[SSAPutInstruction])
    }
  }
  implicit def pToVariableNames(p: P) = new {
    def names(): Iterable[String] = {
      (p.n.getIR().iterateAllInstructions().toIterable).map(i => S(p.n, i).variableNames(p.v)).filter(s => s != null).flatten.toSet
    }
  }
  implicit def p2prettyprintable(p: P): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = {
      p.n.prettyPrint() + "v"+ p.v+"=" + (if(!p.names().isEmpty) p.names.reduce(_+","+_) else "")
    }
  }

  def printCodeLocation(n: N, bytecodeIndex: Int): String = {
    printCodeLocation(n.getMethod(), bytecodeIndex)
  }

  def printCodeLocation(m: IMethod, bytecodeIndex: Int): String = {
    val sm = m.asInstanceOf[ShrikeBTMethod]
    val lineNo = sm.getLineNumber(bytecodeIndex)
    val className = m.getDeclaringClass().getName().getClassName().toString()
    "" + m.prettyPrint + "(" + className + ".java:" + lineNo + ")"
  }

  implicit def iWithEasyUses(i: SSAInstruction) = new {
    def uses: Iterable[V] = Stream.range(0, i.getNumberOfUses()).map(index => { i.getUse(index) })
  }

  // statement
  object S {
    def unapply(b: BasicBlockInContext[IExplodedBasicBlock]): Option[(N, I)] = {
      Some(b.getNode(), b.getDelegate().getInstruction())
    }
    def apply[T <: I](n: N, i: T) = new S(n, i)
  }
  class S[T <: I](n: N, val i: T) extends PrettyPrintable {
    def prettyPrint() = {
      printCodeLocation()
    }
    def printCodeLocation(): String = {
      val ssaInstructionNo = S(n, i).irNo
      val m = n.getMethod().asInstanceOf[ShrikeBTMethod]
      val bytecodeIndex = m.getBytecodeIndex(ssaInstructionNo)
      WALAConversions.printCodeLocation(m, bytecodeIndex)
    }
    def irNo = n.getIR().getInstructions().findIndexOf(ii => i == ii)

    def valuesForVariableName(name: String): Iterable[V] = {
      val maxValue = n.getIR().getSymbolTable().getMaxValueNumber();
      Stream.range(1, maxValue + 1).filter(v => {
        val names = n.getIR().getLocalNames(irNo, v)
        if (names != null) {
          names.contains(name)
        } else
          false
      })
    }

    def variableNames(v: Int): Iterable[String] = {
      val names = n.getIR().getLocalNames(irNo, v)
      if(names != null) names else Iterable()
    }
  }

  object O {
    def unapply(o: O): Option[(N, I)] = {
      o match {
        case o: AllocationSiteInNode => Some(o.getNode(), o.getNode().getIR().getNew(o.getSite()))
        case _ => None
      }
    }
  }

  type V = Int
  type N = CGNode
  type P = LocalPointerKey
  type O = InstanceKey
  type F = FieldReference
  type I = SSAInstruction
  type G = CallGraph
  type C = IClass
  type M = IMethod

  //  	public static String variableName(Integer v, CGNode cgNode,
  //			int ssaInstructionNo) {
  //		String[] localNames;
  //		try {
  //			localNames = cgNode.getIR().getLocalNames(ssaInstructionNo,
  //					v);
  //		} catch (Exception e) {
  //			localNames = null;
  //		} catch (UnimplementedError e) {
  //			localNames = null;
  //		} 
  //		String variableName = null;
  //		if (localNames != null && localNames.length > 0)
  //			variableName = localNames[0];
  //		return variableName;
  //	}

  // access instructions of Some(object, field, is_write)
  //  object AccessI {
  //    def unapply(i: I):Option[(F, V)] = {
  //      i match {
  //        case rI: SSAGetInstruction => Some(rI.getDeclaredField(), rI.getUse(0))
  //        case wI: SSAPutInstruction => Some(wI.getDeclaredField(), wI.getDef())
  //        case _ => None
  //      }
  //    }
  //  }
}