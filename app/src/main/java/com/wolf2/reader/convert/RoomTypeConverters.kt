package com.wolf2.reader.convert

import android.net.Uri
import androidx.room.TypeConverter
import com.wolf2.reader.mode.entity.book.CoverImage
import androidx.core.net.toUri
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

}