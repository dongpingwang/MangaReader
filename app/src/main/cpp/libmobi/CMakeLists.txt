
cmake_minimum_required(VERSION 3.22.1)

project("mobi")

set(mobi_SOURCES
		${CMAKE_CURRENT_SOURCE_DIR}/buffer.c
		${CMAKE_CURRENT_SOURCE_DIR}/buffer.h
		${CMAKE_CURRENT_SOURCE_DIR}/compression.c
		${CMAKE_CURRENT_SOURCE_DIR}/compression.h
		${CMAKE_CURRENT_SOURCE_DIR}/config.h
		${CMAKE_CURRENT_SOURCE_DIR}/debug.c
		${CMAKE_CURRENT_SOURCE_DIR}/debug.h
		${CMAKE_CURRENT_SOURCE_DIR}/index.c
		${CMAKE_CURRENT_SOURCE_DIR}/index.h
		${CMAKE_CURRENT_SOURCE_DIR}/memory.c
		${CMAKE_CURRENT_SOURCE_DIR}/memory.h
		${CMAKE_CURRENT_SOURCE_DIR}/meta.c
		${CMAKE_CURRENT_SOURCE_DIR}/meta.h
		${CMAKE_CURRENT_SOURCE_DIR}/mobi.h
		${CMAKE_CURRENT_SOURCE_DIR}/parse_rawml.c
		${CMAKE_CURRENT_SOURCE_DIR}/parse_rawml.h
		${CMAKE_CURRENT_SOURCE_DIR}/read.c
		${CMAKE_CURRENT_SOURCE_DIR}/read.h
		${CMAKE_CURRENT_SOURCE_DIR}/structure.c
		${CMAKE_CURRENT_SOURCE_DIR}/structure.h
		${CMAKE_CURRENT_SOURCE_DIR}/util.c
		${CMAKE_CURRENT_SOURCE_DIR}/util.h
		${CMAKE_CURRENT_SOURCE_DIR}/write.c
		${CMAKE_CURRENT_SOURCE_DIR}/write.h
)

set(USE_ENCRYPTION OFF)
set(USE_XMLWRITER OFF)
set(USE_ENCRYPTION OFF)
set(USE_MINIZ OFF)
set(USE_LIBXML2 ON)
set(USE_ZLIB ON)

if (USE_ENCRYPTION)
	list(APPEND mobi_SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/encryption.c
			${CMAKE_CURRENT_SOURCE_DIR}/encryption.h
			${CMAKE_CURRENT_SOURCE_DIR}/sha1.c
			${CMAKE_CURRENT_SOURCE_DIR}/sha1.h
			${CMAKE_CURRENT_SOURCE_DIR}/randombytes.c
			${CMAKE_CURRENT_SOURCE_DIR}/randombytes.h)
endif (USE_ENCRYPTION)

if (USE_XMLWRITER)
	list(APPEND mobi_SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/opf.c
			${CMAKE_CURRENT_SOURCE_DIR}/opf.h)
	if (NOT USE_LIBXML2)
		list(APPEND mobi_SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/xmlwriter.c
				${CMAKE_CURRENT_SOURCE_DIR}/xmlwriter.h)
	endif (NOT USE_LIBXML2)
endif (USE_XMLWRITER)


add_library(mobi SHARED ${mobi_SOURCES})
target_link_libraries(mobi PUBLIC android log)


#set_target_properties(mobi PROPERTIES
#                      OUTPUT_NAME "mobi"
#                      SOVERSION ${PACKAGE_VERSION_MAJOR}
#                      VERSION "${PACKAGE_VERSION}"
#  POSITION_INDEPENDENT_CODE ${BUILD_SHARED_LIBS}
#  C_VISIBILITY_PRESET hidden
#  VISIBILITY_INLINES_HIDDEN ON
#  MACOSX_RPATH 1)

if (USE_MINIZ)
	set(miniz_SOURCES
			${CMAKE_CURRENT_SOURCE_DIR}/miniz.c
			${CMAKE_CURRENT_SOURCE_DIR}/miniz.h
	)
	add_library(miniz OBJECT ${miniz_SOURCES})
	target_compile_definitions(miniz PRIVATE
			MINIZ_NO_STDIO
			MINIZ_NO_ZLIB_COMPATIBLE_NAMES
			MINIZ_NO_TIME
			MINIZ_NO_ARCHIVE_APIS
			MINIZ_NO_ARCHIVE_WRITING_APIS
			_POSIX_C_SOURCE=200112L)
	target_link_libraries(mobi PRIVATE miniz)
endif (USE_MINIZ)

if (USE_LIBXML2)
#	find_package(LibXml2 REQUIRED)
	target_link_libraries(mobi PUBLIC LibXml2::LibXml2)
endif (USE_LIBXML2)

if (USE_ZLIB)
#	find_package(ZLIB REQUIRED)
#	target_link_libraries(mobi PUBLIC ZLIB::ZLIB)
	target_link_libraries(mobi PUBLIC zlib)
	target_link_libraries(mobi PUBLIC minizip)
endif (USE_ZLIB)