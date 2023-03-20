package org.redarqas.sq

import cats.effect.IO
import japgolly.scalagraal.AbstractGraalContext
import japgolly.scalagraal.GraalContextPool
import japgolly.scalagraal.ScalaGraalEffect
import japgolly.scalagraal.js.*

import scala.concurrent.Future

import GraalJs._
import japgolly.scalagraal.Expr
import org.graalvm.polyglot.Value

object Program:
  val futureToIO: ScalaGraalEffect.Trans[Future, IO] =
    new:
      def apply[A](fa: => Future[A]): IO[A] = IO.fromFuture(IO.delay(fa))

  def make(
      input: Input,
      prepareInstruction: String,
      transformInstruction: String,
      completeInstruction: String,
      threads: Int
  ): IO[Unit] =

    val instructions = Instructions(
      s => Expr(prepareInstruction).map(_.execute(s)),
      s => Expr(transformInstruction).map(_.execute(s)),
      s => Expr(completeInstruction).map(_.execute(s))
    )
    val context: AbstractGraalContext[IO] = GraalContextPool.Builder
      .fixedThreadPool(threads)
      .fixedContextPerThread()
      .build()
      .trans(futureToIO)
    DependencyGraph
      .make(context)(input, instructions)
      .printables
      .printlns
      .compile
      .drain
