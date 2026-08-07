package io.github.furaibo9714.diskette.ui_settings.sections.floppy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.furaibo9714.diskette.ui_base.utilities.events.MessageEvent
import io.github.furaibo9714.diskette.ui_base.viewmodel.ChannelsDelegate
import io.github.furaibo9714.diskette.ui_base.viewmodel.DefaultChannelsDelegate
import io.github.furaibo9714.diskette.ui_settings.R
import io.github.furaibo9714.diskette.ui_settings.sections.floppy.cases.SettingsFloppyCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsFloppyViewModel @Inject constructor(
  private val floppyCase: SettingsFloppyCase,
) : ViewModel(),
  ChannelsDelegate by DefaultChannelsDelegate() {

  private val uiStateFlow = MutableStateFlow(SettingsFloppyUiState())
  val uiState: StateFlow<SettingsFloppyUiState> = uiStateFlow.asStateFlow()

  fun loadSettings() {
    uiStateFlow.value = uiStateFlow.value.copy(
      isConfigured = floppyCase.isConfigured(),
      baseUrl = floppyCase.getBaseUrl().orEmpty(),
    )
  }

  fun testConnection(
    baseUrl: String,
    apiKey: String,
  ) {
    viewModelScope.launch {
      uiStateFlow.value = uiStateFlow.value.copy(isTesting = true)
      val result = floppyCase.testConnection(baseUrl, apiKey)
      result
        .onSuccess {
          floppyCase.saveConnection(baseUrl, apiKey)
          uiStateFlow.value = uiStateFlow.value.copy(isTesting = false, isConfigured = true, baseUrl = baseUrl)
          messageChannel.send(MessageEvent.Info(R.string.textFloppyConnectionSuccess))
        }.onFailure {
          uiStateFlow.value = uiStateFlow.value.copy(isTesting = false)
          messageChannel.send(MessageEvent.Error(R.string.textFloppyConnectionError))
        }
    }
  }

  fun disconnect() {
    floppyCase.disconnect()
    uiStateFlow.value = SettingsFloppyUiState(isConfigured = false)
  }
}
