import sbt._

import esentire.Dependencies._
import esentire.Versions
import com.typesafe.sbt.packager.docker._

scalaVersion in ThisBuild := esentire.Versions.scala

name := "Skeleton" // FIXME
version := "1.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  Logging.avsl
)

configs(IntegrationTest)

EsentireBuild.settings

enablePlugins(AshScriptPlugin)
enablePlugins(DockerPlugin)

packageName in Docker := s"registry.internal/ata/${packageName.value}"
maintainer in Docker := "Rob McLeod"
dockerBaseImage := "registry.internal/common/zulu:2"
version in Docker := (if (sys.env.getOrElse("BRANCH_NAME", "") == "master")
                        sys.env.getOrElse("BUILD_NUMBER", "dev-latest")
                      else (sys.env.getOrElse("BRANCH_NAME", "dev").split("/").last) + "-latest")
dockerUpdateLatest := sys.env.contains("BUILD_NUMBER") && (sys.env.getOrElse("BRANCH_NAME", "dev") == "master")

dockerCommands ++= Seq(
  Cmd("LABEL", s"BUILD=${sys.env.getOrElse("BUILD_NUMBER", "local")}"),
  Cmd("LABEL", s"GIT_COMMIT=${sys.env.getOrElse("GIT_COMMIT", "local")}")
)

scalacOptions in (Compile, console) := Seq()

(applicationName in Deployment) := packageName.value
(containerImage in Deployment) := (packageName in Docker).value + ":" + (version in Docker).value
(namespace in Deployment) := "ata"
(containerPort in Deployment) := 8080
(port in Deployment) := 8080
(nodePort in Deployment) := 12345 // FIXME
