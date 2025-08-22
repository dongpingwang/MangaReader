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
    fun getAllBooks() :List<Book>


    @Query(
    """
    SELECT b.*, MAX(rr.lastReadTimeMillis) as last_read_time FROM BOOK b
    LEFT JOIN 
    READRECORD rr ON b.uuid = rr.bookUuid GROUP BY b.uuid ORDER BY last_read_time DESC
    """
    )
    fun allBooksSortReadTime(): PagingSource<Int, Book>

    @Query("SELECT * FROM BOOK ORDER BY title DESC")
    fun allBooksSortTitle(): PagingSource<Int, Book>

    @Query("SELECT * FROM BOOK ORDER BY author DESC")
    fun allBooksSortAuthor(): PagingSource<Int, Book>

    @Query(
    """
    SELECT BOOK.*,READRECORD.* FROM Book
    INNER JOIN READRECORD ON BOOK.uuid == READRECORD.bookUuid
    ORDER BY READRECORD.lastReadTimeMillis DESC     
    """
    )
    fun allBooksFilterReading(): PagingSource<Int, Book>

    @Query(
    """
    SELECT BOOK.* FROM BOOK
    LEFT JOIN READRECORD ON BOOK.uuid = READRECORD.bookUuid
    WHERE READRECORD.bookUuid IS NULL
    ORDER BY BOOK.lastAddedTimeMillis DESC
    """
    )
    fun allBooksFilterUnRead(): PagingSource<Int, Book>

    @Query(
    """
    SELECT BOOK.*,FAVORITEBOOK.* FROM Book
    INNER JOIN FAVORITEBOOK ON BOOK.uuid == FAVORITEBOOK.bookUuid 
    """
    )
    fun allBooksFilterFavorite(): PagingSource<Int, Book>

    @Query("SELECT * FROM BOOK WHERE title LIKE '%' || :keyword || '%' OR author LIKE '%' || :keyword || '%'")
    fun search(keyword: String): List<Book>

    @Query("SELECT * FROM BOOK WHERE uri = :uri")
    fun queryByUri(uri: Uri): Book?

    @Query("SELECT * FROM BOOK WHERE uuid = :uuid")
    fun queryByUuid(uuid: String): Book?

    @Insert
    fun insertAll(books: List<Book>)

    @Query("DELETE FROM BOOK")
    fun deleteAll()

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