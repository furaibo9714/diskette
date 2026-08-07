package io.github.furaibo9714.diskette.ui_model

enum class UpcomingFilter {
  OFF,
  UPCOMING,
  RELEASED,
  ;

  fun isActive() = this != OFF
}
