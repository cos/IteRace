/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package iterace.oldjava;


import iterace.LoopContextSelector;

import com.ibm.wala.analysis.reflection.ReflectionContextInterpreter;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultSSAInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DelegatingSSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.IClassHierarchy;


/**
 * nCFA Call graph builder
 */
public class MyCGBuilder extends SSAPropagationCallGraphBuilder {

  public MyCGBuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache, ContextSelector appContextSelector,
      SSAContextInterpreter appContextInterpreter, int instancePolicy) {

    super(cha, options, cache, new DefaultPointerKeyFactory());
    if (options == null) {
      throw new IllegalArgumentException("options is null");
    }

    SSAContextInterpreter defI = new DefaultSSAInterpreter(options, cache);
    defI = new DelegatingSSAContextInterpreter(ReflectionContextInterpreter.createReflectionContextInterpreter(cha, options, getAnalysisCache()), defI);
    SSAContextInterpreter contextInterpreter = new DelegatingSSAContextInterpreter(appContextInterpreter, defI);
    setContextInterpreter(contextInterpreter);
    
    ZeroXInstanceKeys zik = makeInstanceKeys(cha, options, contextInterpreter, instancePolicy);
    setContextSelector(new LoopContextSelector(zik));
    
    
//    setContextSelector(new CS(zik, options, cha));
    setInstanceKeys(zik);
  }
  /**
   * subclasses can override as desired
   */
  protected ZeroXInstanceKeys makeInstanceKeys(IClassHierarchy cha, AnalysisOptions options,
      SSAContextInterpreter contextInterpreter, int instancePolicy) {
    ZeroXInstanceKeys zik = new ZeroXInstanceKeys(options, cha, contextInterpreter, instancePolicy);
    return zik;
  }

}
