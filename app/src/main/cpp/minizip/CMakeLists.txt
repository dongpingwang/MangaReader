
cmake_minimum_required(VERSION 3.22.1)

project("minizip")

add_library(minizip SHARED crypt.h
        ioapi.c
        ioapi.h
        minizip.c
        mztools.c
        mztools.h
        unzip.c
        unzip.h
        zip.c
        zip.h
)
target_include_directories(minizip PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})  # 暴露头文件路径
target_link_libraries(minizip PUBLIC zlib)
