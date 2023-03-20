package org.redarqas.sq

import cats.Show
import cats.effect.IO
import japgolly.scalagraal.AbstractGraalContext
import org.graalvm.polyglot.Value

object DependencyGraph:
  given Show[Value] = Show.show(_.toString())
  def make(context: AbstractGraalContext[IO])(
      input: Input,
      instructions: Instructions
  ): Controller =
    Controller.of(input, GraalBoundary.of(GraalGate.of(context, instructions)))
