package io.github.furaibo9714.diskette.common.errors

import io.github.furaibo9714.diskette.common.errors.DisketteError.AccountLimitsError
import io.github.furaibo9714.diskette.common.errors.DisketteError.AccountLockedError
import io.github.furaibo9714.diskette.common.errors.DisketteError.CoroutineCancellation
import io.github.furaibo9714.diskette.common.errors.DisketteError.ResourceConflictError
import io.github.furaibo9714.diskette.common.errors.DisketteError.ResourceNotFoundError
import io.github.furaibo9714.diskette.common.errors.DisketteError.UnauthorizedError
import io.github.furaibo9714.diskette.common.errors.DisketteError.UnknownError
import io.github.furaibo9714.diskette.common.errors.DisketteError.UnknownHttpError
import io.github.furaibo9714.diskette.common.errors.DisketteError.ValidationError
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

object ErrorHelper {

  fun parse(error: Throwable): DisketteError =
    when (error) {
      is DisketteError -> {
        error
      }
      is HttpException -> {
        when (error.code()) {
          in arrayOf(401, 403) -> UnauthorizedError(error.message)
          404 -> ResourceNotFoundError
          409 -> ResourceConflictError
          420 -> AccountLimitsError
          422 -> ValidationError
          423 -> AccountLockedError
          else -> UnknownHttpError(error.message)
        }
      }
      is CancellationException -> {
        CoroutineCancellation
      }
      else -> {
        UnknownError(error.message)
      }
    }
}
