package iterace.pointeranalysis;

import iterace.IteRace
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.List
import java.util.jar.JarFile
import com.ibm.wala.classLoader.BinaryDirectoryTreeModule
import com.ibm.wala.classLoader.JarFileModule
import com.ibm.wala.classLoader.Module
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.util.config.FileOfClasses
import com.ibm.wala.util.io.FileProvider
import iterace.util.debug
import scala.collection._
import scala.collection.JavaConverters._

object AnalysisScopeBuilder {
  def apply = new AnalysisScopeBuilder(if (System.getProperty("os.name").contains("Linux"))
    "/usr/lib/jvm/java-6-sun-1.6.0.26/jre/lib/rt.jar"
  else
    "/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar")
}

class AnalysisScopeBuilder(jreLibPath: String) {

  val UNDER_ECLIPSE = true;

  val binaryDependencies = mutable.MutableList[String]()
  val jarDependencies = mutable.MutableList[String]()
  val extensionBinaryDependencies = mutable.MutableList[String]()
  val scope = AnalysisScope.createJavaAnalysisScope()

  scope.addToScope(scope.getLoader(AnalysisScope.PRIMORDIAL), new JarFile(jreLibPath));

  def getFile(path: String) =
    if (UNDER_ECLIPSE)
      new FileProvider().getFile(path, getLoader())
    else
      new File(path)

  def addBinaryDependency(directory: String) {
    debug("Binary: " + directory);
    val sd = getFile(directory);
    assert(sd.isDirectory())
    scope.addToScope(scope.getLoader(AnalysisScope.APPLICATION),
      new BinaryDirectoryTreeModule(sd));
  }

  def getLoader() = this.getClass().getClassLoader();

  def addExtensionBinaryDependency(directory: String) {
    debug("Binary extension: " + directory);
    val sd = getFile(directory);
    assert(sd.isDirectory())
    scope.addToScope(scope.getLoader(AnalysisScope.EXTENSION),
      new BinaryDirectoryTreeModule(sd));
  }

  def addJarFolderDependency(path: String) {
    debug("Jar folder: " + path);
    val dir = getFile(path);

    val delim = if (path.endsWith("/")) "" else "/"

    if (!dir.isDirectory())
      return ;

    val files = dir.list();
    if (files == null) return

    for (fileName <- files) {
      if (fileName.endsWith(".jar"))
        addJarDependency(path + delim + fileName);
      else {
        val file = new File(fileName)
        if (file.isDirectory())
          addJarFolderDependency(file.getAbsolutePath());
      }
    }
  }

  def addJarDependency(file: String) {
    debug("Jar: " + file);
    val M = if (UNDER_ECLIPSE)
      new FileProvider().getJarFileModule(file, getLoader());
    else
      new JarFileModule(new JarFile(file, true));

    scope.addToScope(scope.getLoader(AnalysisScope.APPLICATION), M);
  }

  def setExclusionsFile(file: String) {
    scope.setExclusions(FileOfClasses.createFileOfClasses(new File(file)));
  }
}