package org.redarqas.sq

import cats.Show

opaque type Output[A] = A
object Output:
  def apply[A](a: A): Output[A] = a
  extension [A](output: Output[A]) def value: A = output
  given [A](using showA: Show[A]): Show[Output[A]] = showA
