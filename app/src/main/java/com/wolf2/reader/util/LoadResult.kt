package com.wolf2.reader.util

sealed class LoadResult<out T> {
    data object Loading : LoadResult<Nothing>()
    data class Success<T>(val data: T) : LoadResult<T>()
    data class Error(val exception: Throwable?) : LoadResult<Nothing>()
}