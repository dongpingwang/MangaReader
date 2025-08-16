package com.wolf2.reader.mode.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "BookMark")
data class BookMark(
    @PrimaryKey
    var _uuid: String = Uuid.random().toString(),// uuid，主键，自动生成，**无用**
    var bookUuid: String = "", // 书籍uuid
    @ColumnInfo(name = "pageIndex")
    var pageIndex: Int = 0, // 书签位置页面，从0开始
    @ColumnInfo(name = "chapterName")
    var chapterName: String = "",// 章节名称
    @ColumnInfo(name = "lastAddedTimeMillis")
    var lastAddedTimeMillis: Long = System.currentTimeMillis() // 最后添加时间
)