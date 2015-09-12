organization := "org.beybunproject"

name := "xml-content-verifier"

version := "0.1"

resolvers += Resolver.mavenLocal

javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8")

javacOptions in (doc) ++= Seq("-source", "1.8")

resolvers += "Eid public repository" at "http://193.140.74.199:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

crossPaths := false

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "net.sf.saxon" % "Saxon-HE" % "9.6.0-3",
  "org.scala-lang" % "scala-library" % "2.11.7" % Test,
  "org.specs2" % "specs2_2.11" % "2.3.12" % Test
)

publishTo := Some("eid releases" at "http://193.140.74.199:8081/nexus/content/repositories/releases")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
