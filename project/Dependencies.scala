import sbt._

object Dependencies {
  val circeVersion = "0.13.0"

  lazy val mainDeps = Seq(
    "org.graalvm.sdk" % "graal-sdk"       % "20.0.0" % Provided,
    "org.typelevel"   %% "cats-effect"    % "2.1.1",
    "com.monovore"    %% "decline"        % "1.0.0",
    "com.lihaoyi"     %% "fansi"          % "0.2.9",
    "com.lihaoyi"     %% "os-lib"         % "0.6.3",
    "com.monovore"    %% "decline"        % "1.0.0",
    "com.monovore"    %% "decline-effect" % "1.0.0",
    "co.fs2"          %% "fs2-core"       % "2.2.1",
    "co.fs2"          %% "fs2-io"         % "2.2.1",
    "io.circe"        %% "circe-core"     % circeVersion,
    "io.circe"        %% "circe-parser"   % circeVersion,
    "io.circe"        %% "circe-generic"  % circeVersion
  )

  lazy val testDeps = Seq("org.scalatest" %% "scalatest" % "3.1.0" % Test)
}
