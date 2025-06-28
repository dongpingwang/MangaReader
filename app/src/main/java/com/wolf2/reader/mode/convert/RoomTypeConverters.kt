package com.wolf2.reader.mode.convert

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter
import com.wolf2.reader.mode.entity.book.CoverImage
import com.wolf2.reader.mode.entity.book.ExtraInfo
import kotlinx.serialization.json.Json

class RoomTypeConverters {

    @TypeConverter
    fun coverImageToRoom(coverImage: CoverImage): String {
        return Json.encodeToString(coverImage)
    }

    @TypeConverter
    fun coverImageFromRoom(value: String): CoverImage {
        return Json.decodeFromString<CoverImage>(value)
    }

    @TypeConverter
    fun uriToRoom(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun uriFromRoom(value: String): Uri {
        return value.toUri()
    }

    @TypeConverter
    fun extraInfoToRoom(extraInfo: ExtraInfo): String {
        return Json.encodeToString(extraInfo)
    }

    @TypeConverter
    fun extraInfoFromRoom(value: String): ExtraInfo {
        return Json.decodeFromString<ExtraInfo>(value)
    }

}