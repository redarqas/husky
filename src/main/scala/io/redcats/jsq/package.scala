package io.redcats

import java.nio.file.{Files, Path, Paths}

import cats.data.NonEmptyList

import scala.util.Try

package object jsq {

  trait Input

  object Input {
    final case class FileInput(paths: NonEmptyList[Path]) extends Input
    final case object StdInput                            extends Input
  }

  sealed abstract class Language(val letters: String)

  object Language {

    final object JS     extends Language("js")
    final object RUBY   extends Language("ruby")
    final object PYTHON extends Language("python")
    final object R      extends Language("R")

    def of(input: String): Either[Throwable, Language] = input.toLowerCase match {
      case "js"     => Right(JS)
      case "python" => Right(PYTHON)
      case "ruby"   => Right(RUBY)
      case lang     => Left(new Exception(s"$lang in not a valid language"))
    }
  }

  sealed trait Expression {
    def name: String
    def content: String
  }

  object Expression {

    final case class StringExp(name: String, content: String)           extends Expression
    final case class FileExp(name: String, path: Path, content: String) extends Expression

    def of(name: String, in: String): Either[Throwable, Expression] = {
      val path = Paths.get(in)
      if (!Files.exists(path)) {
        Right(StringExp(name, in))
      } else {
        Try(FileExp(name, path, new String(Files.readAllBytes(path)))).toEither
      }
    }
  }
}
