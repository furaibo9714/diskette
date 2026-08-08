package io.github.furaibo9714.diskette.ui_people.list.cases

import io.github.furaibo9714.diskette.common.Mode
import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.PeopleRepository
import io.github.furaibo9714.diskette.ui_model.MediaId
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Person
import io.github.furaibo9714.diskette.ui_people.list.recycler.PeopleListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class PeopleListItemsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val peopleRepository: PeopleRepository,
) {

  suspend fun loadPeople(
    mediaId: MediaId,
    mode: Mode,
    department: Person.Department,
  ): List<PeopleListItem.PersonItem> =
    withContext(dispatchers.IO) {
      val ids = Ids.EMPTY.copy(trakt = mediaId)
      val people: Map<Person.Department, List<Person>> = when (mode) {
        Mode.SHOWS -> peopleRepository.loadAllForShow(ids)
        Mode.MOVIES -> peopleRepository.loadAllForMovie(ids)
      }
      people.getOrDefault(department, emptyList()).map {
        PeopleListItem.PersonItem(it)
      }
    }
}
