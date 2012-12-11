
libraryDependencies += "com.typesafe" % "config" % "0.5.+"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0.M4" % "test"

libraryDependencies += "net.debasishg" % "sjson" % "0.15"

unmanagedSourceDirectories in Test <+= sourceDirectory / "subjects"

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true
