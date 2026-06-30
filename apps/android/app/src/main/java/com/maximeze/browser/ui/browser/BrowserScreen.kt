package com.maximeze.browser.ui.browser

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.maximeze.browser.ui.BrowserUiState
import com.maximeze.browser.data.model.Tab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    state: BrowserUiState,
    onNavigate: (String) -> Unit,
    onSearch: (String) -> Unit,
    onNewTab: () -> Unit,
    onCloseTab: (String) -> Unit,
    onShowTabGrid: () -> Unit,
    onPageLoadFinished: (tabId: String, url: String, title: String) -> Unit,
    onAddBookmark: (url: String, title: String) -> Unit,
) {
    val activeTab = state.tabs.find { it.id == state.activeTabId }
    var omniboxInput by remember(activeTab?.url) { mutableStateOf(activeTab?.url ?: "") }
    var omniboxFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab strip (compact, horizontal scroll)
        if (state.tabs.size > 1) {
            TabStrip(
                tabs = state.tabs,
                activeTabId = state.activeTabId,
                onClose = onCloseTab,
            )
        }

        // Web content (fills all space between toolbar areas)
        Box(modifier = Modifier.weight(1f)) {
            state.tabs.forEach { tab ->
                if (tab.id == state.activeTabId) {
                    BrowserWebView(
                        tab = tab,
                        onPageLoadFinished = { url, title ->
                            onPageLoadFinished(tab.id, url, title)
                            omniboxInput = url
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Omnibox suggestions overlay (shown when focused)
            if (omniboxFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                )
            }
        }

        // Bottom navigation bar — follows Android bottom nav pattern
        BottomNavigationBar(
            omniboxValue = omniboxInput,
            onOmniboxChange = { omniboxInput = it },
            onOmniboxFocused = { omniboxFocused = it },
            onNavigate = {
                val url = if (it.startsWith("http://") || it.startsWith("https://")) it
                else if (it.contains(".") && !it.contains(" ")) "https://$it"
                else {
                    onSearch(it)
                    focusManager.clearFocus()
                    return@BottomNavigationBar
                }
                onNavigate(url)
                focusManager.clearFocus()
                omniboxFocused = false
            },
            onBack = { /* WebView back via state */ },
            onForward = { /* WebView forward via state */ },
            onRefresh = { /* WebView reload via state */ },
            onNewTab = onNewTab,
            onShowTabGrid = onShowTabGrid,
            tabCount = state.tabs.size,
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: Tab,
    onPageLoadFinished: (url: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        onPageLoadFinished(url, view.title ?: url)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        // Handle all http/https URLs inside the WebView
                        val url = request.url.toString()
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            return false
                        }
                        return true
                    }
                }

                webViewRef.value = this
                if (tab.url.isNotBlank() && tab.url != "about:blank") {
                    loadUrl(tab.url)
                }
            }
        },
        update = { webView ->
            if (webView.url != tab.url && tab.url.isNotBlank() && tab.url != "about:blank") {
                webView.loadUrl(tab.url)
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun TabStrip(
    tabs: List<Tab>,
    activeTabId: String,
    onClose: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            val isActive = tab.id == activeTabId
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = tab.title.ifBlank { tab.url },
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { onClose(tab.id) },
                    modifier = Modifier.size(20.dp),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close tab", modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomNavigationBar(
    omniboxValue: String,
    onOmniboxChange: (String) -> Unit,
    onOmniboxFocused: (Boolean) -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onNewTab: () -> Unit,
    onShowTabGrid: () -> Unit,
    tabCount: Int,
) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            // Omnibox
            OutlinedTextField(
                value = omniboxValue,
                onValueChange = onOmniboxChange,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onNavigate(omniboxValue) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )

            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }

            IconButton(onClick = onShowTabGrid) {
                BadgedBox(
                    badge = {
                        if (tabCount > 1) {
                            Badge { Text(tabCount.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Default.Tab, contentDescription = "Tabs")
                }
            }
        }
    }
}
