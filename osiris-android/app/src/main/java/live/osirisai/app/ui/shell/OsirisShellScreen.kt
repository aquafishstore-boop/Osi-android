package live.osirisai.app.ui.shell

import android.app.Activity
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import live.osirisai.app.BuildConfig
import live.osirisai.app.R
import live.osirisai.app.connectivity.NetworkMonitor
import live.osirisai.app.ui.splash.OsirisSplash
import live.osirisai.app.ui.theme.OsirisCharcoal
import live.osirisai.app.ui.theme.OsirisGold
import live.osirisai.app.ui.theme.OsirisMuted
import live.osirisai.app.ui.theme.OsirisText
import live.osirisai.app.web.CustomTabsHelper
import live.osirisai.app.web.LocationBridge
import live.osirisai.app.web.OsirisJsBridge
import live.osirisai.app.web.OsirisWebViewClient
import live.osirisai.app.web.OsirisWebViewFactory
import live.osirisai.app.web.WebLoadEvent
import java.net.HttpURLConnection
import java.net.URL

private class ShellActions {
    var onWebEvent: (WebLoadEvent) -> Unit = {}
    var onShare: (String) -> Unit = {}
    var onRequestLocation: () -> Unit = {}
    var onOpenAbout: () -> Unit = {}
    var onHaptic: () -> Unit = {}
}

@Composable
fun OsirisShellScreen(initialUrl: String) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val networkMonitor = remember { NetworkMonitor(context) }
    val actions = remember { ShellActions() }

    var state by remember { mutableStateOf(ShellUiState(isOnline = networkMonitor.isOnline)) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var cacheClearedNotice by remember { mutableStateOf(false) }

    val latestInitialUrl by rememberUpdatedState(initialUrl)

    fun haptic() {
        ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.values.any { it }) {
            scope.launch {
                val loc = LocationBridge.fetchCurrentLocation(context)
                val wv = webViewRef
                if (loc != null && wv != null) {
                    LocationBridge.injectLocation(wv, loc.latitude, loc.longitude)
                }
            }
        }
    }

    fun requestLocate() {
        if (LocationBridge.hasPermission(context)) {
            scope.launch {
                val loc = LocationBridge.fetchCurrentLocation(context)
                val wv = webViewRef
                if (loc != null && wv != null) {
                    LocationBridge.injectLocation(wv, loc.latitude, loc.longitude)
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    fun reload() {
        state = state.copy(
            overlay = ShellOverlay.None,
            pageLoading = true,
            phase = if (state.healthOk) ShellPhase.Ready else ShellPhase.Splash,
        )
        webViewRef?.loadUrl(latestInitialUrl)
    }

    actions.onWebEvent = { event ->
        when (event) {
            is WebLoadEvent.PageStarted -> {
                state = state.copy(pageLoading = true)
            }
            is WebLoadEvent.PageFinished -> {
                state = state.copy(
                    pageLoading = false,
                    phase = ShellPhase.Ready,
                    overlay = if (state.isOnline) ShellOverlay.None else ShellOverlay.Offline,
                )
            }
            is WebLoadEvent.HttpError -> {
                if (state.isOnline) {
                    state = state.copy(
                        overlay = ShellOverlay.Error(
                            titleRes = R.string.error_title,
                            bodyRes = R.string.error_body,
                            detail = "${event.code}: ${event.description}",
                        ),
                        phase = ShellPhase.Ready,
                    )
                }
            }
            is WebLoadEvent.SslBlocked -> {
                state = state.copy(
                    overlay = ShellOverlay.Ssl,
                    phase = ShellPhase.Ready,
                )
            }
            is WebLoadEvent.ExternalUrl -> {
                CustomTabsHelper.open(context, event.url)
            }
            is WebLoadEvent.NavigationBlocked -> Unit
        }
    }
    actions.onShare = { text -> OsirisJsBridge.launchShareChooser(context, text) }
    actions.onRequestLocation = { requestLocate() }
    actions.onOpenAbout = { state = state.copy(showAbout = true) }
    actions.onHaptic = { haptic() }

    LaunchedEffect(Unit) {
        networkMonitor.connectivityFlow().collect { online ->
            state = state.copy(isOnline = online)
            if (!online) {
                state = state.copy(overlay = ShellOverlay.Offline)
            } else if (state.overlay is ShellOverlay.Offline) {
                state = state.copy(overlay = ShellOverlay.None)
                reload()
            }
        }
    }

    LaunchedEffect(Unit) {
        val ok = withContext(Dispatchers.IO) { pingHealth() }
        state = state.copy(healthOk = ok)
        delay(700)
        if (state.phase == ShellPhase.Splash && state.overlay == ShellOverlay.None) {
            state = state.copy(phase = ShellPhase.Ready)
        }
    }

    BackHandler {
        val wv = webViewRef
        when {
            state.showAbout -> state = state.copy(showAbout = false)
            state.showExitConfirm -> state = state.copy(showExitConfirm = false)
            wv != null -> {
                LocationBridge.injectEscapeOrClosePanel(wv) {
                    if (wv.canGoBack()) {
                        wv.goBack()
                    } else {
                        state = state.copy(showExitConfirm = true)
                    }
                }
            }
            else -> state = state.copy(showExitConfirm = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val bridge = OsirisJsBridge(
                    context = ctx.applicationContext,
                    callbacks = object : OsirisJsBridge.Callbacks {
                        override fun onShare(text: String) = actions.onShare(text)
                        override fun onRequestLocation() = actions.onRequestLocation()
                        override fun onOpenAbout() = actions.onOpenAbout()
                        override fun onHaptic() = actions.onHaptic()
                        override fun onLog(message: String) = Unit
                    },
                )
                val client = OsirisWebViewClient { event -> actions.onWebEvent(event) }
                OsirisWebViewFactory.create(ctx, bridge, client, WebChromeClient()).also { wv ->
                    webViewRef = wv
                    wv.loadUrl(initialUrl)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = context.getString(R.string.webview_cd) },
            update = { wv -> webViewRef = wv },
            onRelease = { wv ->
                (wv.parent as? ViewGroup)?.removeView(wv)
                wv.destroy()
                if (webViewRef === wv) webViewRef = null
            },
        )

        AnimatedVisibility(
            visible = state.phase == ShellPhase.Splash,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            OsirisSplash()
        }

        when (val overlay = state.overlay) {
            ShellOverlay.None -> Unit
            ShellOverlay.Offline -> ConnectionOverlay(
                title = stringResource(R.string.offline_title),
                body = stringResource(R.string.offline_body),
                onRetry = { reload() },
                onAbout = { state = state.copy(showAbout = true) },
            )
            ShellOverlay.Ssl -> ConnectionOverlay(
                title = stringResource(R.string.ssl_error_title),
                body = stringResource(R.string.ssl_error_body),
                onRetry = { reload() },
                onAbout = { state = state.copy(showAbout = true) },
            )
            is ShellOverlay.Error -> ConnectionOverlay(
                title = stringResource(overlay.titleRes),
                body = stringResource(overlay.bodyRes),
                detail = overlay.detail,
                onRetry = { reload() },
                onAbout = { state = state.copy(showAbout = true) },
            )
        }

        if (state.phase == ShellPhase.Ready && state.overlay == ShellOverlay.None) {
            IconButton(
                onClick = {
                    haptic()
                    state = state.copy(showAbout = true)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.about),
                    tint = OsirisMuted.copy(alpha = 0.55f),
                )
            }
        }

        if (state.showAbout) {
            AboutSheet(
                onDismiss = { state = state.copy(showAbout = false) },
                onOpenDocs = {
                    CustomTabsHelper.open(context, BuildConfig.OSIRIS_DOCS_URL)
                },
                onClearCache = {
                    webViewRef?.let { OsirisWebViewFactory.clearBrowsingData(it) }
                    cacheClearedNotice = true
                    haptic()
                },
            )
        }

        if (state.showExitConfirm) {
            AlertDialog(
                onDismissRequest = { state = state.copy(showExitConfirm = false) },
                title = { Text(stringResource(R.string.exit_confirm_title), color = OsirisText) },
                text = { Text(stringResource(R.string.exit_confirm_body), color = OsirisMuted) },
                confirmButton = {
                    TextButton(onClick = { (context as? Activity)?.finish() }) {
                        Text(stringResource(R.string.exit), color = OsirisGold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { state = state.copy(showExitConfirm = false) }) {
                        Text(stringResource(R.string.cancel), color = OsirisMuted)
                    }
                },
                containerColor = OsirisCharcoal,
            )
        }
    }

    DisposableEffect(cacheClearedNotice) {
        if (!cacheClearedNotice) {
            return@DisposableEffect onDispose { }
        }
        val job = scope.launch {
            delay(1_200)
            cacheClearedNotice = false
        }
        onDispose { job.cancel() }
    }
}

private fun pingHealth(): Boolean {
    return runCatching {
        val connection = (URL(BuildConfig.OSIRIS_HEALTH_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 4_000
            readTimeout = 4_000
            requestMethod = "GET"
            instanceFollowRedirects = false
        }
        try {
            connection.responseCode in 200..299
        } finally {
            connection.disconnect()
        }
    }.getOrDefault(false)
}
