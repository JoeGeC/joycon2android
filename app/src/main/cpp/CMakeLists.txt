cmake_minimum_required(VERSION 3.22.1)
project(uhid_relay C)

# Executable disguised as shared library for APK packaging.
# Android's build system only packages lib*.so from native builds.
add_executable(uhid_relay uhid_relay.c)
set_target_properties(uhid_relay PROPERTIES
    OUTPUT_NAME "libuhid_relay"
    SUFFIX ".so"
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}"
)

add_executable(uinput_relay uinput_relay.c)
set_target_properties(uinput_relay PROPERTIES
    OUTPUT_NAME "libuinput_relay"
    SUFFIX ".so"
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}"
)
