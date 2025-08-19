package com.wolf2.reader.util

fun IntRange.contains(other: IntRange): Boolean {
    return this.first <= other.first && this.last >= other.last
}

fun IntRange.length(): Int {
    return this.last - this.first + 1
}

fun IntRange.index(pos: Int): Int {
    return this.first + pos
}