@file:Suppress("DEPRECATION")

package io.github.furaibo9714.diskette.data_local.database.dao

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.github.furaibo9714.diskette.data_local.database.dao.helpers.TestData
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsDaoTest : BaseDaoTest() {

  @Test
  fun shouldInsertAndSaveData() {
    runBlocking {
      val settings = TestData.createSettings()

      database.settingsDao().upsert(settings)
      val result = database.settingsDao().getAll()
      assertThat(result).isEqualTo(settings)
    }
  }

  /**
   * [SettingsDao.getAll] is declared non-null and throws on an empty table, so callers ask
   * [SettingsDao.getCount] first - that is the emptiness check this asserts. The previous version
   * expected getAll to return null, which the contract has never allowed.
   */
  @Test
  fun shouldReportEmptyBeforeAnySettingsAreStored() {
    runBlocking {
      assertThat(database.settingsDao().getCount()).isEqualTo(0)

      database.settingsDao().upsert(TestData.createSettings())

      assertThat(database.settingsDao().getCount()).isEqualTo(1)
    }
  }

  @Test
  fun shouldUpdateRowIfAlreadyExists() {
    runBlocking {
      val settings = TestData.createSettings()

      database.settingsDao().upsert(settings)
      assertThat(database.settingsDao().getAll()).isEqualTo(settings)

      val settings2 = settings.copy(myShowsEndedSortBy = "sort")
      database.settingsDao().upsert(settings2)
      assertThat(database.settingsDao().getAll()).isEqualTo(settings2)
    }
  }
}
