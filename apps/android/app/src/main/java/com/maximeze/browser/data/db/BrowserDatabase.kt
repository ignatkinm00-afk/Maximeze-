package com.maximeze.browser.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.maximeze.browser.data.model.Bookmark
import com.maximeze.browser.data.model.BookmarkFolder
import com.maximeze.browser.data.model.HistoryEntry

@Database(
    entities = [Bookmark::class, BookmarkFolder::class, HistoryEntry::class],
    version = 1,
    exportSchema = true,
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: BrowserDatabase? = null

        fun getInstance(context: Context): BrowserDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    BrowserDatabase::class.java,
                    "maximeze_browser.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
