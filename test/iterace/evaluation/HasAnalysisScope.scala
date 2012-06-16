package iterace.evaluation

import iterace.pointeranalysis.AnalysisScopeBuilder

trait HasAnalysisScope {
  val analysisScope =  AnalysisScopeBuilder("walaExclusions.txt")
  val entryClass:String
  val entryMethod = "main([Ljava/lang/String;)V"
}