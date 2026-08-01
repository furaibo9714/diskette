package com.michaldrabik.showly2.ui.main.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.repository.ListsRepository
import com.michaldrabik.repository.floppy.FloppyConnectionManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.floppy.FloppySyncManager
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * One-time backfill for list items added before Floppy list sync existed (or added while Floppy
 * was unreachable) - they never got a [com.michaldrabik.data_local.database.model.FloppySyncQueue]
 * entry, so they were never pushed. Re-adding an item Floppy already has is a safe no-op
 * (confirmed empirically: `409 "Media already in the list"`, no duplicates), so this just
 * re-enqueues every local item of every Floppy-linked list rather than diffing against Floppy's
 * current contents first. Runs once - guarded by [SettingsRepository.isFloppyListsBackfilled] -
 * and only marks itself done if Floppy was actually configured at the time, so it retries on next
 * app start for anyone who sets up Floppy after this ships.
 */
@ViewModelScoped
class MainFloppyListsBackfillCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val listsRepository: ListsRepository,
  private val connectionManager: FloppyConnectionManager,
  private val floppySyncManager: FloppySyncManager,
) {

  suspend fun backfillListsIfNeeded() {
    if (settingsRepository.isFloppyListsBackfilled) return
    if (!connectionManager.isConfigured()) return

    listsRepository.loadAll()
      .filter { it.idFloppy != null }
      .forEach { list ->
        listsRepository.loadItemsById(list.id).forEach { item ->
          val mode = Mode.fromType(item.type)
          floppySyncManager.scheduleListItemAdd(IdTrakt(item.idTrakt), mode, list.idFloppy)
        }
      }

    settingsRepository.isFloppyListsBackfilled = true
  }
}
