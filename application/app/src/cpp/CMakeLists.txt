# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

#add_library( # Sets the name of the library.
#             native-lib
#
#             # Sets the library as a shared library.
#             SHARED
#
#             # Provides a relative path to your source file(s).
#             native-lib.cpp )


# OPENSSL 3.0
add_library(openssl STATIC IMPORTED)
set_target_properties(openssl PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/openssl-3.0.0-alpha6/build/lib/libssl.a)
add_library(crypto STATIC IMPORTED)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/openssl-3.0.0-alpha6/build/lib/libcrypto.a)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/openssl-3.0.0-alpha6/build/include)

## LIBCONFIG
#add_library(libconfig STATIC IMPORTED)
#set_target_properties(libconfig PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libconfig-1.7.2/build/lib/libconfig.a)
#include_directories(${CMAKE_CURRENT_SOURCE_DIR}/libconfig-1.7.2/build/include)

# ZLIB
add_library(zlib STATIC IMPORTED)
set_target_properties(zlib PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/zlib/build/lib/libz.a)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/zlib/build/include)

# CURL
add_library(libcurl STATIC IMPORTED)
set_target_properties(libcurl PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/curl/build/lib/libcurl.a)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/curl/build/include)

# LIBTIDY
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/tidy-html5 ${CMAKE_CURRENT_SOURCE_DIR}/tidy-html5/build)
add_library( libtidy STATIC IMPORTED )
set_target_properties( libtidy PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/tidy-html5/build/libtidys.a )
include_directories( ${CMAKE_CURRENT_SOURCE_DIR}/tidy-html5/include )

# PROJECT C CODE
add_library(phone-lib SHARED ${CMAKE_CURRENT_SOURCE_DIR}/download/src/main.c ${CMAKE_CURRENT_SOURCE_DIR}/download/src/encryption.c ${CMAKE_CURRENT_SOURCE_DIR}/download/src/key_tree.c ${CMAKE_CURRENT_SOURCE_DIR}/download/src/download.c)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/download/include)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

# LOG LIB
find_library(log-lib log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

#target_link_libraries( # Specifies the target library.
#                       native-lib
#
#                       # Links the target library to the log library
#                       # included in the NDK.
#                       ${log-lib} )

target_link_libraries(phone-lib openssl crypto zlib libcurl libtidy ${log-lib})
