package iterace.evaluation

import iterace.pointeranalysis.AnalysisScopeBuilder

trait SubjectScope {
  val analysisScope =  AnalysisScopeBuilder("walaExclusions.txt")
  val entryClass:String
  val entryMethod = "main([Ljava/lang/String;)V"
}