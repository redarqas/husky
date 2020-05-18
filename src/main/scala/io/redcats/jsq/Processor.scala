package io.redcats.jsq

import org.graalvm.polyglot.{Context, Source, Value}
import cats.implicits._

trait Processor[A, T] {
  def prepare(unit: A): Either[Throwable, T]
  def process(prepared: T): Either[Throwable, T]
  def complete(processed: T): Either[Throwable, T]

  def run(unit: A): Either[Throwable, T] =
    for {
      pre  <- prepare(unit)
      pro  <- process(pre)
      comp <- complete(pro)
    } yield comp
  def close: Either[Throwable, Unit]
}

object Processor {

  private def langContext(lang: Language): Either[Throwable, Context] = {
    def imports(ctx: Context): Either[Throwable, Unit] =
      lang match {
        case Language.JS | Language.R => ().asRight
        case Language.PYTHON          => loadSource(ctx, Language.PYTHON, "import json").map(_ => ())
        case Language.RUBY            => loadSource(ctx, Language.RUBY, "require 'json'").map(_ => ())
      }
    for {
      ctx <- Either.catchNonFatal(Context.newBuilder(lang.letters).allowAllAccess(true).build)
      _   <- imports(ctx)
    } yield ctx
  }

  private def loadInvokable(context: Context, lang: Language, exp: Expression): Either[Throwable, Value] = {
    def evalOrBind(funcSrc: String): Either[Throwable, Value] =
      Either.catchNonFatal(context.eval(Source.create(lang.letters, funcSrc))).flatMap {
        case v if v.canExecute => v.asRight[Throwable]
        case _                 => Either.catchNonFatal(context.getBindings(lang.letters).getMember(exp.name))
      }
    for {
      funcSrc <- functionSource(lang, exp)
      parsed  <- evalOrBind(funcSrc)
      invokable <- if (parsed.canExecute) parsed.asRight
      else new Exception(s"${exp.name} can not be executed as a function").asLeft
    } yield invokable
  }

  private def loadSource(context: Context, lang: Language, source: String): Either[Throwable, Value] =
    Either.catchNonFatal(context.eval(Source.create(lang.letters, source)))

  private def functionSource(lang: Language, exp: Expression): Either[Throwable, String] = lang match {
    case Language.JS                                                           => s"const ${exp.name} = ${exp.content}".asRight
    case Language.PYTHON if exp.content.trim.startsWith("lambda")              => s"${exp.name} = ${exp.content}".asRight
    case Language.PYTHON if exp.content.trim.startsWith(s"def ${exp.name}")    => exp.content.asRight
    case Language.R if exp.content.trim.startsWith("function")                 => s"${exp.name} = ${exp.content}".asRight
    case Language.R if exp.content.trim.startsWith(s"${exp.name} -> function") => exp.content.asRight
    case Language.RUBY if exp.content.trim.startsWith("->")                    => s"${exp.name} = ${exp.content}".asRight
    case Language.RUBY if exp.content.trim.startsWith(s"def ${exp.name}")      => exp.content.asRight
    case _                                                                     => new Exception(s"${exp.name} should be a lambda or a function").asLeft
  }

  def of[A](
      lang: Language,
      optPre: Option[Expression],
      pro: Expression,
      optComp: Option[Expression]): Either[Throwable, Processor[A, Value]] = {
    val loadedContext: Either[Throwable, (Context, Option[Value], Value, Option[Value])] = for {
      ctx           <- langContext(lang)
      preInvokable  <- optPre.fold(none[Value].asRight[Throwable])(pre => loadInvokable(ctx, lang, pre).map(_.some))
      proInvokable  <- loadInvokable(ctx, lang, pro)
      compInvokable <- optComp.fold(none[Value].asRight[Throwable])(cmp => loadInvokable(ctx, lang, cmp).map(_.some))
    } yield (ctx, preInvokable, proInvokable, compInvokable)

    loadedContext.map {
      case (ctx, optPre, pro, optComp) =>
        new Processor[A, Value] {
          override def prepare(unit: A): Either[Throwable, Value] =
            optPre.fold(ctx.asValue(unit).asRight[Throwable])(pre =>
              Either.catchNonFatal(pre.execute(ctx.asValue(unit))))

          override def process(prepared: Value): Either[Throwable, Value] =
            Either.catchNonFatal(pro.execute(prepared))

          override def complete(processed: Value): Either[Throwable, Value] =
            optComp.fold(processed.asRight[Throwable])(comp => Either.catchNonFatal(comp.execute(processed)))

          override def close: Either[Throwable, Unit] = Either.catchNonFatal(ctx.close())
        }
    }

  }

}
