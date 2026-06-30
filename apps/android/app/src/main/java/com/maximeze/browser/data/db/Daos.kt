package com.maximeze.browser.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maximeze.browser.data.model.Bookmark
import com.maximeze.browser.data.model.BookmarkFolder
import com.maximeze.browser.data.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmark_folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<BookmarkFolder>>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): Bookmark?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: BookmarkFolder)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteFolder(folder: BookmarkFolder)

    @Update
    suspend fun updateBookmark(bookmark: Bookmark)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 200): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): HistoryEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HistoryEntry)

    @Query("UPDATE history SET visitCount = visitCount + 1, visitedAt = :visitedAt, title = :title WHERE url = :url")
    suspend fun updateVisit(url: String, title: String, visitedAt: Long)

    @Query("DELETE FROM history")
    suspend fun clearAll()

    @Query("DELETE FROM history WHERE visitedAt < :cutoff")
    suspend fun clearOlderThan(cutoff: Long)

    @Query("""
        SELECT * FROM history
        WHERE url LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%'
        ORDER BY visitCount DESC, visitedAt DESC
        LIMIT :limit
    """)
    suspend fun search(query: String, limit: Int = 5): List<HistoryEntry>
}
