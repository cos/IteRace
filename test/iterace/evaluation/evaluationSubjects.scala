package iterace.evaluation

trait BHScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/barnesHut/bin")
  val entryClass = "LbarnesHut/ParallelBarneshut"
}
trait EM3DScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/em3d/bin")
  val entryClass = "Lem3d/parallelArray/Em3d"
}
trait JUnitScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/junit/bin")
  val entryClass = "Ljunit/tests/ParallelAllTests"
}
trait LuSearchScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/lusearch/bin")
  analysisScope.addBinaryDependency("../lib/parallelArray.mock")
  val entryClass = "Lorg/dacapo/lusearch/Search"
}
trait MonteCarloScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/montecarlo/bin")
  val entryClass = "Lmontecarlo/parallel/JGFMonteCarloBench"
  override val entryMethod = "JGFrun(I)V"
}
trait OldCorefScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/coref/bin");
  analysisScope.addJarDependency("../evaluation/coref/java_cup_runtime.jar");
  val entryClass = "LLBJ2/nlp/coref/ClusterMerger"
}
trait WEKAScope { this: HasAnalysisScope =>
  analysisScope.addJarDependency("../evaluation/weka/lib/java-cup.jar")
  analysisScope.addJarDependency("../evaluation/weka/lib/JFlex.jar")
  analysisScope.addJarDependency("../evaluation/weka/lib/junit.jar")
  analysisScope.addBinaryDependency("../evaluation/weka/bin")
  val entryClass = "Lweka/clusterers/EM"
}