package iterace.pointeranalysis;

import iterace.IteRace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.FileProvider;
import iterace.util.debug;

public class AnalysisScopeBuilder {
	
  public static Boolean UNDER_ECLIPSE = true;
	
	public List<String> binaryDependencies = new ArrayList<String>();
	public List<String> jarDependencies = new ArrayList<String>();;
	public List<String> extensionBinaryDependencies = new ArrayList<String>();;
	private AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();;

	public AnalysisScopeBuilder(String jreLibPath) throws IllegalArgumentException, IOException {
		System.out.println(jreLibPath);
		scope.addToScope(scope.getLoader(AnalysisScope.PRIMORDIAL), new JarFile(jreLibPath));
	}
	
	public File getFile(String path) throws IOException {
		if(UNDER_ECLIPSE) 
			return new FileProvider().getFile(path, getLoader());
		else 
			return new File(path);
	}

	public void addBinaryDependency(String directory) throws IOException {
		debug("Binary: "+directory);
	  File sd = getFile(directory);
		assert sd.isDirectory();
		scope.addToScope(scope.getLoader(AnalysisScope.APPLICATION), new BinaryDirectoryTreeModule(sd));
	}

	private void debug(String string) {
		debug.display(string);
	}

	private ClassLoader getLoader() {
		return this.getClass().getClassLoader();
	}
	
	public void addExtensionBinaryDependency(String directory) throws IOException {
		debug("Binary extension: "+directory);
		File sd = getFile(directory);
		assert sd.isDirectory();
		scope.addToScope(scope.getLoader(AnalysisScope.EXTENSION), new BinaryDirectoryTreeModule(sd));
	}
	
	public void addJarFolderDependency(String path) throws IOException {
    debug("Jar folder: "+path);
	  File dir = getFile(path);
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

	public void addJarDependency(String file) throws IOException {
		debug("Jar: "+file);
		Module M;
		if(UNDER_ECLIPSE)
			M = new FileProvider().getJarFileModule(file, getLoader());
		else
			M= new JarFileModule(new JarFile(file, true));
		
		scope.addToScope(scope.getLoader(AnalysisScope.APPLICATION), M);
	}
	
	public AnalysisScope getAnalysisScope() throws IOException {
		return scope;
	}
	
	public void setExclusionsFile(String file) throws IOException {
		scope.setExclusions(FileOfClasses.createFileOfClasses(new File(file)));
	}
}