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

  override def main: Opts[IO[ExitCode]] = {
    val in: Opts[Input] =
      Opts.arguments[Path](metavar = "files").orNone.map(_.fold[Input](Input.StdInput)(Input.FileInput))

    val process: Opts[Expression] = Opts
      .argument[String](metavar = "process")
      .mapValidated(c => Expression.of("process", c).leftMap(_.getMessage).toValidatedNel)

    val lang: Opts[Language] = Opts
      .option[String](long = "language", short = "l", help = "process language [js|ruby|python|R]")
      .withDefault("js")
      .mapValidated(l => Language.of(l).leftMap(_.getMessage).toValidatedNel)

    val prepare0: Opts[Option[Expression]] = Opts
      .option[String](
        long = "prepare",
        short = "pre",
        help = "prepare the input to a suitable type for the process action")
      .mapValidated(c => Expression.of("prepare", c).leftMap(_.getMessage).toValidatedNel)
      .orNone

    val complete: Opts[Option[Expression]] = Opts
      .option[String](
        long = "complete",
        short = "comp",
        help = "transform the output of process action to a printable type")
      .mapValidated(c => Expression.of("complete", c).leftMap(_.getMessage).toValidatedNel)
      .orNone

    val processor: Opts[Processor[String, Value]] =
      (lang, prepare0, process, complete).mapN { case (l, pre, pro, comp) => (l, pre, pro, comp) }.mapValidated {
        case (Language.JS, None, pro, comp) =>
          Processor
            .of[String](Language.JS, Expression.StringExp("prepare", "input => JSON.parse(input)").some, pro, comp)
            .leftMap(_.getMessage)
            .toValidatedNel
        case (Language.PYTHON, None, pro, comp) =>
          Processor
            .of[String](
              Language.PYTHON,
              Expression.StringExp("prepare", "lambda input: json.loads(input)").some,
              pro,
              comp)
            .leftMap(_.getMessage)
            .toValidatedNel
        case (Language.RUBY, None, pro, comp) =>
          Processor
            .of[String](
              Language.RUBY,
              Expression.StringExp("prepare", "-> (input) { JSON.parse(input) }").some,
              pro,
              comp)
            .leftMap(_.getMessage)
            .toValidatedNel
        case (l, pre, pro, comp) => Processor.of[String](l, pre, pro, comp).leftMap(_.getMessage).toValidatedNel
      }

    (processor, in).mapN {
      case (processor, in) =>
        prog(processor, in)
          .use(_ => IO.unit)
          .as(ExitCode.Success)
    }

  }

  def lines(blocker: Blocker, in: Input): Stream[IO, String] =
    bytesStream(blocker, in)
      .through(text.utf8Decode)
      .through(text.lines)
      .map(_.trim)
      .filter(_.nonEmpty)

  def all(blocker: Blocker, processor: Processor[String, Value], in: Input): IO[Unit] = {

    lines(blocker, in)
      .evalMap(line =>
        IO.delay(processor.run(line).leftMap(_.getMessage).map(_.toString).merge).attempt.map(_.toOption))
      .unNone
      .map(raw => fansi.Color.LightGreen(raw).toString())
      .showLinesStdOut
      .compile
      .drain
  }

  def prog(processor: Processor[String, Value], in: Input): Resource[IO, Unit] =
    for {
      blocker       <- Blocker[IO]
      langProcessor <- Resource.make(IO(processor))(p => IO.delay(p.close))
      _             <- Resource.liftF(all(blocker, langProcessor, in))

    } yield ()

  def bytesStream(blocker: Blocker, in: Input): Stream[IO, Byte] = in match {
    case Input.StdInput =>
      fs2.io
        .stdin[IO](bufSize = 4096, blocker = blocker)
    case Input.FileInput(paths: NonEmptyList[Path]) =>
      paths.reduceMap(p =>
        fs2.io.readInputStream[IO](
          fis = blocker.delay[IO, InputStream](Files.newInputStream(p)),
          chunkSize = 4096,
          blocker = blocker))
  }
}
