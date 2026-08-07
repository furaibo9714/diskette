package io.github.furaibo9714.diskette.ui_discover_movies.di

import android.content.Context
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.isTablet
import io.github.furaibo9714.diskette.ui_discover_movies.helpers.itemtype.ImageTypeProvider
import io.github.furaibo9714.diskette.ui_discover_movies.helpers.itemtype.PhoneImageTypeProvider
import io.github.furaibo9714.diskette.ui_discover_movies.helpers.itemtype.TabletImageTypeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DiscoverMoviesModule {

  @Provides
  internal fun providesItemTypeProvider(
    @ApplicationContext context: Context,
  ): ImageTypeProvider =
    if (context.isTablet()) {
      TabletImageTypeProvider()
    } else {
      PhoneImageTypeProvider()
    }
}
