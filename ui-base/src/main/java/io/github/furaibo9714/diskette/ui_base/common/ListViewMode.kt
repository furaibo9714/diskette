package io.github.furaibo9714.diskette.ui_base.common

enum class ListViewMode {
  LIST_NORMAL,
  LIST_GRID,
  LIST_COMPACT,
  ;

  /** Order the view mode chip cycles through on each tap. */
  fun next(): ListViewMode =
    when (this) {
      LIST_NORMAL -> LIST_GRID
      LIST_GRID -> LIST_COMPACT
      LIST_COMPACT -> LIST_NORMAL
    }
}
