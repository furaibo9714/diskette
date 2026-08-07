package io.github.furaibo9714.diskette.ui_base.utilities

import io.github.furaibo9714.diskette.ui_model.Tip

interface TipsHost {
  fun isTipShown(tip: Tip): Boolean

  fun showTip(tip: Tip)

  fun setTipShow(tip: Tip)
}
