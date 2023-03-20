package org.redarqas.sq

import java.nio.file.Path

enum Input:
  case FileInput(path: Path)
  case StdIn
