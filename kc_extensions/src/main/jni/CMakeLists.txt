cmake_minimum_required(VERSION 2.8.12 FATAL_ERROR)

message("Building Loudness Measurement Library for ${CMAKE_SYSTEM}")


#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -msse2 -mfpmath=sse")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -msse2 -mfpmath=sse")

#if(ANDROID)
#message("Building for android")
#endif()
#if(LINUX)
#message("Building for linux")
#endif()
#if(APPLE)
#message("Building for apple")
#endif()
#if(MSVC OR MSYS OR MINGW)
#message("Building for windows")
#endif()

project(ebur128-jni)
add_subdirectory(libebur128)

#include_directories(${EBUR128_INCLUDE_DIR})
add_library(ebur128-jni SHARED JEBUR128)
target_link_libraries(ebur128-jni ebur128 android log)