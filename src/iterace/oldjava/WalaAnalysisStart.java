package iterace.oldjava;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultSSAInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.FileProvider;

public class WalaAnalysisStart {

	public final static String MAIN_METHOD = "main([Ljava/lang/String;)V";
	public Entrypoint entrypoint;
	public PointerAnalysis pointerAnalysis;
	public CallGraph callGraph;
	public String entryClass;
	public String entryMethod;
	public final List<String> binaryDependencies = new ArrayList<String>();
	public final List<String> jarDependencies = new ArrayList<String>();
	public final List<String> extensionBinaryDependencies = new ArrayList<String>();
	public PropagationCallGraphBuilder builder = null;
	public AnalysisCache cache;
	
	public WalaAnalysisStart() {
		
	}

	public static SSAPropagationCallGraphBuilder makeCFABuilder(AnalysisOptions options, AnalysisCache cache,
	    IClassHierarchy cha, AnalysisScope scope) {

		if (options == null) {
			throw new IllegalArgumentException("options is null");
		}
		Util.addDefaultSelectors(options, cha);
		Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);

		DefaultContextSelector appContextSelector = new DefaultContextSelector(options, cha);
		
		return new MyCGBuilder(cha, options, cache, appContextSelector, new DefaultSSAInterpreter(options, cache),
		    ZeroXInstanceKeys.SMUSH_STRINGS | ZeroXInstanceKeys.ALLOCATIONS);
		// ZeroXInstanceKeys.SMUSH_MANY |
		// ZeroXInstanceKeys.SMUSH_THROWABLES |
	}

	public void addBinaryDependency(String path) {
		this.binaryDependencies.add(path);
	}

	public void addExtensionBinaryDependency(String path) {
		this.extensionBinaryDependencies.add(path);
	}
	
	public void addJarFolderDependency(String path) {
	  File dir = new File(path);
	  String delim;
	  
	  if (path.endsWith("/"))
	    delim = "";
	  else
	    delim = "/";
	  
	  if (!dir.isDirectory())
	    return;
	  
	  String[] files = dir.list();
	  if (files == null)
	    return;
	  
	  for (String fileName : files) {
      if (fileName.endsWith(".jar"))
        addJarDependency(path + delim + fileName);
      else {
        File file = new File(fileName);
        if (file.isDirectory())
          addJarFolderDependency(file.getAbsolutePath());
      }
    }
	}

	public void addJarDependency(String file) {
		this.jarDependencies.add(file);
	}

	public void setup(String entryClass, String entryMethod) throws ClassHierarchyException, IllegalArgumentException,
	    CancelException, IOException {
		this.entryClass = entryClass;
		this.entryMethod = entryMethod;
		AnalysisScope scope = getAnalysisScope();
		scope.setExclusions(FileOfClasses.createFileOfClasses(new File("walaExclusions.txt")));

		IClassHierarchy cha = ClassHierarchy.make(scope);

		Set<Entrypoint> entrypoints = new HashSet<Entrypoint>();
		TypeReference typeReference = TypeReference.findOrCreate(scope.getLoader(AnalysisScope.APPLICATION),
		    TypeName.string2TypeName(entryClass));
		MethodReference methodReference = MethodReference.findOrCreate(typeReference,
		    entryMethod.substring(0, entryMethod.indexOf('(')), entryMethod.substring(entryMethod.indexOf('(')));
//		displayClassHierachy(cha);
		entrypoint = new DefaultEntrypoint(methodReference, cha);
		entrypoints.add(entrypoint);

		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		cache = new AnalysisCache();
		builder = makeCFABuilder(options, cache, cha, scope);

		callGraph = builder.makeCallGraph(options);
		pointerAnalysis = builder.getPointerAnalysis();
	}

	@SuppressWarnings("unused")
  private static void displayClassHierachy(IClassHierarchy cha) {
	  Iterator<IClass> iterator = cha.iterator();
		while (iterator.hasNext()) {
			IClass iClass = (IClass) iterator.next();
			if (iClass.getName().toString().equals("Lweka/clusterers/EM")) {
				System.out.println("FOUNT IT!!!!   " + iClass.getName());
				Collection<IMethod> allMethods = iClass.getAllMethods();
				for (IMethod iMethod : allMethods) {
					System.out.println(iMethod);
				}
			}
		}
  }

	private AnalysisScope getAnalysisScope() throws IOException {
		AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
		ClassLoader loader = WalaAnalysisStart.class.getClassLoader();

		// Add the the j2se jar files
		String[] stdlibs = WalaProperties.getJ2SEJarFiles();
		for (int i = 0; i < stdlibs.length; i++) {
			scope.addToScope(scope.getLoader(AnalysisScope.PRIMORDIAL), new JarFile(stdlibs[i]));
		}

		for (String directory : binaryDependencies) {
			File sd = FileProvider.getFile(directory, loader);
			assert sd.isDirectory();
			scope.addToScope(scope.getLoader(AnalysisScope.APPLICATION), new BinaryDirectoryTreeModule(sd));
		}
		for (String directory : extensionBinaryDependencies) {
			File sd = FileProvider.getFile(directory, loader);
			assert sd.isDirectory();
			scope.addToScope(scope.getLoader(AnalysisScope.EXTENSION), new BinaryDirectoryTreeModule(sd));
		}

		for (String path : jarDependencies) {
			Module M = FileProvider.getJarFileModule(path, loader);
			scope.addToScope(scope.getLoader(AnalysisScope.APPLICATION), M);
		}

		return scope;
	}
}