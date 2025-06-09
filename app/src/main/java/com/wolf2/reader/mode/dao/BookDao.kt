package com.wolf2.reader.mode.dao

import android.net.Uri
import androidx.paging.PagingSource
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
    fun allBooks(): PagingSource<Int, Book>

    @Query("SELECT * FROM BOOK")
    fun observeAll(): Flow<List<Book>>

    @Query("SELECT * FROM BOOK WHERE uri = :uri")
    fun queryByUri(uri: Uri): Book?

    @Query("SELECT * FROM BOOK WHERE uuid = :uuid")
    fun queryByUuid(uuid: String): Book?

    @Insert
    fun insertAll(books: List<Book>)

    @Query("DELETE FROM BOOK")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(book: Book)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(book: Book)

    @Delete
    fun delete(book: Book)

    @Query(
        """
        SELECT book.* FROM book
        INNER JOIN (
            SELECT bookUuid, MAX(lastReadTimeMillis) AS max_time
            FROM READRECORD
            GROUP BY bookUuid
            ORDER BY max_time DESC
            LIMIT 1
        ) AS latest_record ON book.uuid = latest_record.bookUuid
    """
    )
    fun observeLatestReadBook(): Flow<Book?>
}