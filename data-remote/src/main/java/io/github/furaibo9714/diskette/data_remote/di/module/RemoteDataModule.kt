package io.github.furaibo9714.diskette.data_remote.di.module

import io.github.furaibo9714.diskette.data_remote.MainRemoteDataSource
import io.github.furaibo9714.diskette.data_remote.RemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataModule {

  @Binds
  @Singleton
  internal abstract fun providesRemoteDataSource(source: MainRemoteDataSource): RemoteDataSource
}
