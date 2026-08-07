package io.github.furaibo9714.diskette.common.errors

sealed class DisketteError(
  errorMessage: String?,
) : Throwable(errorMessage) {

  object ValidationError : DisketteError("ValidationError")

  object ResourceConflictError : DisketteError("ResourceConflictError")

  object ResourceNotFoundError : DisketteError("ResourceNotFoundError")

  object AccountLockedError : DisketteError("AccountLockedError")

  object AccountLimitsError : DisketteError("AccountLimitsError")

  data class UnauthorizedError(
    val errorMessage: String?,
  ) : DisketteError(errorMessage)

  data class UnknownHttpError(
    val errorMessage: String?,
  ) : DisketteError(errorMessage)

  data class UnknownError(
    val errorMessage: String?,
  ) : DisketteError(errorMessage)

  object CoroutineCancellation : DisketteError("")
}
