package com.wolf2.reader.mode.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "ReadTime")
data class ReadTime @OptIn(ExperimentalUuidApi::class) constructor(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    var _uuid: String = Uuid.random().toString(),// uuid，主键，自动生成，**无用**
    @ColumnInfo(name = "bookUuid")
    var bookUuid: String = "", // 书籍uuid
    @ColumnInfo(name = "startReadTimeMillis")
    var startReadTimeMillis: Long = System.currentTimeMillis(), // 开始阅读时间戳
    @ColumnInfo(name = "endReadTimeMillis")
    var endReadTimeMillis: Long = System.currentTimeMillis(), // 结束阅读时间戳
    @ColumnInfo(name = "date")
    var date: String = "", // 日期，如20250821
)