package io.github.furaibo9714.diskette.data_local.di

import android.content.Context
import androidx.room.Room
import io.github.furaibo9714.diskette.data_local.database.AppDatabase
import io.github.furaibo9714.diskette.data_local.database.migrations.DATABASE_NAME
import io.github.furaibo9714.diskette.data_local.utilities.TransactionsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class StorageModule {

  @Provides
  @Singleton
  internal fun providesDatabase(
    @ApplicationContext context: Context,
  ): AppDatabase {
    Timber.d("Creating database...")
    return Room
      .databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        DATABASE_NAME,
      ).build()
  }

  @Provides
  @Singleton
  internal fun providesTransactions(database: AppDatabase): TransactionsProvider = TransactionsProvider(database)
}
