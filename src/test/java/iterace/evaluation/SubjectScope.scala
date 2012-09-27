package iterace.evaluation

import wala.AnalysisScopeBuilder
import com.typesafe.config.ConfigFactory

trait SubjectScope {
  //TODO: get the conf out of here
  val conf = ConfigFactory.load("local.conf")
  var analysisScope = AnalysisScopeBuilder(conf.getString("wala.jre-lib-path"), "walaExclusions.txt");

  val entryClass: String
  val entryMethod = "main([Ljava/lang/String;)V"
}