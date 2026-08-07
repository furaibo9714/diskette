package io.github.furaibo9714.diskette.ui.main.delegates

import androidx.lifecycle.DefaultLifecycleObserver
import io.github.furaibo9714.diskette.databinding.ActivityMainBinding
import io.github.furaibo9714.diskette.ui.main.MainViewModel
import io.github.furaibo9714.diskette.ui_base.utilities.TipsHost
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.gone
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_model.Tip

interface TipsDelegate : TipsHost {
  fun registerTipsDelegate(
    viewModel: MainViewModel,
    binding: ActivityMainBinding,
  )

  fun showAllTips()

  fun hideAllTips()
}

class MainTipsDelegate :
  TipsDelegate,
  DefaultLifecycleObserver {

  private lateinit var viewModel: MainViewModel
  private lateinit var binding: ActivityMainBinding

  private val tips by lazy {
    mapOf(
      Tip.MENU_DISCOVER to binding.tutorialTipDiscover,
      Tip.MENU_MY_SHOWS to binding.tutorialTipMyShows,
      Tip.MENU_MODES to binding.tutorialTipModeMenu,
    )
  }

  override fun registerTipsDelegate(
    viewModel: MainViewModel,
    binding: ActivityMainBinding,
  ) {
    this.viewModel = viewModel
    this.binding = binding
    setupTips()
  }

  private fun setupTips() {
    tips.entries.forEach { (tip, view) ->
      view.visibleIf(!isTipShown(tip))
      view.onClick {
        it.gone()
        showTip(tip)
      }
    }
  }

  override fun setTipShow(tip: Tip) = viewModel.setTipShown(tip)

  override fun isTipShown(tip: Tip) = viewModel.isTipShown(tip)

  override fun showTip(tip: Tip) {
    binding.tutorialView.showTip(tip)
    setTipShow(tip)
  }

  override fun showAllTips() {
    tips.entries.forEach { (tip, view) -> view.visibleIf(!isTipShown(tip)) }
  }

  override fun hideAllTips() {
    tips.values.forEach { it.gone() }
  }
}
