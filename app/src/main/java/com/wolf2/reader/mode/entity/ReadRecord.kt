package com.wolf2.reader.mode.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.wolf2.reader.mode.entity.book.Book

@Entity(
    tableName = "ReadRecord", foreignKeys = [ForeignKey(
        entity = Book::class,
        parentColumns = ["uuid"],
        childColumns = ["bookUuid"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ReadRecord(
    @PrimaryKey
    @ColumnInfo(name = "bookUuid")
    var bookUuid: String = "", // 书籍uuid
    @ColumnInfo(name = "curPage")
    var curPage: Int = 0, // 阅读到的页面，从0开始
    @ColumnInfo(name = "pageCount")
    var pageCount: Int = 0, // 总页面数
    @ColumnInfo(name = "lastReadTimeMillis")
    var lastReadTimeMillis: Long = System.currentTimeMillis() // 最后阅读时间
)