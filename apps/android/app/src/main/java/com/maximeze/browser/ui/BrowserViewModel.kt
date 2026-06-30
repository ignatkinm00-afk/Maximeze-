package com.maximeze.browser.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maximeze.browser.data.db.BrowserDatabase
import com.maximeze.browser.data.model.Bookmark
import com.maximeze.browser.data.model.HistoryEntry
import com.maximeze.browser.data.model.SearchEngine
import com.maximeze.browser.data.model.Tab
import com.maximeze.browser.data.model.buildUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class BrowserSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val blockAds: Boolean = true,
    val blockTrackers: Boolean = true,
    val doNotTrack: Boolean = false,
    val javaScriptEnabled: Boolean = true,
    val httpsOnly: Boolean = false,
    val safeSearch: Boolean = false,
    val showSearchSuggestions: Boolean = true,
    val cookiesEnabled: Boolean = true,
    val fontSize: Float = 1.0f,
    val openLinksInBackground: Boolean = false,
    val clearDataOnExit: Boolean = false,
    val askDownloadLocation: Boolean = true,
)

data class BrowserUiState(
    val tabs: List<Tab> = emptyList(),
    val activeTabId: String = "",
    val showTabGrid: Boolean = false,
    val showSettings: Boolean = false,
    val settings: BrowserSettings = BrowserSettings(),
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BrowserDatabase.getInstance(application)
    private val bookmarkDao = db.bookmarkDao()
    private val historyDao = db.historyDao()

    private val _uiState = MutableStateFlow(run {
        val firstTabId = UUID.randomUUID().toString()
        BrowserUiState(
            tabs = listOf(Tab(id = firstTabId, url = "about:blank", title = "Новая вкладка", isActive = true)),
            activeTabId = firstTabId,
        )
    })
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    val bookmarks = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history = historyDao.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun openTab(url: String, incognito: Boolean = false) {
        val tab = Tab(
            id = UUID.randomUUID().toString(),
            url = url,
            title = if (url == "about:blank") "Новая вкладка" else url,
            isIncognito = incognito,
            isActive = true,
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { it.copy(isActive = false) } + tab,
                activeTabId = tab.id,
                showTabGrid = false,
            )
        }
    }

    fun closeTab(tabId: String) {
        _uiState.update { state ->
            val remaining = state.tabs.filter { it.id != tabId }
            val closedWasActive = state.activeTabId == tabId
            if (remaining.isEmpty()) {
                val newTab = Tab(id = UUID.randomUUID().toString(), url = "about:blank", title = "Новая вкладка", isActive = true)
                state.copy(tabs = listOf(newTab), activeTabId = newTab.id)
            } else if (closedWasActive) {
                val newActive = remaining.last()
                state.copy(tabs = remaining.map { it.copy(isActive = it.id == newActive.id) }, activeTabId = newActive.id)
            } else {
                state.copy(tabs = remaining)
            }
        }
    }

    fun activateTab(tabId: String) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { it.copy(isActive = it.id == tabId) },
                activeTabId = tabId,
                showTabGrid = false,
            )
        }
    }

    fun navigate(url: String) {
        val id = _uiState.value.activeTabId
        _uiState.update { state ->
            state.copy(tabs = state.tabs.map {
                if (it.id == id) it.copy(url = url, isLoading = true, title = url) else it
            })
        }
    }

    fun search(query: String) {
        navigate(_uiState.value.settings.searchEngine.buildUrl(query))
    }

    fun onPageLoadFinished(tabId: String, url: String, title: String) {
        _uiState.update { state ->
            state.copy(tabs = state.tabs.map {
                if (it.id == tabId) it.copy(url = url, title = title.ifBlank { url }, isLoading = false) else it
            })
        }
        val isIncognito = _uiState.value.tabs.find { it.id == tabId }?.isIncognito ?: false
        if (!isIncognito && url != "about:blank") recordVisit(url, title)
    }

    fun toggleTabGrid() = _uiState.update { it.copy(showTabGrid = !it.showTabGrid) }

    fun toggleSettings() = _uiState.update { it.copy(showSettings = !it.showSettings) }

    fun updateSettings(update: (BrowserSettings) -> BrowserSettings) =
        _uiState.update { it.copy(settings = update(it.settings)) }

    // Bookmarks
    fun addBookmark(url: String, title: String) {
        viewModelScope.launch {
            bookmarkDao.insertBookmark(Bookmark(id = UUID.randomUUID().toString(), url = url, title = title))
        }
    }

    fun removeBookmark(bookmark: Bookmark) {
        viewModelScope.launch { bookmarkDao.deleteBookmark(bookmark) }
    }

    suspend fun isBookmarked(url: String): Boolean = bookmarkDao.getByUrl(url) != null

    // History
    private fun recordVisit(url: String, title: String) {
        viewModelScope.launch {
            val existing = historyDao.getByUrl(url)
            if (existing != null) {
                historyDao.updateVisit(url, title, System.currentTimeMillis())
            } else {
                historyDao.insertEntry(HistoryEntry(id = UUID.randomUUID().toString(), url = url, title = title))
            }
        }
    }

    fun clearHistory() = viewModelScope.launch { historyDao.clearAll() }
}
