package io.redcats.jsq

import org.graalvm.polyglot.{Source, Value}
import cats.implicits._

object Main {

  def run[A](lang: Language, pre: Option[String], pro: String, comp: Option[String])(
      unit: A): Either[Throwable, Value] = {
    val processor: Either[Throwable, Processor[A, Value]] = Processor.of[A](
      lang,
      pre.map(Expression.StringExp("prepare", _)),
      Expression.StringExp("process", pro),
      comp.map(Expression.StringExp("complete", _)))
    processor.flatMap(_.run(unit))
  }

  def main(args: Array[String]): Unit = {
    run(
      Language.R,
      "function(x) result <- x + 1".some,
      "function(x) result <- x + 1",
      "function(x) result <- x + 1".some)(1)
      .map(s => println(s.asDouble()))
      .recover { case t: Throwable => println(t) }
    run(Language.JS, "x => x + ': JS pre'".some, "x => x + ' pro'", "x => x + ' comp'".some)("my line")
      .map(s => println(s.asString()))
      .recover { case t: Throwable => println(t) }

    run(Language.JS, "input => JSON.parse(input)".some, "parsed => parsed.name", "x => x + ' comp'".some)(
      """{"name":"chaqouri", "age": 39}""")
      .map(s => println(s.asString()))
      .recover { case t: Throwable => println(t.getMessage) }

    run(Language.RUBY, "-> (x) { x + ': Ruby pre'}".some, "-> (x) { x + ' pro'}", "-> (x) { x + ' comp'}".some)(
      "my line")
      .map(s => println(s.asString()))
      .recover { case t: Throwable => println(t) }

    run(
      Language.RUBY,
      "-> (input) { JSON.parse(input) }".some,
      "-> (hash) { hash['name']}",
      "-> (x) { x + ' comp'}".some)("""{"name":"chaqouri", "age": 39}""")
      .map(s => println(s.asString()))
      .recover { case t: Throwable => println(t) }

    run(Language.PYTHON, "lambda x: x + ': python pre'".some, "lambda x: x + ' pro'", "lambda x: x + ' comp'".some)(
      "my line").map(s => println(s.asString())).recover { case t: Throwable => println(t) }

    run(Language.PYTHON, "lambda x: json.loads(x)".some, "lambda x: x['name']", "lambda x: x + ' comp'".some)(
      """{"name":"chaqouri", "age": 39}""")
      .map(s => println(s.asString()))
      .recover { case t: Throwable => println(t) }
  }

}
