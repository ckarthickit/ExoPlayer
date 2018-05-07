cmake_minimum_required(VERSION 2.8.12 FATAL_ERROR)

message("Building Loudness Measurement Library")

project(ebur128-jni)
add_subdirectory(libebur128)

#include_directories(${EBUR128_INCLUDE_DIR})
add_library(ebur128-jni SHARED JEBUR128)
target_link_libraries(ebur128-jni ebur128 android log)