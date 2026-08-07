package io.github.furaibo9714.diskette.common.di

import io.github.furaibo9714.diskette.common.dispatchers.CoroutineDispatchers
import io.github.furaibo9714.diskette.common.dispatchers.DefaultCoroutineDispatchers
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CommonBindingModule {

  @Binds
  abstract fun bindCoroutineDispatchers(dispatchers: DefaultCoroutineDispatchers): CoroutineDispatchers
}
