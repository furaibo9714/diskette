package com.michaldrabik.ui_base.floppy

import androidx.annotation.StringRes
import androidx.work.WorkInfo
import com.michaldrabik.ui_base.R

enum class FloppySyncPhase(
  @StringRes val textRes: Int,
) {
  IMPORTING_WATCHLIST(R.string.textFloppySyncImportingWatchlist),
  IMPORTING_WATCHED(R.string.textFloppySyncImportingWatched),
  SYNCING_DETAILS(R.string.textFloppySyncSyncingDetails),
  EXPORTING(R.string.textFloppySyncExporting),
}

/**
 * Resolves the current [FloppySyncPhase] set via [FloppySyncWorker]'s `setProgress` calls, for
 * screens observing [FloppySyncWorker.TAG_ID] work to show what a running full sync is doing
 * right now, rather than a static "syncing" label.
 */
@StringRes
fun WorkInfo.floppySyncPhaseTextRes(): Int {
  val phase = progress
    .getString(FloppySyncWorker.ARG_SYNC_PHASE)
    ?.let { name -> runCatching { FloppySyncPhase.valueOf(name) }.getOrNull() }
  return phase?.textRes ?: R.string.textFloppySyncRunning
}
