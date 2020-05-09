import Common._
import Dependencies._

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, GitVersioning)
  .commonSettings("jsq")
  .settings(
    libraryDependencies ++= mainDeps ++ testDeps,
    buildInfoKeys             := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, git.baseVersion, git.gitHeadCommit),
    buildInfoPackage          := "io.redcats.jsq",
    buildInfoUsePackageAsPath := true,
    githubOwner               := "redarqas",
    githubRepository          := "jsq",
    githubTokenSource         := TokenSource.GitConfig("github.token") || TokenSource.Environment("GITHUB_TOKEN"),
    resolvers += Resolver.githubPackages("redarqas")
  )
