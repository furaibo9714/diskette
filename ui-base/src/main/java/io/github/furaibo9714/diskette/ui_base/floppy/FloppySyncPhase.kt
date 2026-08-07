package io.github.furaibo9714.diskette.ui_base.floppy

import android.content.Context
import androidx.annotation.StringRes
import androidx.work.WorkInfo
import io.github.furaibo9714.diskette.ui_base.R

enum class FloppySyncPhase(
  @StringRes val textRes: Int,
) {
  IMPORTING_WATCHLIST(R.string.textFloppySyncImportingWatchlist),
  IMPORTING_WATCHED(R.string.textFloppySyncImportingWatched),
  SYNCING_DETAILS(R.string.textFloppySyncSyncingDetails),
  EXPORTING(R.string.textFloppySyncExporting),
}

data class FloppySyncProgressState(
  val phase: FloppySyncPhase,
  val count: Int = 0,
  val total: Int = 0,
)

/**
 * Resolves the current [FloppySyncPhase]/count/total set via [FloppySyncWorker]'s `setProgress`
 * calls, for screens observing [FloppySyncWorker.TAG_ID] work to show what a running full sync is
 * doing right now, rather than a static "syncing" label.
 */
fun WorkInfo.floppySyncProgressState(): FloppySyncProgressState? {
  val phase = progress
    .getString(FloppySyncWorker.ARG_SYNC_PHASE)
    ?.let { name -> runCatching { FloppySyncPhase.valueOf(name) }.getOrNull() }
    ?: return null
  return FloppySyncProgressState(
    phase = phase,
    count = progress.getInt(FloppySyncWorker.ARG_SYNC_COUNT, 0),
    total = progress.getInt(FloppySyncWorker.ARG_SYNC_TOTAL, 0),
  )
}

fun FloppySyncProgressState.format(context: Context): String {
  val phaseText = context.getString(phase.textRes)
  return if (total > 0) {
    context.getString(R.string.textFloppySyncPhaseWithProgress, phaseText, count, total)
  } else {
    phaseText
  }
}
