
cmake_minimum_required(VERSION 3.22.1)

project("EPUB3Processor")


add_library(EPUB3Processor SHARED
        EPUB3_private.h
        EPUB3.h
        EPUB3.c)

target_link_libraries(EPUB3Processor
        minizip
        zlib
        LibXml2::LibXml2
        android
        log)