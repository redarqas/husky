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
      .options[Path](long = "files", short = "fs", help = "files to parse")
      .orNone
      .map(_.fold[Input](Input.StdIn)(Input.FileInput(_)))

  val prepare: Opts[String] =
    Opts
      .option[String](
        long = "prepare",
        short = "prep",
        help = "prepare function to apply to a chunck"
      )
      .withDefault("input => JSON.parse(input)")

  val transform: Opts[String] =
    Opts
      .argument[String](
        metavar = "transform function to apply to a prepared chunck"
      )
      .withDefault("input => input")

  val complete: Opts[String] =
    Opts
      .option[String](
        long = "complete",
        short = "comp",
        help = "complete function to apply to a transformed chunck"
      )
      .withDefault(
        """input => JSON.stringify(input, null, 2)"""
      )

  val threads: Opts[Int] =
    Opts
      .option[Int](
        long = "nb-threads",
        short = "n",
        help = "number of threads to use"
      )
      .withDefault(4)

  def main: Opts[IO[ExitCode]] =
    (in, prepare, transform, complete, threads)
      .mapN((i, p, t, c, nb) => Program.make(i, p, t, c, nb))
      .map(_.as(ExitCode.Success))
