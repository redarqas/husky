import com.softwaremill.SbtSoftwareMill.autoImport.{commonSmlBuildSettings, wartRemoverSettings}
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import sbt.Keys._
import sbt.Project

object Common {
  implicit class ProjectFrom(project: Project) {
    def commonSettings(nameArg: String): Project = project.settings(
      name              := nameArg,
      organization      := "io.redcats",
      scalaVersion      := "2.13.1",
      scalafmtOnCompile := true,
      commonSmlBuildSettings,
      wartRemoverSettings,
      scalacOptions ++= Seq(
        "-Ymacro-annotations",
        "-Xfatal-warnings"
      )
    )
  }
}
