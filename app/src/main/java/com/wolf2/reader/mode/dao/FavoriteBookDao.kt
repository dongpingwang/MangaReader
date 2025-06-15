package com.wolf2.reader.mode.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wolf2.reader.mode.entity.FavoriteBook

@Dao
interface FavoriteBookDao {

    @Query("SELECT * FROM FAVORITEBOOK WHERE bookUuid=:bookUuid")
    fun getFavoriteBook(bookUuid: String): FavoriteBook?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(favorite: FavoriteBook)

    @Delete
    fun delete(favorite: FavoriteBook)
}