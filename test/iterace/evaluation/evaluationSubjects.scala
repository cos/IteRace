package iterace.evaluation

trait BHScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/barnesHut/bin")
}
trait EM3DScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/em3d/bin")
}
trait JUnitScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/junit/bin")
}
trait LuSearchScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/lusearch/bin")
  analysisScope.addBinaryDependency("../lib/parallelArray.mock")
}
trait MonteCarloScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/montecarlo/bin")
}
trait OldCorefScope { this: HasAnalysisScope =>
  analysisScope.addBinaryDependency("../evaluation/coref/bin");
  analysisScope.addJarDependency("../evaluation/coref/java_cup_runtime.jar");
}
trait WEKAScope { this: HasAnalysisScope =>
  analysisScope.addJarDependency("../evaluation/weka/lib/java-cup.jar")
  analysisScope.addJarDependency("../evaluation/weka/lib/JFlex.jar")
  analysisScope.addJarDependency("../evaluation/weka/lib/junit.jar")
  analysisScope.addBinaryDependency("../evaluation/weka/bin")
}