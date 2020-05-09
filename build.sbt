import Common._
import Dependencies._

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, GitVersioning)
  .commonSettings("husky")
  .settings(
    libraryDependencies ++= mainDeps ++ testDeps,
    buildInfoKeys             := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, git.baseVersion, git.gitHeadCommit),
    buildInfoPackage          := "io.redcats.husky",
    buildInfoUsePackageAsPath := true,
    githubOwner               := "redarqas",
    githubRepository          := "bunny",
    githubTokenSource         := TokenSource.GitConfig("github.token") || TokenSource.Environment("GITHUB_TOKEN"),
    resolvers += Resolver.githubPackages("redarqas")
  )
