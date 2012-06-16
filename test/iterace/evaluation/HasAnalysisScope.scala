package iterace.evaluation

import iterace.pointeranalysis.AnalysisScopeBuilder

trait HasAnalysisScope {
  val analysisScope =  AnalysisScopeBuilder("walaExclusions.txt")
}