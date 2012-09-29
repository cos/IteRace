package iterace.evaluation

import wala.AnalysisScope._

trait BHScope extends SubjectScope {
  dependencies += Dependency("../evaluation/barnesHut/bin")
  val entryClass = "LbarnesHut/ParallelBarneshut"
}
trait EM3DScope extends SubjectScope {
  dependencies += Dependency("../evaluation/em3d/bin")
  val entryClass = "Lem3d/parallelArray/Em3d"
}
trait JUnitScope extends SubjectScope {
  dependencies += Dependency("../evaluation/junit/bin")
  val entryClass = "Ljunit/tests/ParallelAllTests"
}
trait LuSearchScope extends SubjectScope {
  dependencies ++= Seq(Dependency("../evaluation/lusearch/bin"), Dependency("../lib/parallelArray.mock"))
  val entryClass = "Lorg/dacapo/lusearch/Search"
}
trait MonteCarloScope extends SubjectScope {
  dependencies ++= Seq(Dependency("../evaluation/montecarlo/bin"))
  val entryClass = "Lmontecarlo/parallel/JGFMonteCarloBench"
  override val entryMethod = "JGFrun(I)V"
}
trait OldCorefScope extends SubjectScope {
  dependencies ++= Seq(
    Dependency("../evaluation/coref/bin"),
    Dependency("../evaluation/coref/java_cup_runtime.jar", DependencyNature.Jar))

  val entryClass = "LLBJ2/nlp/coref/ClusterMerger"
}
trait WEKAScope extends SubjectScope {
  val entryClass = "Lweka/clusterers/EM"

  dependencies ++= Seq(
    Dependency("../evaluation/weka/lib/java-cup.jar", DependencyNature.Jar),
    Dependency("../evaluation/weka/lib/JFlex.jar", DependencyNature.Jar),
    Dependency("../evaluation/weka/lib/junit.jar", DependencyNature.Jar),
    Dependency("../evaluation/weka/bin"))
}