package org.redarqas.sq

import cats.effect.IO
import cats.Show

/** Part Transformer Output port
  *   - Chainable primitives to apply on a text to get the expected output
  */
trait Gate[A, B, C]:
  def prepare(part: String): IO[A]
  def transform(prepared: A): IO[B]
  def complete(transformed: B): IO[Output[C]]
  def run(part: String): IO[Output[C]] = for
    a <- prepare(part)
    b <- transform(a)
    c <- complete(b)
  yield c
