package com.wolf2.reader.mode.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wolf2.reader.mode.entity.BookMark

@Dao
interface BookMarkDao {
    @Query("SELECT * FROM BOOKMARK WHERE bookUuid=:bookUuid")
    fun queryByBookUuid(bookUuid: String): List<BookMark>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bookMark: BookMark)

    @Delete
    fun delete(bookMark: BookMark)
}