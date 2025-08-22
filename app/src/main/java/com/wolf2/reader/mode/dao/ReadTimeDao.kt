package com.wolf2.reader.mode.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wolf2.reader.mode.entity.ReadTime
import kotlinx.coroutines.flow.Flow


@Dao
interface ReadTimeDao {
    @Query("SELECT * FROM READTIME WHERE bookUuid=:bookUuid")
    fun queryByBookUuid(bookUuid: String): List<ReadTime>

    @Query("SELECT * FROM READTIME ORDER BY endReadTimeMillis")
    fun observeAllReadTimes(): Flow<List<ReadTime>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(readTime: ReadTime)
}