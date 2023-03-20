package org.redarqas.sq

import cats.effect.IO
import fs2.Stream
import cats.Show
import cats.syntax.all.*
import scala.io.StdIn

trait Controller:
  def printables: Stream[IO, String]

object Controller:
  private def texts(input: Input): Stream[IO, String] = input match
    case Input.FileInput(path) =>
      fs2.io.file
        .readAll[IO](path, 4096)
        .through(fs2.text.utf8Decode)
        .through(fs2.text.lines)
    case Input.StdIn =>
      fs2.io
        .stdin[IO](bufSize = 4096)
        .through(fs2.text.utf8Decode)
        .through(fs2.text.lines)

  def of[T: Show](
      input: Input,
      boundary: Boundary[Stream[IO, String], T]
  ): Controller =
    new:
      def printables: Stream[IO, String] =
        boundary
          .outputs(texts(input))
          .map(_.show)
