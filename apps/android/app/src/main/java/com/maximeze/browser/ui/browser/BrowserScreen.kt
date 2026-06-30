package com.maximeze.browser.ui.browser

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.maximeze.browser.data.model.Tab
import com.maximeze.browser.ui.BrowserSettings
import com.maximeze.browser.ui.BrowserUiState
import com.maximeze.browser.ui.home.HomeScreen
import java.net.URI

@Composable
fun BrowserScreen(
    state: BrowserUiState,
    onNavigate: (String) -> Unit,
    onSearch: (String) -> Unit,
    onNewTab: () -> Unit,
    onCloseTab: (String) -> Unit,
    onShowTabGrid: () -> Unit,
    onShowSettings: () -> Unit,
    onPageLoadFinished: (tabId: String, url: String, title: String) -> Unit,
    onAddBookmark: (url: String, title: String) -> Unit,
) {
    val activeTab = state.tabs.find { it.id == state.activeTabId }
    var omniboxInput by remember(activeTab?.url) { mutableStateOf(activeTab?.url ?: "") }
    var omniboxFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab strip (shown when multiple tabs)
            if (state.tabs.size > 1) {
                ModernTabStrip(
                    tabs = state.tabs,
                    activeTabId = state.activeTabId,
                    onClose = onCloseTab,
                )
            }

            // Loading progress bar
            if (activeTab?.isLoading == true) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Web content
            Box(modifier = Modifier.weight(1f)) {
                val isHome = activeTab?.url.isNullOrBlank() || activeTab?.url == "about:blank"
                if (isHome) {
                    HomeScreen(
                        onSearch = { query ->
                            onSearch(query)
                            omniboxInput = query
                        },
                        onNavigate = { url ->
                            onNavigate(url)
                            omniboxInput = url
                        },
                    )
                } else {
                    state.tabs.forEach { tab ->
                        if (tab.id == state.activeTabId) {
                            BrowserWebView(
                                tab = tab,
                                settings = state.settings,
                                onPageLoadFinished = { url, title ->
                                    onPageLoadFinished(tab.id, url, title)
                                    omniboxInput = url
                                },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }

                // Scrim when searching
                if (omniboxFocused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable {
                                omniboxFocused = false
                                focusManager.clearFocus()
                            }
                    )
                }
            }

            // Bottom navigation bar
            ModernBottomBar(
                activeTab = activeTab,
                omniboxValue = omniboxInput,
                omniboxFocused = omniboxFocused,
                onOmniboxChange = { omniboxInput = it },
                onOmniboxFocused = { omniboxFocused = it },
                onSubmit = {
                    val url = resolveInput(omniboxInput)
                    if (url != null) {
                        onNavigate(url)
                    } else {
                        onSearch(omniboxInput)
                    }
                    focusManager.clearFocus()
                    omniboxFocused = false
                },
                onShowTabGrid = onShowTabGrid,
                onNewTab = onNewTab,
                onShowSettings = onShowSettings,
                onAddBookmark = {
                    activeTab?.let { tab -> onAddBookmark(tab.url, tab.title) }
                },
                tabCount = state.tabs.size,
            )
        }
    }
}

@Composable
private fun ModernTabStrip(
    tabs: List<Tab>,
    activeTabId: String,
    onClose: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .horizontalScroll(rememberScrollState())
            .height(40.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        tabs.forEach { tab ->
            val isActive = tab.id == activeTabId
            Surface(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                tonalElevation = if (isActive) 4.dp else 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (tab.isIncognito) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = tab.title.ifBlank { "Новая вкладка" },
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(
                        onClick = { onClose(tab.id) },
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernBottomBar(
    activeTab: Tab?,
    omniboxValue: String,
    omniboxFocused: Boolean,
    onOmniboxChange: (String) -> Unit,
    onOmniboxFocused: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onShowTabGrid: () -> Unit,
    onNewTab: () -> Unit,
    onShowSettings: () -> Unit,
    onAddBookmark: () -> Unit,
    tabCount: Int,
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            if (omniboxFocused) {
                // Expanded search mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { onOmniboxFocused(false) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Отмена")
                    }
                    OutlinedTextField(
                        value = omniboxValue,
                        onValueChange = onOmniboxChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Поиск или адрес сайта", fontSize = 14.sp) },
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                    )
                    IconButton(onClick = onSubmit) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                // Compact mode — pill + controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Back button
                    IconButton(onClick = { /* handled via WebView ref */ }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", modifier = Modifier.size(22.dp))
                    }

                    // Address pill (tap to expand)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { onOmniboxFocused(true) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            val url = activeTab?.url ?: ""
                            val isSecure = url.startsWith("https://")
                            val isHome = url.isBlank() || url == "about:blank"

                            if (isHome) {
                                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Новая вкладка", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Icon(
                                    if (isSecure) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = getDisplayHost(url),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    // Tabs button
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        BadgedBox(
                            badge = {
                                if (tabCount > 1) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text(tabCount.toString(), fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = onShowTabGrid) {
                                Icon(Icons.Default.Tab, contentDescription = "Вкладки", modifier = Modifier.size(22.dp))
                            }
                        }
                    }

                    // More menu
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Меню", modifier = Modifier.size(22.dp))
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Новая вкладка") },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                onClick = { onNewTab(); showMenu = false },
                            )
                            DropdownMenuItem(
                                text = { Text("Инкогнито") },
                                leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                                onClick = { showMenu = false },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Добавить закладку") },
                                leadingIcon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null) },
                                onClick = { onAddBookmark(); showMenu = false },
                            )
                            DropdownMenuItem(
                                text = { Text("Поделиться") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = { showMenu = false },
                            )
                            DropdownMenuItem(
                                text = { Text("Найти на странице") },
                                leadingIcon = { Icon(Icons.Default.FindInPage, contentDescription = null) },
                                onClick = { showMenu = false },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Настройки") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = { onShowSettings(); showMenu = false },
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: Tab,
    settings: BrowserSettings,
    onPageLoadFinished: (url: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                this.settings.javaScriptEnabled = settings.javaScriptEnabled
                this.settings.domStorageEnabled = true
                this.settings.loadWithOverviewMode = true
                this.settings.useWideViewPort = true
                this.settings.setSupportZoom(true)
                this.settings.builtInZoomControls = true
                this.settings.displayZoomControls = false
                this.settings.allowFileAccess = false
                this.settings.allowContentAccess = false
                this.settings.textZoom = (settings.fontSize * 100).toInt()

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        onPageLoadFinished(url, view.title ?: url)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val url = request.url.toString()
                        if (settings.httpsOnly && url.startsWith("http://")) {
                            val httpsUrl = url.replace("http://", "https://")
                            view.loadUrl(httpsUrl)
                            return true
                        }
                        return !(url.startsWith("http://") || url.startsWith("https://"))
                    }
                }

                if (tab.url.isNotBlank() && tab.url != "about:blank") loadUrl(tab.url)
            }
        },
        update = { webView ->
            if (webView.url != tab.url && tab.url.isNotBlank() && tab.url != "about:blank") {
                webView.loadUrl(tab.url)
            }
            webView.settings.javaScriptEnabled = settings.javaScriptEnabled
            webView.settings.textZoom = (settings.fontSize * 100).toInt()
        },
        modifier = modifier,
    )
}

private fun resolveInput(input: String): String? {
    val trimmed = input.trim()
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    if (trimmed.contains(".") && !trimmed.contains(" ")) return "https://$trimmed"
    return null
}

private fun getDisplayHost(url: String): String {
    return try {
        URI(url).host?.removePrefix("www.") ?: url
    } catch (_: Exception) {
        url
    }
}
