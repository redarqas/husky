package org.redarqas.sq

import cats.effect.IO
import fs2.Stream
import cats.Show
import io.circe.*
import fs2.data.json.circe.*
import cats.syntax.all.*
import scala.io.StdIn
import fs2.io.file.{Files, Flags, Path}
import fs2.data.json.*
import fs2.data.json.circe.*
import fs2.Fallible
import cats.effect.std.Console

trait Controller:
  def printables: Stream[IO, String]

object Controller:
  def loop[T](stream: fs2.Stream[IO, T]): fs2.Stream[IO, T] =
    stream.handleErrorWith(t =>
      fs2.Stream.eval(Console[IO].error(s"${t.getMessage}")) >> loop(stream)
    )
  private def parts(input: Input) = input match
    case Input.FileInput(paths) =>
      paths.reduceMap(p =>
        Files[IO]
          .readAll(Path.fromNioPath(p), 4096, Flags.Read)
          .through(fs2.text.utf8Decode)
          .through(tokens[IO, String])
          .through(ast.values[IO, Json])
          .map(_.noSpaces)
      )
    case Input.StdIn =>
      val reader = fs2.io
        .stdin[IO](bufSize = 4096)
        .through(fs2.text.utf8Decode)
        .through(tokens[IO, String])
        .through(ast.values[IO, Json])
        .map(_.noSpaces)
      loop(reader)

  def of[T: Show](
      input: Input,
      boundary: Boundary[Stream[IO, String], T]
  ): Controller =
    new:
      def printables: Stream[IO, String] =
        boundary
          .outputs(parts(input))
          .map(_.show)
