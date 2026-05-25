/**
 * @file ApiResponse.hpp
 * @brief Represents the response from a DeepSeek API call.
 */

#ifndef DEEPSEEK_API_RESPONSE_HPP
#define DEEPSEEK_API_RESPONSE_HPP

#include <string>
#include <cstdint>

namespace deepseek {
namespace api {

class ApiResponse {
public:
    ApiResponse();

    [[nodiscard]] const std::string& getId() const { return id_; }
    void setId(const std::string& id) { id_ = id; }
    
    [[nodiscard]] const std::string& getContent() const { return content_; }
    void setContent(const std::string& content) { content_ = content; }
    
    [[nodiscard]] const std::string& getThoughtContent() const { return thoughtContent_; }
    void setThoughtContent(const std::string& thoughtContent) { thoughtContent_ = thoughtContent_; }
    
    [[nodiscard]] int getPromptTokens() const { return promptTokens_; }
    void setPromptTokens(int tokens) { promptTokens_ = tokens; }
    
    [[nodiscard]] int getCompletionTokens() const { return completionTokens_; }
    void setCompletionTokens(int tokens) { completionTokens_ = tokens; }
    
    [[nodiscard]] int getTotalTokens() const { return totalTokens_; }
    void setTotalTokens(int tokens) { totalTokens_ = tokens; }
    
    [[nodiscard]] int64_t getTimestamp() const { return timestamp_; }
    void setTimestamp(int64_t timestamp) { timestamp_ = timestamp; }
    
    [[nodiscard]] bool isSuccess() const { return success_; }
    void setSuccess(bool success) { success_ = success; }
    
    [[nodiscard]] const std::string& getErrorMessage() const { return errorMessage_; }
    void setErrorMessage(const std::string& error) { errorMessage_ = error; }

    static ApiResponse success(const std::string& id, const std::string& content,
                               const std::string& thoughtContent,
                               int promptTokens, int completionTokens);
    static ApiResponse error(const std::string& errorMessage);

private:
    std::string id_;
    std::string content_;
    std::string thoughtContent_;
    int promptTokens_;
    int completionTokens_;
    int totalTokens_;
    int64_t timestamp_;
    bool success_;
    std::string errorMessage_;
};

} // namespace api
} // namespace deepseek

#endif // DEEPSEEK_API_RESPONSE_HPP
