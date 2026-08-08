package io.github.furaibo9714.diskette.ui_backup.migrations

import io.github.furaibo9714.diskette.common.extensions.dateIsoStringFromMillis
import io.github.furaibo9714.diskette.common.extensions.nowUtcMillis
import io.github.furaibo9714.diskette.ui_backup.BackupConfig.SCHEME_PLATFORM
import io.github.furaibo9714.diskette.ui_backup.BackupConfig.SCHEME_VERSION
import io.github.furaibo9714.diskette.ui_backup.model.BackupEpisode
import io.github.furaibo9714.diskette.ui_backup.model.BackupList
import io.github.furaibo9714.diskette.ui_backup.model.BackupListItem
import io.github.furaibo9714.diskette.ui_backup.model.BackupLists
import io.github.furaibo9714.diskette.ui_backup.model.BackupMovie
import io.github.furaibo9714.diskette.ui_backup.model.BackupMovies
import io.github.furaibo9714.diskette.ui_backup.model.BackupScheme
import io.github.furaibo9714.diskette.ui_backup.model.BackupSeason
import io.github.furaibo9714.diskette.ui_backup.model.BackupShow
import io.github.furaibo9714.diskette.ui_backup.model.BackupShows
import io.github.furaibo9714.diskette.ui_backup.model.v1.BackupScheme1

internal object BackupMigrationV1 {

  fun migrate(scheme: BackupScheme1): BackupScheme =
    BackupScheme(
      version = SCHEME_VERSION,
      platform = SCHEME_PLATFORM,
      createdAt = dateIsoStringFromMillis(nowUtcMillis()),
      shows = BackupShows(
        collectionHistory = scheme.shows.collectionHistory.map {
          BackupShow(
            mediaId = it.mediaId,
            tmdbId = it.tmdbId,
            title = it.title,
            addedAt = it.addedAt,
            updatedAt = it.updatedAt,
          )
        },
        collectionWatchlist = scheme.shows.collectionWatchlist.map {
          BackupShow(
            mediaId = it.mediaId,
            tmdbId = it.tmdbId,
            title = it.title,
            addedAt = it.addedAt,
            updatedAt = it.updatedAt,
          )
        },
        collectionHidden = scheme.shows.collectionHidden.map {
          BackupShow(
            mediaId = it.mediaId,
            tmdbId = it.tmdbId,
            title = it.title,
            addedAt = it.addedAt,
            updatedAt = it.updatedAt,
          )
        },
        progressEpisodes = scheme.shows.progressEpisodes.map {
          BackupEpisode(
            mediaId = it.mediaId,
            showTraktId = it.showTraktId,
            showTmdbId = -1,
            episodeNumber = it.episodeNumber,
            seasonNumber = it.seasonNumber,
            addedAt = it.addedAt,
          )
        },
        progressSeasons = scheme.shows.progressSeasons.map {
          BackupSeason(
            mediaId = it.mediaId,
            showTraktId = it.showTraktId,
            showTmdbId = -1,
            seasonNumber = it.seasonNumber,
          )
        },
        progressPinned = scheme.shows.progressPinned,
        progressOnHold = scheme.shows.progressOnHold,
        ratingsShows = emptyList(),
        ratingsSeasons = emptyList(),
        ratingsEpisodes = emptyList(),
      ),
      movies = BackupMovies(
        collectionHistory = scheme.movies.collectionHistory.map {
          BackupMovie(
            mediaId = it.mediaId,
            tmdbId = it.tmdbId,
            title = it.title,
            addedAt = it.addedAt,
          )
        },
        collectionWatchlist = scheme.movies.collectionWatchlist.map {
          BackupMovie(
            mediaId = it.mediaId,
            tmdbId = it.tmdbId,
            title = it.title,
            addedAt = it.addedAt,
          )
        },
        collectionHidden = scheme.movies.collectionHidden.map {
          BackupMovie(
            mediaId = it.mediaId,
            tmdbId = it.tmdbId,
            title = it.title,
            addedAt = it.addedAt,
          )
        },
        progressPinned = scheme.movies.progressPinned,
      ),
      lists = BackupLists(
        lists = scheme.lists.lists.map {
          BackupList(
            id = it.id,
            mediaId = it.mediaId,
            slugId = it.slugId,
            name = it.name,
            description = it.description,
            privacy = it.privacy,
            itemCount = it.itemCount,
            createdAt = it.createdAt,
            updatedAt = it.updatedAt,
            items = it.items.map { item ->
              BackupListItem(
                id = item.id,
                listId = item.listId,
                mediaId = item.mediaId,
                tmdbId = -1,
                type = item.type,
                rank = item.rank,
                listedAt = item.listedAt,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt,
              )
            },
          )
        },
      ),
    )
}
