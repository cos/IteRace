
libraryDependencies += "com.typesafe" % "config" % "0.5.+"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0.M4" % "test"

libraryDependencies += "net.debasishg" % "sjson_2.8.1" % "0.9.1"

unmanagedSourceDirectories in Test <+= sourceDirectory / "subjects"

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true
