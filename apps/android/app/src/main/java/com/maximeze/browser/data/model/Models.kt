package com.maximeze.browser.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val favicon: String? = null,
    val folderId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "bookmark_folders")
data class BookmarkFolder(
    @PrimaryKey val id: String,
    val name: String,
    val parentId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val favicon: String? = null,
    val visitedAt: Long = System.currentTimeMillis(),
    val visitCount: Int = 1,
)

data class Tab(
    val id: String,
    val url: String,
    val title: String = url,
    val favicon: String? = null,
    val isLoading: Boolean = false,
    val isActive: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isIncognito: Boolean = false,
    val isSuspended: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
)

enum class SearchEngine(val id: String, val displayName: String, val searchUrl: String) {
    GOOGLE("google", "Google", "https://www.google.com/search?q=%s"),
    BING("bing", "Bing", "https://www.bing.com/search?q=%s"),
    DUCKDUCKGO("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q=%s"),
}

fun SearchEngine.buildUrl(query: String): String =
    searchUrl.format(java.net.URLEncoder.encode(query, "UTF-8"))
