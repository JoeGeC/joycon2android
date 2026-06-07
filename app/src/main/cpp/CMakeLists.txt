cmake_minimum_required(VERSION 3.22.1)
project(uhid_gamepad C)

add_library(uhid_gamepad SHARED uhid_gamepad.c)
find_library(log-lib log)
target_link_libraries(uhid_gamepad ${log-lib})

# Executable disguised as shared library for APK packaging.
# Android's build system only packages lib*.so from native builds.
add_executable(uhid_relay uhid_relay.c)
set_target_properties(uhid_relay PROPERTIES
    OUTPUT_NAME "libuhid_relay"
    SUFFIX ".so"
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}"
)
