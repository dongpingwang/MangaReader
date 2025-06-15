package com.wolf2.reader.mode.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.wolf2.reader.mode.entity.book.Book

@Entity(
    tableName = "FavoriteBook", foreignKeys = [ForeignKey(
        entity = Book::class,
        parentColumns = ["uuid"],
        childColumns = ["bookUuid"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FavoriteBook(
    @PrimaryKey
    @ColumnInfo(name = "bookUuid")
    var bookUuid: String = "" // 书籍uuid
)