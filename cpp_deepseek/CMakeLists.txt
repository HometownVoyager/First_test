cmake_minimum_required(VERSION 3.14)
project(deepseek_chat_app VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Compiler warnings
if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
    add_compile_options(-Wall -Wextra -Wpedantic)
elseif(MSVC)
    add_compile_options(/W4)
endif()

# Source files
set(SOURCES
    src/model/Message.cpp
    src/model/RoleCard.cpp
    src/model/Conversation.cpp
    src/model/WorldBookEntry.cpp
    src/model/WorldBook.cpp
    src/model/ApiKeyConfig.cpp
    src/api/ApiResponse.cpp
    src/api/ApiRequestConfig.cpp
)

set(HEADERS
    include/deepseek/model/Message.hpp
    include/deepseek/model/RoleCard.hpp
    include/deepseek/model/Conversation.hpp
    include/deepseek/model/WorldBookEntry.hpp
    include/deepseek/model/WorldBook.hpp
    include/deepseek/model/ApiKeyConfig.hpp
    include/deepseek/api/DeepSeekModel.hpp
    include/deepseek/api/ApiResponse.hpp
    include/deepseek/api/ApiRequestConfig.hpp
)

# Create library
add_library(deepseek_lib STATIC ${SOURCES} ${HEADERS})
target_include_directories(deepseek_lib PUBLIC 
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    $<INSTALL_INTERFACE:include>
)

# Find CURL for HTTP requests
find_package(CURL REQUIRED)
target_link_libraries(deepseek_lib PRIVATE CURL::libcurl)

# Find nlohmann_json for JSON processing
find_package(nlohmann_json QUIET)
if(nlohmann_json_FOUND)
    target_link_libraries(deepseek_lib PRIVATE nlohmann_json::nlohmann_json)
else()
    # Fetch nlohmann_json if not found
    include(FetchContent)
    FetchContent_Declare(
        json
        URL https://github.com/nlohmann/json/releases/download/v3.11.2/json.tar.xz
    )
    FetchContent_MakeAvailable(json)
    target_link_libraries(deepseek_lib PRIVATE nlohmann_json::nlohmann_json)
endif()

# Main executable (console application)
add_executable(deepseek_console src/main_console.cpp)
target_link_libraries(deepseek_console PRIVATE deepseek_lib)

# Install targets
install(TARGETS deepseek_lib deepseek_console
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
    RUNTIME DESTINATION bin
)
install(DIRECTORY include/ DESTINATION include)
