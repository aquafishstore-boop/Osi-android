package live.osirisai.app.ui.shell

sealed interface ShellPhase {
    data object Splash : ShellPhase
    data object Ready : ShellPhase
}

sealed interface ShellOverlay {
    data object None : ShellOverlay
    data object Offline : ShellOverlay
    data class Error(val titleRes: Int, val bodyRes: Int, val detail: String? = null) : ShellOverlay
    data object Ssl : ShellOverlay
}

data class ShellUiState(
    val phase: ShellPhase = ShellPhase.Splash,
    val overlay: ShellOverlay = ShellOverlay.None,
    val showAbout: Boolean = false,
    val showExitConfirm: Boolean = false,
    val isOnline: Boolean = true,
    val pageLoading: Boolean = true,
    val healthOk: Boolean = false,
)
