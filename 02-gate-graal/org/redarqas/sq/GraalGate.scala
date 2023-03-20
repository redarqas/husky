package org.redarqas.sq

import org.graalvm.polyglot.Value
import cats.Show
import cats.effect.IO
import japgolly.scalagraal.GraalContext
import japgolly.scalagraal.Expr
import japgolly.scalagraal.Expr.Result
import japgolly.scalagraal.AbstractGraalContext

object GraalGate:
  def of(
      ctx: AbstractGraalContext[IO],
      intructions: Instructions
  ): Gate[Value, Value, Value] =
    new:
      def prepare(part: String): IO[Value] =
        ctx.eval(intructions.prepare(part)).flatMap(IO.fromEither)
      def transform(prepared: Value): IO[Value] =
        ctx.eval(intructions.transform(prepared)).flatMap(IO.fromEither)
      def complete(transformed: Value): IO[Output[Value]] =
        ctx
          .eval(intructions.complete(transformed))
          .flatMap(IO.fromEither)
          .map(Output.apply)
