package io.github.furaibo9714.diskette.ui_people.details.cases

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.repository.PeopleRepository
import io.github.furaibo9714.diskette.ui_base.dates.DateFormatProvider
import io.github.furaibo9714.diskette.ui_model.Person
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class PersonDetailsLoadCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val peopleRepository: PeopleRepository,
  private val dateFormatProvider: DateFormatProvider,
) {

  suspend fun loadDetails(person: Person) =
    withContext(dispatchers.IO) {
      peopleRepository
        .loadDetails(person)
        .copy(characters = person.characters)
    }

  fun loadDateFormat() = dateFormatProvider.loadShortDayFormat()
}
