ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "scala-cats-work",
    // Добавляем библиотеку Cats Effect
    libraryDependencies ++= Seq(
      "org.testcontainers" % "postgresql" % "1.19.0",
      "org.typelevel"     %% "cats-effect" % "3.5.0",
      "org.tpolecat"      %% "doobie-postgres" % "1.0.0-RC4", // Специально для Постгреса
      "co.fs2" %% "fs2-core" % "3.9.3",
    )
  )