package io.github.furaibo9714.diskette.ui.main.cases

import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.repository.ListsRepository
import io.github.furaibo9714.diskette.repository.floppy.FloppyConnectionManager
import io.github.furaibo9714.diskette.repository.settings.SettingsRepository
import io.github.furaibo9714.diskette.ui_base.floppy.FloppySyncManager
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * One-time backfill for list items added before Floppy list sync existed (or added while Floppy
 * was unreachable) - they never got a [io.github.furaibo9714.diskette.data_local.database.model.FloppySyncQueue]
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
