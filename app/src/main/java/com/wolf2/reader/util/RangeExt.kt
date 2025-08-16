package com.wolf2.reader.util


fun IntRange.contains(other: IntRange): Boolean {
    return this.first <= other.first && this.last >= other.last
}