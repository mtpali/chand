cmake_minimum_required(VERSION 3.22.1)

project(cndx LANGUAGES CXX)

add_library(cndx SHARED promo_guard.cpp)

target_compile_features(cndx PRIVATE cxx_std_17)
target_compile_options(cndx PRIVATE
    -O3
    -fvisibility=hidden
    -fvisibility-inlines-hidden
    -ffunction-sections
    -fdata-sections
    -fno-exceptions
    -fno-rtti
)
target_link_options(cndx PRIVATE
    -Wl,--gc-sections
    -Wl,--exclude-libs,ALL
)
