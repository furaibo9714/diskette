package io.github.furaibo9714.diskette.ui_backup.features.export

import io.github.furaibo9714.diskette.common.extensions.nowUtc
import io.github.furaibo9714.diskette.common.extensions.toLocalZone
import java.time.format.DateTimeFormatter

object BackupFileName {

  val prefix = "diskette_export_"
  val fileType = ".json"
  val dateTimePattern = "yyyyMMddHHmmss"
  val memeType = "application/json"

  fun create(): String {
    val dateFormat = DateTimeFormatter.ofPattern(dateTimePattern)
    val currentDate = nowUtc().toLocalZone()
    return prefix + dateFormat.format(currentDate) + fileType
  }
}
