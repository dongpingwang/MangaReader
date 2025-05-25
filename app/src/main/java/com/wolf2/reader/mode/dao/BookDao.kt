package com.wolf2.reader.mode.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wolf2.reader.mode.entity.book.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM BOOK")
    fun getAll(): List<Book>

    @Query("SELECT * FROM BOOK")
    fun observeAll(): Flow<List<Book>>

    @Query("SELECT * FROM BOOK WHERE uri = :uri")
    fun queryByUri(uri: Uri): Book?

    @Insert
    fun insertAll(songs: List<Book>)

    @Query("DELETE FROM BOOK")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(book: Book)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(book: Book)

    @Delete
    fun delete(book: Book)
}