package org.redarqas.sq

import org.graalvm.polyglot.Value
import cats.effect.IO
import fs2.Stream
import cats.effect.std.Console
import japgolly.scalagraal.*

object GraalBoundary:
  def of(
      gate: Gate[Value, Value, Value]
  ): Boundary[fs2.Stream[IO, String], Value] =

    def prepare(s: String): Stream[IO, Value] =
      val prepTry = gate
        .prepare(s)
        .onError(t =>
          Console[IO].error(s"Prepare error $s: ${t.getMessage()}\n")
        )
        .attempt
        .map(_.toOption)
      Stream.eval(prepTry).unNone

    def transform(s: Value): Stream[IO, Value] =
      val transTry = gate
        .transform(s)
        .onError(t =>
          Console[IO].error(s"Transform error $s: ${t.getMessage()}\n")
        )
        .attempt
        .map(_.toOption)
      Stream
        .eval(transTry)
        .unNone
        .flatMap(v =>
          if (v.hasArrayElements())
            Stream.fromIterator[IO](v.arrayIterator(), 3)
          else
            Stream.emit[IO, Value](v)
        )

    def complete(s: Value): Stream[IO, Output[Value]] =
      val compTry = gate
        .complete(s)
        .onError(t =>
          Console[IO].error(s"Complete error $s: ${t.getMessage()}\n")
        )
        .attempt
        .map(_.toOption)
      Stream.eval(compTry).unNone

    new:
      def outputs(input: fs2.Stream[IO, String]): Stream[IO, Output[Value]] =
        input.flatMap(prepare).flatMap(transform).flatMap(complete)
