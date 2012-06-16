package iterace.evaluation

trait BHScope {this:HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/barnesHut/bin");
} 
trait EM3DScope {this:HasAnalysisScope => 
  analysisScope.addBinaryDependency("../evaluation/em3d/bin")
}