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

  def evaluate(filter: String): IO[Unit] = IO.delay {
    val string: String = """{ "name":"John", "age":30, "city":"New York"}"""
    val context        = Context.create()
    val json           = context.eval("js", s"JSON.parse('$string')")
    val function       = context.eval("js", s"i => {return JSON.stringify($filter)}")
    val result         = function.execute(json).toString
    println(result)
  }

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
        val function = context.eval("js", s"i => {return JSON.stringify($filter)}")
        function.execute(json).toString.some
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
