import sbt.Keys._
import sbt.Project

object Common {

  implicit class ProjectFrom(project: Project) {

    def commonSettings(nameArg: String): Project = project.settings(
      name := nameArg,
      organization := "io.redcats",
      scalaVersion := "2.12.11"
    )
  }
}
