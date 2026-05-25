/**
 * @file ApiResponse.cpp
 * @brief Implementation of the ApiResponse class.
 */

#include "deepseek/api/ApiResponse.hpp"
#include <chrono>

namespace deepseek {
namespace api {

ApiResponse::ApiResponse()
    : timestamp_(std::chrono::duration_cast<std::chrono::milliseconds>(
          std::chrono::system_clock::now().time_since_epoch()).count())
    , success_(true)
    , promptTokens_(0)
    , completionTokens_(0)
    , totalTokens_(0) {
}

ApiResponse ApiResponse::success(const std::string& id, const std::string& content,
                                  const std::string& thoughtContent,
                                  int promptTokens, int completionTokens) {
    ApiResponse response;
    response.setId(id);
    response.setContent(content);
    response.setThoughtContent(thoughtContent);
    response.setPromptTokens(promptTokens);
    response.setCompletionTokens(completionTokens);
    response.setTotalTokens(promptTokens + completionTokens);
    return response;
}

ApiResponse ApiResponse::error(const std::string& errorMessage) {
    ApiResponse response;
    response.setSuccess(false);
    response.setErrorMessage(errorMessage);
    return response;
}

} // namespace api
} // namespace deepseek
