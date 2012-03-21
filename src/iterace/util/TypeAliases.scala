package iterace.util
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey
import com.ibm.wala.types.FieldReference
import com.ibm.wala.ssa.SSAInstruction
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction

trait TypeAliases {
  type SS = BasicBlockInContext[IExplodedBasicBlock]
  type V = Int
  type N = CGNode
  type P = LocalPointerKey
  type O = InstanceKey
  type F = FieldReference
  
  type I = SSAInstruction
  type PutI = SSAPutInstruction
  type AccessI = SSAFieldAccessInstruction
  
  type G = CallGraph
  type C = IClass
  type M = IMethod
}