
cmake_minimum_required(VERSION 3.22.1)

project("reader_jni")

add_subdirectory("libmobi")
add_subdirectory("zlib")
add_subdirectory("libxml2")
add_subdirectory("minizip")
add_subdirectory("EPUB3Processor")

add_library(reader_jni SHARED
        MobiReader_jni.cpp
        EpubReader_jni.cpp)

target_link_libraries(reader_jni
        android
        log
        mobi
        EPUB3Processor)