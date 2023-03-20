package org.redarqas.sq

import org.graalvm.polyglot.Value
import japgolly.scalagraal.Expr

case class Instructions(
    prepare: String => Expr[Value],
    transform: Value => Expr[Value],
    complete: Value => Expr[Value]
)
