package org.redarqas.sq

import java.nio.file.Path
import cats.data.NonEmptyList

enum Input:
  case FileInput(paths: NonEmptyList[Path])
  case StdIn
