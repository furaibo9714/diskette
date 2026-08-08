package io.github.furaibo9714.diskette.data_local.database.migrations

import androidx.room.migration.Migration

const val DATABASE_VERSION = 1
const val DATABASE_NAME = "DISKETTE_DB"

/**
 * Schema history starts here. Every table is keyed on `media_id`, a `"{source}:{id}"` string
 * addressing the item the way Floppy does, so nothing inherited from the Trakt-era schema
 * survives to migrate from.
 */
class Migrations {

  fun getAll(): List<Migration> = emptyList()
}
