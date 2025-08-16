package com.wolf2.reader.mode.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wolf2.reader.mode.convert.RoomTypeConverters
import com.wolf2.reader.mode.dao.BookDao
import com.wolf2.reader.mode.dao.BookMarkDao
import com.wolf2.reader.mode.dao.FavoriteBookDao
import com.wolf2.reader.mode.dao.ReadRecordDao
import com.wolf2.reader.mode.entity.BookMark
import com.wolf2.reader.mode.entity.FavoriteBook
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.book.Book
import timber.log.Timber

private const val DATABASE_NAME = "reader"
private const val DATABASE_VERSION = 1

@TypeConverters(RoomTypeConverters::class)
@Database(
    entities = [Book::class, ReadRecord::class, FavoriteBook::class, BookMark::class],
    version = DATABASE_VERSION
)
private abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun readRecordDao(): ReadRecordDao
    abstract fun favoriteDao(): FavoriteBookDao
    abstract fun bookMarkDao(): BookMarkDao
}

object DatabaseHelper {
    private var appDatabase: AppDatabase? = null
    private lateinit var app: Application

    fun init(context: Application) {
        app = context
        Timber.d("init db: ${appDatabase()}")
    }

    private fun appDatabase(): AppDatabase {
        if (appDatabase != null) {
            return appDatabase!!
        } else {
            val database = Room.databaseBuilder(
                app,
                AppDatabase::class.java,
                DATABASE_NAME
            ).build()
            appDatabase = database
            return database
        }
    }

    fun bookDao(): BookDao {
        return appDatabase().bookDao()
    }

    fun readRecordDao(): ReadRecordDao {
        return appDatabase().readRecordDao()
    }

    fun favoriteBookDao(): FavoriteBookDao {
        return appDatabase().favoriteDao()
    }

    fun bookMarkDao(): BookMarkDao {
        return appDatabase().bookMarkDao()
    }
}