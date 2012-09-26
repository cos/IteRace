package iterace.pointeranalysis

import com.ibm.wala.analysis.reflection.ReflectionContextInterpreter
import com.ibm.wala.ipa.callgraph.AnalysisCache
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.callgraph.ContextSelector
import com.ibm.wala.ipa.callgraph.propagation.{ SSAContextInterpreter, SSAPropagationCallGraphBuilder, InstanceKeyFactory }
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultSSAInterpreter
import com.ibm.wala.ipa.callgraph.propagation.cfa.DelegatingSSAContextInterpreter
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector
import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.ipa.callgraph.impl.Util;
import iterace.IteRaceOption

class CallGraphBuilder(
  options: AnalysisOptions,
  cache: AnalysisCache,
  cha: IClassHierarchy,
  scope: AnalysisScope,
  iteRaceOptions: Set[IteRaceOption])
  extends SSAPropagationCallGraphBuilder(cha, options, cache, new DefaultPointerKeyFactory()) {

  Util.addDefaultSelectors(options, cha);
  Util.addDefaultBypassLogic(options, scope, classOf[Util].getClassLoader(), cha);

  val instancePolicy =
    ZeroXInstanceKeys.SMUSH_STRINGS |
      ZeroXInstanceKeys.ALLOCATIONS |
      ZeroXInstanceKeys.SMUSH_THROWABLES
  //		  		ZeroXInstanceKeys.SMUSH_MANY |

  val defaultInterpreter = new DefaultSSAInterpreter(options, cache)

  val reflectionInterpreter = new DelegatingSSAContextInterpreter(
    ReflectionContextInterpreter.createReflectionContextInterpreter(cha, options, cache), defaultInterpreter)

  val contextInterpreter = new DelegatingSSAContextInterpreter(
    defaultInterpreter, reflectionInterpreter)

  this.setContextInterpreter(contextInterpreter)

  val zik: ZeroXInstanceKeys = new ZeroXInstanceKeys(options, cha, contextInterpreter, instancePolicy)
  setContextSelector(new LoopContextSelector(iteRaceOptions, zik))
  setInstanceKeys(zik);
}