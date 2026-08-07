package io.github.furaibo9714.diskette.ui_settings.sections.floppy

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.furaibo9714.diskette.ui_base.BaseFragment
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.launchAndRepeatStarted
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_base.utilities.viewBinding
import io.github.furaibo9714.diskette.ui_settings.R
import io.github.furaibo9714.diskette.ui_settings.databinding.FragmentSettingsFloppyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFloppyFragment : BaseFragment<SettingsFloppyViewModel>(R.layout.fragment_settings_floppy) {

  override val viewModel by viewModels<SettingsFloppyViewModel>()
  private val binding by viewBinding(FragmentSettingsFloppyBinding::bind)

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    setupClicks()
    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } },
      doAfterLaunch = { viewModel.loadSettings() },
    )
  }

  private fun setupClicks() {
    with(binding) {
      floppyTestConnectionButton.onClick {
        val url = floppyUrlInput.text?.toString().orEmpty().trim()
        val apiKey = floppyApiKeyInput.text?.toString().orEmpty().trim()
        if (url.isBlank() || apiKey.isBlank()) {
          return@onClick
        }
        viewModel.testConnection(url, apiKey)
      }
      floppyDisconnectButton.onClick { showDisconnectDialog() }
    }
  }

  private fun render(uiState: SettingsFloppyUiState) {
    with(binding) {
      floppyConnectSection.visibleIf(!uiState.isConfigured)
      floppyConfiguredSection.visibleIf(uiState.isConfigured)
      floppyTestProgress.visibleIf(uiState.isTesting)
      floppyTestConnectionButton.visibleIf(!uiState.isTesting)

      if (uiState.isConfigured) {
        floppyConnectedSummary.text = getString(R.string.textFloppyConnectedTo, uiState.baseUrl)
      }
    }
  }

  private fun showDisconnectDialog() {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textFloppyDisconnect)
      .setMessage(R.string.textFloppyDisconnectMessage)
      .setPositiveButton(R.string.textYes) { _, _ -> viewModel.disconnect() }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }
}
