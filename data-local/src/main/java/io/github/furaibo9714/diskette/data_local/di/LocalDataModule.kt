package io.github.furaibo9714.diskette.data_local.di

import io.github.furaibo9714.diskette.data_local.LocalDataSource
import io.github.furaibo9714.diskette.data_local.MainLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataModule {

  @Binds
  @Singleton
  internal abstract fun providesLocalDataSource(source: MainLocalDataSource): LocalDataSource
}
