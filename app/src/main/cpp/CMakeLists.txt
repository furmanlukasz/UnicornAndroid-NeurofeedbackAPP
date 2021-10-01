
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("unicornandroidapi")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set (OBOE_DIR oboe)

add_subdirectory(${OBOE_DIR} ./oboe)

include_directories(${OBOE_DIR}/include)
include_directories(${OBOE_DIR}/samples/shared)

# Debug utilities
set (DEBUG_UTILS_PATH "${OBOE_DIR}/samples/debug-utils")
set (DEBUG_UTILS_SOURCES ${DEBUG_UTILS_PATH}/trace.cpp)
include_directories(${DEBUG_UTILS_PATH})

add_library( # Sets the name of the library.
        unicornandroidapi

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        unicornandroidapi.cpp )

# App specific sources
set (APP_SOURCES
        jni_bridge.cpp
        HelloOboeEngine.cpp
        SoundGenerator.cpp
        LatencyTuningCallback.cpp
        )

add_library(my_oboe    SHARED    ${DEBUG_UTILS_SOURCES}    ${APP_SOURCES})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        unicornandroidapi

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )

target_link_libraries(my_oboe android ${log-lib}  oboe)
