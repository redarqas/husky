package org.redarqas.sq

import org.graalvm.polyglot.Value
import cats.effect.IO
import fs2.Stream
import cats.effect.std.Console

object GraalBoundary:
  def of(
      gate: Gate[Value, Value, Value]
  ): Boundary[fs2.Stream[IO, String], Value] =
    new:
      def outputs(input: fs2.Stream[IO, String]): Stream[IO, Output[Value]] =
        input
          .evalMap(s =>
            gate
              .run(s)
              .onError(t =>
                Console[IO].error(s"Invalid $s: ${t.getMessage()}\n")
              )
              .attempt
              .map(_.toOption)
          )
          .unNone
