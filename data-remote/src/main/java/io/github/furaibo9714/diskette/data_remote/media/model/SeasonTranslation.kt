package io.github.furaibo9714.diskette.data_remote.media.model

data class SeasonTranslation(
  val season: Int,
  val number: Int,
  val ids: Ids,
  val translations: List<Translation>?,
)
