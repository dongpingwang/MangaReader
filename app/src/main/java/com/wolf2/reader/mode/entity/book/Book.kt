package com.wolf2.reader.mode.entity.book

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "Book")
data class Book(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    var uuid: String = Uuid.random().toString(), // uuid，主键，自动生成
    @ColumnInfo(name = "uri")
    var uri: Uri = Uri.EMPTY, // 文件uri
    @ColumnInfo(name = "title")
    var title: String = "", // 书名
    @ColumnInfo(name = "author")
    var author: String = "", // 作者
    @ColumnInfo(name = "mimeType")
    var mimeType: String = "",// mimeType
    @ColumnInfo(name = "coverImage")
    var cover: CoverImage = CoverImage(),// 封面
    @Ignore
    var chapters: List<Chapter> = emptyList(), // 章节信息
    @Ignore
    var pageContents: List<PageContent> = emptyList(), // 每页内容
    @ColumnInfo(name = "lastAddedTimeMillis")
    var lastAddedTimeMillis: Long = System.currentTimeMillis(), // 添加时间
    @ColumnInfo(name = "extraInfo")
    var extraInfo: ExtraInfo = ExtraInfo() // 其他信息
)