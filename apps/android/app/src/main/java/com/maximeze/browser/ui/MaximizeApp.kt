package com.maximeze.browser.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maximeze.browser.ui.browser.BrowserScreen
import com.maximeze.browser.ui.settings.SettingsScreen
import com.maximeze.browser.ui.tabs.TabGridScreen
import com.maximeze.browser.ui.theme.MaximizeTheme

@Composable
fun MaximizeApp(vm: BrowserViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    val darkTheme = when (state.settings.theme) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaximizeTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                state.showSettings -> {
                    SettingsScreen(
                        settings = state.settings,
                        onUpdateSettings = { vm.updateSettings { _ -> it } },
                        onClearHistory = { vm.clearHistory() },
                        onDismiss = { vm.toggleSettings() },
                    )
                }
                state.showTabGrid -> {
                    TabGridScreen(
                        tabs = state.tabs,
                        onTabSelect = { vm.activateTab(it) },
                        onTabClose = { vm.closeTab(it) },
                        onNewTab = { vm.openTab("about:blank") },
                        onDismiss = { vm.toggleTabGrid() },
                    )
                }
                else -> {
                    BrowserScreen(
                        state = state,
                        onNavigate = { vm.navigate(it) },
                        onSearch = { vm.search(it) },
                        onNewTab = { vm.openTab("about:blank") },
                        onCloseTab = { vm.closeTab(it) },
                        onShowTabGrid = { vm.toggleTabGrid() },
                        onShowSettings = { vm.toggleSettings() },
                        onPageLoadFinished = { tabId, url, title ->
                            vm.onPageLoadFinished(tabId, url, title)
                        },
                        onAddBookmark = { url, title -> vm.addBookmark(url, title) },
                    )
                }
            }
        }
    }
}
