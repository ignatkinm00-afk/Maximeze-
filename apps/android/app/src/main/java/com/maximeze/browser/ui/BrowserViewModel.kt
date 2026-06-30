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

data class BrowserUiState(
    val tabs: List<Tab> = listOf(Tab(id = UUID.randomUUID().toString(), url = "about:blank")),
    val activeTabId: String = "",
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val isIncognitoMode: Boolean = false,
    val showTabGrid: Boolean = false,
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BrowserDatabase.getInstance(application)
    private val bookmarkDao = db.bookmarkDao()
    private val historyDao = db.historyDao()

    private val _uiState = MutableStateFlow(
        BrowserUiState(
            activeTabId = UUID.randomUUID().toString()
        ).let { state ->
            val firstTabId = UUID.randomUUID().toString()
            state.copy(
                tabs = listOf(
                    Tab(
                        id = firstTabId,
                        url = "about:blank",
                        title = "New Tab",
                        isActive = true,
                    )
                ),
                activeTabId = firstTabId,
            )
        }
    )
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    val bookmarks = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history = historyDao.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun openTab(url: String, incognito: Boolean = false) {
        val tab = Tab(
            id = UUID.randomUUID().toString(),
            url = url,
            isIncognito = incognito,
            isActive = true,
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { it.copy(isActive = false) } + tab,
                activeTabId = tab.id,
            )
        }
    }

    fun closeTab(tabId: String) {
        _uiState.update { state ->
            val newTabs = state.tabs.filter { it.id != tabId }
            val closedWasActive = state.tabs.find { it.id == tabId }?.isActive == true

            if (newTabs.isEmpty()) {
                // Open a fresh tab when the last one is closed
                val newTab = Tab(
                    id = UUID.randomUUID().toString(),
                    url = "about:blank",
                    title = "New Tab",
                    isActive = true,
                )
                state.copy(tabs = listOf(newTab), activeTabId = newTab.id)
            } else if (closedWasActive) {
                val activatedTabs = newTabs.mapIndexed { i, t ->
                    t.copy(isActive = i == newTabs.lastIndex)
                }
                state.copy(tabs = activatedTabs, activeTabId = activatedTabs.last().id)
            } else {
                state.copy(tabs = newTabs)
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
        val activeId = _uiState.value.activeTabId
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == activeId) it.copy(url = url, isLoading = true, title = url)
                    else it
                }
            )
        }
    }

    fun search(query: String) {
        val url = _uiState.value.searchEngine.buildUrl(query)
        navigate(url)
    }

    fun onPageLoadFinished(tabId: String, url: String, title: String) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map {
                    if (it.id == tabId) it.copy(url = url, title = title, isLoading = false)
                    else it
                }
            )
        }
        if (!_uiState.value.tabs.find { it.id == tabId }?.isIncognito!!) {
            recordVisit(url, title)
        }
    }

    fun toggleTabGrid() {
        _uiState.update { it.copy(showTabGrid = !it.showTabGrid) }
    }

    fun setSearchEngine(engine: SearchEngine) {
        _uiState.update { it.copy(searchEngine = engine) }
    }

    // Bookmarks
    fun addBookmark(url: String, title: String) {
        viewModelScope.launch {
            val bookmark = Bookmark(
                id = UUID.randomUUID().toString(),
                url = url,
                title = title,
            )
            bookmarkDao.insertBookmark(bookmark)
        }
    }

    fun removeBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmark(bookmark)
        }
    }

    suspend fun isBookmarked(url: String): Boolean =
        bookmarkDao.getByUrl(url) != null

    // History
    private fun recordVisit(url: String, title: String) {
        viewModelScope.launch {
            val existing = historyDao.getByUrl(url)
            if (existing != null) {
                historyDao.updateVisit(url, title, System.currentTimeMillis())
            } else {
                historyDao.insertEntry(
                    HistoryEntry(
                        id = UUID.randomUUID().toString(),
                        url = url,
                        title = title,
                    )
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch { historyDao.clearAll() }
    }
}
