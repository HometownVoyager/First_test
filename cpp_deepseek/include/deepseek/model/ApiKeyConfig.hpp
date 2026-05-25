/**
 * @file ApiKeyConfig.hpp
 * @brief Represents an API Key configuration with metadata.
 */

#ifndef DEEPSEEK_API_KEY_CONFIG_HPP
#define DEEPSEEK_API_KEY_CONFIG_HPP

#include <string>
#include <cstdint>

namespace deepseek {
namespace model {

class ApiKeyConfig {
public:
    ApiKeyConfig();
    ApiKeyConfig(const std::string& name, const std::string& apiKey, const std::string& endpoint);

    [[nodiscard]] const std::string& getId() const { return id_; }
    void setId(const std::string& id) { id_ = id; }
    
    [[nodiscard]] const std::string& getName() const { return name_; }
    void setName(const std::string& name) { name_ = name; }
    
    [[nodiscard]] const std::string& getApiKey() const { return apiKey_; }
    void setApiKey(const std::string& apiKey) { apiKey_ = apiKey; }
    
    [[nodiscard]] const std::string& getEndpoint() const { return endpoint_; }
    void setEndpoint(const std::string& endpoint) { endpoint_ = endpoint; }
    
    [[nodiscard]] int64_t getCreatedAt() const { return createdAt_; }
    void setCreatedAt(int64_t createdAt) { createdAt_ = createdAt; }
    
    [[nodiscard]] int64_t getUpdatedAt() const { return updatedAt_; }
    void setUpdatedAt(int64_t updatedAt) { updatedAt_ = updatedAt; }

    [[nodiscard]] std::string getMaskedApiKey() const;
    [[nodiscard]] std::string toString() const;

private:
    std::string id_;
    std::string name_;
    std::string apiKey_;
    std::string endpoint_;
    int64_t createdAt_;
    int64_t updatedAt_;
};

} // namespace model
} // namespace deepseek

#endif // DEEPSEEK_API_KEY_CONFIG_HPP
