package iterace.evaluation

import com.typesafe.config.ConfigFactory
import wala.AnalysisScope._
import wala.Dependency

trait SubjectScope { 
  val dependencies = collection.mutable.Set(Dependency("../ParallelArray-mock"))
  
  val entryClass: String
  val entryMethod = "main([Ljava/lang/String;)V"
}