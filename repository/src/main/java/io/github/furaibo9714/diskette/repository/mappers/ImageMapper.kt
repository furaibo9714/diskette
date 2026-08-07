package io.github.furaibo9714.diskette.repository.mappers

import io.github.furaibo9714.diskette.ui_model.IdTmdb
import io.github.furaibo9714.diskette.ui_model.IdTvdb
import io.github.furaibo9714.diskette.ui_model.Image
import io.github.furaibo9714.diskette.ui_model.ImageFamily
import io.github.furaibo9714.diskette.ui_model.ImageSource
import io.github.furaibo9714.diskette.ui_model.ImageStatus.AVAILABLE
import java.util.Locale.ROOT
import javax.inject.Inject
import io.github.furaibo9714.diskette.data_local.database.model.MovieImage as MovieImageDb
import io.github.furaibo9714.diskette.data_local.database.model.ShowImage as ShowImageDb

class ImageMapper @Inject constructor() {

  fun fromDatabase(imageDb: ShowImageDb): Image =
    Image(
      imageDb.id,
      IdTvdb(imageDb.idTvdb),
      IdTmdb(imageDb.idTmdb),
      enumValueOf(imageDb.type.uppercase(ROOT)),
      enumValueOf(imageDb.family.uppercase(ROOT)),
      imageDb.fileUrl,
      imageDb.thumbnailUrl,
      AVAILABLE,
      ImageSource.fromKey(imageDb.source),
    )

  fun fromDatabase(imageDb: MovieImageDb): Image =
    Image(
      imageDb.id,
      IdTvdb(),
      IdTmdb(imageDb.idTmdb),
      enumValueOf(imageDb.type.uppercase(ROOT)),
      ImageFamily.MOVIE,
      imageDb.fileUrl,
      "",
      AVAILABLE,
      ImageSource.fromKey(imageDb.source),
    )

  fun toDatabaseShow(image: Image): ShowImageDb =
    ShowImageDb(
      idTvdb = image.idTvdb.id,
      idTmdb = image.idTmdb.id,
      type = image.type.key,
      family = image.family.key,
      fileUrl = image.fileUrl,
      thumbnailUrl = image.thumbnailUrl,
      source = image.source.key,
    )

  fun toDatabaseMovie(image: Image): MovieImageDb =
    MovieImageDb(
      idTmdb = image.idTmdb.id,
      type = image.type.key,
      fileUrl = image.fileUrl,
      source = image.source.key,
    )
}
