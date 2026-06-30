package com.maximeze.browser.ui.browser

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
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
    val currentUrl = activeTab?.url ?: ""
    var omniboxInput by remember(currentUrl) {
        mutableStateOf(TextFieldValue(currentUrl, TextRange(0, currentUrl.length)))
    }
    var omniboxFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val omniboxFocusRequester = remember { FocusRequester() }

    LaunchedEffect(omniboxFocused) {
        if (omniboxFocused) omniboxFocusRequester.requestFocus()
    }

    // imePadding() поднимает весь UI над клавиатурой
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Полоска вкладок (когда их больше одной)
        if (state.tabs.size > 1) {
            TabStrip(
                tabs = state.tabs,
                activeTabId = state.activeTabId,
                onClose = onCloseTab,
            )
        }

        // Индикатор загрузки
        if (activeTab?.isLoading == true) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent,
            )
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Веб-контент
        Box(modifier = Modifier.weight(1f)) {
            val isHome = currentUrl.isBlank() || currentUrl == "about:blank"
            if (isHome) {
                HomeScreen(
                    onSearch = { query ->
                        onSearch(query)
                        omniboxInput = TextFieldValue(query, TextRange(query.length))
                    },
                    onNavigate = { url ->
                        onNavigate(url)
                        omniboxInput = TextFieldValue(url, TextRange(url.length))
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
                                omniboxInput = TextFieldValue(url, TextRange(url.length))
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            // Затемнение при открытой строке поиска
            if (omniboxFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable {
                            omniboxFocused = false
                            focusManager.clearFocus()
                        }
                )
            }
        }

        // Нижняя панель
        BottomBar(
            activeTab = activeTab,
            omniboxInput = omniboxInput,
            omniboxFocused = omniboxFocused,
            omniboxFocusRequester = omniboxFocusRequester,
            onOmniboxChange = { omniboxInput = it },
            onOmniboxFocused = { omniboxFocused = it },
            onSubmit = {
                val raw = omniboxInput.text.trim()
                val url = resolveInput(raw)
                if (url != null) onNavigate(url) else onSearch(raw)
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

// ──────────────────────────────────────────────────────────────
// Полоска вкладок
// ──────────────────────────────────────────────────────────────

@Composable
private fun TabStrip(
    tabs: List<Tab>,
    activeTabId: String,
    onClose: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(MaterialTheme.colorScheme.surface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            val isActive = tab.id == activeTabId
            Surface(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp, vertical = 3.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                tonalElevation = if (isActive) 3.dp else 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (tab.isIncognito) {
                        Icon(
                            Icons.Default.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(3.dp))
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
                        fontSize = 11.sp,
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable { onClose(tab.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Закрыть",
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Нижняя панель навигации
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBar(
    activeTab: Tab?,
    omniboxInput: TextFieldValue,
    omniboxFocused: Boolean,
    omniboxFocusRequester: FocusRequester,
    onOmniboxChange: (TextFieldValue) -> Unit,
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
        tonalElevation = 4.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )

            AnimatedContent(
                targetState = omniboxFocused,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "omnibox_mode",
            ) { focused ->
                if (focused) {
                    // ── Режим поиска / ввода адреса ──
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        IconButton(
                            onClick = { onOmniboxFocused(false) },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Отмена",
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        TextField(
                            value = omniboxInput,
                            onValueChange = onOmniboxChange,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(omniboxFocusRequester),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    "Поиск или адрес сайта",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary,
                            ),
                            shape = RoundedCornerShape(28.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        )

                        IconButton(
                            onClick = onSubmit,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Поиск",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                } else {
                    // ── Компактный режим ──
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        // Назад
                        IconButton(
                            onClick = { /* back via WebView */ },
                            modifier = Modifier.size(42.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        // Адресная строка-таблетка
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { onOmniboxFocused(true) },
                            shape = RoundedCornerShape(21.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 0.dp,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                val url = activeTab?.url ?: ""
                                val isSecure = url.startsWith("https://")
                                val isHome = url.isBlank() || url == "about:blank"

                                if (isHome) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Поиск или адрес",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                } else {
                                    Icon(
                                        if (isSecure) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (isSecure) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.error,
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = getDisplayHost(url),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }

                        // Кнопка вкладок
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center,
                        ) {
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
                                    Icon(
                                        Icons.Default.Tab,
                                        contentDescription = "Вкладки",
                                        modifier = Modifier.size(22.dp),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }

                        // Меню
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(42.dp),
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Меню",
                                    modifier = Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
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

            // Отступ для system navigation bar (жест-навигация)
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

// ──────────────────────────────────────────────────────────────
// WebView
// ──────────────────────────────────────────────────────────────

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

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        val url = request.url.toString()
                        if (settings.httpsOnly && url.startsWith("http://")) {
                            view.loadUrl(url.replace("http://", "https://"))
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

// ──────────────────────────────────────────────────────────────
// Утилиты
// ──────────────────────────────────────────────────────────────

private fun resolveInput(input: String): String? {
    val t = input.trim()
    if (t.startsWith("http://") || t.startsWith("https://")) return t
    if (t.contains(".") && !t.contains(" ")) return "https://$t"
    return null
}

private fun getDisplayHost(url: String): String {
    return try {
        URI(url).host?.removePrefix("www.") ?: url
    } catch (_: Exception) {
        url
    }
}
