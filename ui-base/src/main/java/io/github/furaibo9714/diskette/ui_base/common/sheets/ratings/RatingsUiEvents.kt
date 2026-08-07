@file:Suppress("ktlint:standard:filename")

package io.github.furaibo9714.diskette.ui_base.common.sheets.ratings

import io.github.furaibo9714.diskette.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation
import io.github.furaibo9714.diskette.ui_base.utilities.events.Event

data class FinishUiEvent(
  val operation: Operation,
) : Event<Operation>(operation)
