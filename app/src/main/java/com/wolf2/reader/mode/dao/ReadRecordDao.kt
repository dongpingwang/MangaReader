package com.wolf2.reader.mode.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wolf2.reader.mode.entity.ReadRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadRecordDao {

    @Query("SELECT * FROM READRECORD")
    fun observeAll(): Flow<List<ReadRecord>>

    @Query("SELECT COUNT(*) FROM READRECORD WHERE curPage >= 0 AND curPage < (pageCount-1)")
    fun observeReadingCount(): Flow<Int>

    @Query("SELECT * FROM READRECORD WHERE bookUuid=:bookUuid")
    fun getReadRecord(bookUuid: String): ReadRecord?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(record: ReadRecord)

    @Delete
    fun delete(record: ReadRecord)
}