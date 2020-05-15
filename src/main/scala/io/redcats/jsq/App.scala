package io.redcats.jsq

import java.io.InputStream
import java.nio.file.{Files, Path, Paths}

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import org.graalvm.polyglot._
import fs2._
import com.monovore.decline._
import com.monovore.decline.effect._

object App extends CommandIOApp(name = "jsq", header = "", version = "lastest") {

  trait Input {
    def bytesStream(blocker: Blocker): Stream[IO, Byte]
  }

  final case class FileInput(paths: NonEmptyList[Path]) extends Input {

    override def bytesStream(blocker: Blocker): Stream[IO, Byte] =
      paths.reduceMap(p =>
        fs2.io.readInputStream[IO](blocker.delay[IO, InputStream](Files.newInputStream(p)), 4096, blocker))

  }

  final case object StdInput extends Input {

    override def bytesStream(blocker: Blocker): Stream[IO, Byte] =
      fs2.io
        .stdin[IO](4096, blocker)
  }

  override def main: Opts[IO[ExitCode]] = {
    val filter = Opts
      .argument[String](metavar = "filter")
      .map(p => if (Files.exists(Paths.get(p))) new String(Files.readAllBytes(Paths.get(p))) else p)

    val in: Opts[Input] = Opts.arguments[Path](metavar = "files").orNone.map(_.fold[Input](StdInput)(FileInput))

    (filter, in).mapN {
      case (filter, in) =>
        prog(filter, in)
          .use(_ => IO.unit)
          .as(ExitCode.Success)
    }

  }

  def contextR: Resource[IO, Context] =
    Resource.make(IO.delay {
      val ctx = Context.create()
      ctx.initialize("js")
      ctx
    })(ctx => IO.delay(ctx.close()))

  def lines(blocker: Blocker, in: Input): Stream[IO, String] =
    in.bytesStream(blocker)
      .through(text.utf8Decode)
      .through(text.lines)
      .map(_.trim)
      .filter(_.nonEmpty)

  def eval(context: Context, string: String, function: Value): IO[Option[String]] =
    IO.delay {
        val json   = context.eval("js", s"JSON.parse('$string')")
        val result = function.execute(json)
        if (result.isNull) none[String] else result.toString.some
      }
      .handleError(_ => none[String])

  def all(blocker: Blocker, filter: String, in: Input): IO[Unit] =
    Stream
      .resource(contextR)
      .evalMap(ctx => blocker.blockOn(IO.delay(ctx.eval("js", s"$filter"))).map(f => (f, ctx)))
      .flatMap { case (f, ctx) => lines(blocker, in).map(line => (f, ctx, line)) }
      .evalMap { case (f, ctx, line) => blocker.blockOn(eval(ctx, line, f)) }
      .unNone
      .showLinesStdOut
      .compile
      .drain

  def prog(filter: String, in: Input): Resource[IO, Unit] =
    for {
      blocker <- Blocker[IO]
      _       <- Resource.liftF(all(blocker, filter, in))
    } yield ()
}
