
libraryDependencies += "com.typesafe" % "config" % "0.5.+"

libraryDependencies += "org.scalatest" % "scalatest_2.10.0" % "2.0.M5" % "test"

libraryDependencies += "net.debasishg" % "sjson_2.9.1" % "0.17"

unmanagedSourceDirectories in Test <+= sourceDirectory / "subjects"

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true

mainClass := Some("iterace.IteRace")

javaOptions += "-Xmx4G" 
