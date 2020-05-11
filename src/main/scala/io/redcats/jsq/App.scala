package io.redcats.jsq

import cats.effect._
import cats.implicits._
import org.graalvm.polyglot._
import fs2._

import com.monovore.decline._
import com.monovore.decline.effect._

object App extends CommandIOApp(name = "jsq", header = "", version = "lastest") {

  override def main: Opts[IO[ExitCode]] =
    Opts
      .argument[String](metavar = "transfo")
      .map(t =>
        prog(t)
          .use(_ => IO.unit)
          .as(ExitCode.Success))

  def contextR: Resource[IO, Context] = Resource.make(IO.delay(Context.create()))(ctx => IO.delay(ctx.close()))

  def lines(blocker: Blocker): Stream[IO, String] =
    fs2.io
      .stdin[IO](10000, blocker)
      .through(text.utf8Decode)
      .through(text.lines)
      .map(_.trim)
      .filter(_.nonEmpty)

  def eval(context: Context, string: String, filter: String): IO[Option[String]] =
    IO.delay {
        val json     = context.eval("js", s"JSON.parse('$string')")
        val function = context.eval("js", s"$filter")
        val result   = function.execute(json)
        if (result.isNull) none[String] else result.toString.some
      }
      .handleError(_ => none[String])

  def all(blocker: Blocker, filter: String): IO[Unit] =
    Stream
      .resource(contextR)
      .flatMap(ctx => lines(blocker).map(line => (ctx, line)))
      .evalMap { case (ctx, line) => eval(ctx, line, filter) }
      .unNone
      .showLinesStdOut
      .compile
      .drain

  def prog(filter: String): Resource[IO, Unit] =
    for {
      blocker <- Blocker[IO]
      _       <- Resource.liftF(all(blocker, filter))
    } yield ()
}
