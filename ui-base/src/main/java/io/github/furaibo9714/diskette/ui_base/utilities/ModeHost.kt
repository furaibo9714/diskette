package io.github.furaibo9714.diskette.ui_base.utilities

import io.github.furaibo9714.diskette.common.Mode

interface ModeHost {
  fun setMode(
    mode: Mode,
    force: Boolean = false,
  )

  fun getMode(): Mode
}
