package io.github.furaibo9714.diskette.ui_lists.create.cases

import io.github.furaibo9714.diskette.data_remote.floppy.model.FloppyListCreateRequest
import io.github.furaibo9714.diskette.repository.ListsRepository
import io.github.furaibo9714.diskette.repository.floppy.FloppyConnectionManager
import io.github.furaibo9714.diskette.ui_model.CustomList
import dagger.hilt.android.scopes.ViewModelScoped
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class CreateListCase @Inject constructor(
  private val connectionManager: FloppyConnectionManager,
  private val listsRepository: ListsRepository,
) {

  suspend fun createList(
    name: String,
    description: String?,
  ): CustomList {
    val idFloppy = createFloppyList(name, description)
    return listsRepository.createList(name, description, idTrakt = null, idFloppy = idFloppy, idSlug = null)
  }

  suspend fun updateList(list: CustomList): CustomList {
    val idFloppy = list.idFloppy ?: createFloppyList(list.name, list.description)
    if (list.idFloppy != null && idFloppy != null) {
      try {
        connectionManager.service().updateList(idFloppy, FloppyListCreateRequest(list.name, list.description))
      } catch (error: Throwable) {
        Timber.w(error, "Failed to update list on Floppy. Local update proceeds regardless.")
      }
    }
    return listsRepository.updateList(list.id, idTrakt = null, idFloppy = idFloppy, idSlug = null, list.name, list.description)
  }

  private suspend fun createFloppyList(
    name: String,
    description: String?,
  ): Long? {
    if (!connectionManager.isConfigured()) return null
    return try {
      connectionManager.service().createList(FloppyListCreateRequest(name, description)).id
    } catch (error: Throwable) {
      Timber.w(error, "Failed to create list on Floppy. Local list will be created without a Floppy id.")
      null
    }
  }
}
