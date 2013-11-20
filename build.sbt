
resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

unmanagedSourceDirectories in Test <+= sourceDirectory / "subjects"

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true

mainClass := Some("iterace.IteRace")

javaOptions += "-Xmx4G" 

libraryDependencies ++= Seq(
"com.typesafe" % "config" % "0.5.+",
"org.scalatest" % "scalatest_2.10.0" % "2.0.M5" % "test",
"net.debasishg" % "sjson_2.10" % "0.19",
"com.ibm.wala" % "com.ibm.wala.util" % "1.3.4-SNAPSHOT",
"com.ibm.wala" % "com.ibm.wala.shrike" % "1.3.4-SNAPSHOT",
"com.ibm.wala" % "com.ibm.wala.core" % "1.3.4-SNAPSHOT",
"University of Illinois" %% "walafacade" % "0.1",
"University of Illinois" %% "util" % "0.1",
"University of Illinois" %% "parallelarraymock" % "0.1")