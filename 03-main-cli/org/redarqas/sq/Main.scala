package org.redarqas.sq

import cats.effect.IO
import cats.effect.ExitCode
import com.monovore.decline.Opts
import java.nio.file.Path
import com.monovore.decline.effect.CommandIOApp
import cats.syntax.all.*

object Main
    extends CommandIOApp(
      name = "sq",
      header = "jq using js",
      version = "1.0.0"
    ):

  val in: Opts[Input] =
    Opts
      .argument[Path](metavar = "file")
      .orNone
      .map(_.fold[Input](Input.StdIn)(Input.FileInput(_)))

  val prepare: Opts[String] =
    Opts
      .argument[String](metavar = "prepare function")
      .withDefault("input => JSON.parse(input)")

  val transform: Opts[String] =
    Opts
      .argument[String](metavar = "transform function")
      .withDefault("input => input")

  val complete: Opts[String] =
    Opts
      .argument[String](metavar = "complete function")
      .withDefault("input => input")

  val threads: Opts[Int] =
    Opts
      .argument[Int](metavar = "number of threads to use")
      .withDefault(4)

  def main: Opts[IO[ExitCode]] =
    (in, prepare, transform, complete, threads)
      .mapN((i, p, t, c, nb) => Program.make(i, p, t, c, nb))
      .map(_.as(ExitCode.Success))
