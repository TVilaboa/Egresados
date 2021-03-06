name := """ismet-scalongo-seed"""

version := "1.0"

lazy val `scalongo` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(jdbc, cache, ws, filters, specs2 % Test,"org.jsoup" % "jsoup" % "1.9.2",
  "com.github.tototoshi" %% "scala-csv" % "1.2.2")

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)


resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "1.0.1"

libraryDependencies += "com.github.t3hnar" % "scala-bcrypt_2.11" % "2.5"

libraryDependencies += "org.ehcache" % "ehcache" % "3.3.1"

//libraryDependencies += "com.enragedginger" %% "akka-quartz-scheduler" % "1.6.0-akka-2.4.x"

fork in run := false