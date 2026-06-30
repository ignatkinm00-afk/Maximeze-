package com.maximeze.browser.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maximeze.browser.ui.browser.BrowserScreen
import com.maximeze.browser.ui.tabs.TabGridScreen

@Composable
fun MaximizeApp(vm: BrowserViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        if (state.showTabGrid) {
            TabGridScreen(
                tabs = state.tabs,
                onTabSelect = { vm.activateTab(it) },
                onTabClose = { vm.closeTab(it) },
                onNewTab = { vm.openTab("about:blank") },
                onDismiss = { vm.toggleTabGrid() },
            )
        } else {
            BrowserScreen(
                state = state,
                onNavigate = { vm.navigate(it) },
                onSearch = { vm.search(it) },
                onNewTab = { vm.openTab("about:blank") },
                onCloseTab = { vm.closeTab(it) },
                onShowTabGrid = { vm.toggleTabGrid() },
                onPageLoadFinished = { tabId, url, title ->
                    vm.onPageLoadFinished(tabId, url, title)
                },
                onAddBookmark = { url, title -> vm.addBookmark(url, title) },
            )
        }
    }
}
