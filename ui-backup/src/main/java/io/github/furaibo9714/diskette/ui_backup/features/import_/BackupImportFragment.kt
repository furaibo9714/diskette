package io.github.furaibo9714.diskette.ui_backup.features.import_

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import io.github.furaibo9714.diskette.ui_backup.R
import io.github.furaibo9714.diskette.ui_backup.databinding.FragmentBackupImportBinding
import io.github.furaibo9714.diskette.ui_backup.features.export.cases.ReadBackupJsonFromFileUseCase
import io.github.furaibo9714.diskette.ui_backup.features.import_.model.BackupImportStatus.Idle
import io.github.furaibo9714.diskette.ui_backup.features.import_.model.BackupImportStatus.Importing
import io.github.furaibo9714.diskette.ui_backup.features.import_.model.BackupImportStatus.Initializing
import io.github.furaibo9714.diskette.ui_base.BaseFragment
import io.github.furaibo9714.diskette.ui_base.utilities.SnackbarHost
import io.github.furaibo9714.diskette.ui_base.utilities.events.MessageEvent.Error
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.doOnApplyWindowInsets
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.launchAndRepeatStarted
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.onClick
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.showErrorSnackbar
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.showInfoSnackbar
import io.github.furaibo9714.diskette.ui_base.utilities.extensions.visibleIf
import io.github.furaibo9714.diskette.ui_base.utilities.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class BackupImportFragment : BaseFragment<BackupImportViewModel>(R.layout.fragment_backup_import) {

  @Inject
  lateinit var readBackupJsonFromFileUseCase: ReadBackupJsonFromFileUseCase

  override val viewModel by viewModels<BackupImportViewModel>()
  private val binding by viewBinding(FragmentBackupImportBinding::bind)

  private val pickFileContract = registerForActivityResult(
    OpenDocument(),
  ) { uri ->
    uri?.let { readImportFile(it) }
  }

  private var snackbar: Snackbar? = null

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupInsets()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
    )

    activity?.window?.addFlags(FLAG_KEEP_SCREEN_ON)
  }

  private fun setupView() {
    with(binding) {
      toolbar.onClick { activity?.onBackPressed() }
      importButton.onClick { openNewImport() }
    }
  }

  private fun setupInsets() {
    with(binding) {
      root.doOnApplyWindowInsets { view, insets, padding, _ ->
        val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
          top = padding.top + inset.top,
          bottom = padding.bottom + inset.bottom,
        )
      }
    }
  }

  private fun openNewImport() {
    pickFileContract.launch(arrayOf("application/json"))
  }

  private fun readImportFile(uri: Uri) {
    readBackupJsonFromFileUseCase(requireContext(), uri).fold(
      onSuccess = { viewModel.runImport(it) },
      onFailure = {
        showErrorSnack(it)
        Timber.e(it)
      },
    )
  }

  private fun showSuccessSnack() {
    val host = (requireActivity() as SnackbarHost).provideSnackbarLayout()
    snackbar = host.showInfoSnackbar(
      message = getString(R.string.textBackupImportSuccess),
    )
  }

  private fun showErrorSnack(error: Throwable) {
    if (error is CancellationException) {
      return
    }
    val host = (requireActivity() as SnackbarHost).provideSnackbarLayout()
    snackbar = host.showErrorSnackbar(
      message = error.localizedMessage ?: getString(R.string.errorGeneral),
    )
  }

  override fun onDestroyView() {
    if (viewModel.uiState.value.isImporting != Idle) {
      showSnack(Error(R.string.errorImportCancelled))
    } else {
      snackbar?.dismiss()
    }

    activity?.window?.clearFlags(FLAG_KEEP_SCREEN_ON)
    super.onDestroyView()
  }

  private fun render(uiState: BackupImportUiState) {
    uiState.run {
      with(binding) {
        progressBar.visibleIf(isImporting != Idle)
        importButton.visibleIf(isImporting == Idle, gone = false)
        importButton.isEnabled = isImporting == Idle
      }
      renderImportStatus(uiState)

      if (isSuccess) {
        showSuccessSnack()
        viewModel.clearState()
      }

      if (isError != null) {
        showErrorSnack(isError)
        viewModel.clearState()
      }
    }
  }

  private fun renderImportStatus(uiState: BackupImportUiState) {
    with(binding) {
      statusText.visibleIf(uiState.isImporting != Idle)
      statusText.text = when (uiState.isImporting) {
        is Idle -> ""
        is Initializing -> "Importing..."
        is Importing -> {
          val status = uiState.isImporting
          "Importing...\n\n\"${status.title}\"\n(${status.count}/${status.total})"
        }
      }
    }
  }
}
