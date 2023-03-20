package org.redarqas.sq

import fs2.Stream
import cats.effect.IO

/** Partitionner Input port
  *
  *   - Takes any input of type A and transform into a stream of Texts
  */
trait Boundary[A, C]:
  def outputs(input: A): Stream[IO, Output[C]]
