import io.github.furaibo9714.diskette.ui_discover.recycler.DiscoverListItem
import io.github.furaibo9714.diskette.ui_model.AirTime
import io.github.furaibo9714.diskette.ui_model.IdImdb
import io.github.furaibo9714.diskette.ui_model.IdSlug
import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.IdTrakt
import io.github.furaibo9714.diskette.ui_model.IdTvRage
import io.github.furaibo9714.diskette.ui_model.IdTvdb
import io.github.furaibo9714.diskette.ui_model.Ids
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageFamily
import io.github.furaibo9714.diskette.ui_model.ImageSource
import io.github.furaibo9714.diskette.ui_model.ImageStatus
import io.github.furaibo9714.diskette.ui_model.ImageType
import io.github.furaibo9714.diskette.ui_model.Show
import io.github.furaibo9714.diskette.ui_model.ShowStatus

object TestData {

  val DISCOVER_LIST_ITEM = DiscoverListItem(
    show = Show(
      ids = Ids(
        trakt = IdTrakt(id = 0),
        slug = IdSlug(id = ""),
        tvdb = IdTvdb(id = 0),
        imdb = IdImdb(id = ""),
        tmdb = IdTmdb(id = 0),
        tvrage = IdTvRage(id = 0),
      ),
      title = "DISCOVER_LIST_ITEM",
      year = 0,
      overview = "",
      firstAired = "",
      runtime = 0,
      airTime = AirTime(day = "", time = "", timezone = ""),
      certification = "",
      network = "",
      country = "",
      trailer = "",
      homepage = "",
      status = ShowStatus.UNKNOWN,
      rating = 0.0f,
      votes = 0,
      commentCount = 0,
      genres = listOf(),
      airedEpisodes = 0,
      createdAt = 0,
      updatedAt = 0,
    ),
    image = Image(
      id = 0,
      idTvdb = IdTvdb(id = 0),
      idTmdb = IdTmdb(id = 0),
      type = ImageType.POSTER,
      family = ImageFamily.SHOW,
      fileUrl = "",
      thumbnailUrl = "",
      status = ImageStatus.UNKNOWN,
      source = ImageSource.TMDB,
    ),
    isLoading = false,
    isFollowed = false,
    isWatchlist = false,
  )
}
