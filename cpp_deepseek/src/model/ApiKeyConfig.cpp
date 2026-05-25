/**
 * @file ApiKeyConfig.cpp
 * @brief Implementation of the ApiKeyConfig class.
 */

#include "deepseek/model/ApiKeyConfig.hpp"
#include <chrono>
#include <sstream>

namespace deepseek {
namespace model {

namespace {
std::string generateUuid() {
    static std::random_device rd;
    static std::mt19937_64 gen(rd());
    static std::uniform_int_distribution<uint64_t> dis;
    
    uint64_t uuid = dis(gen);
    std::stringstream ss;
    ss << std::hex << std::setfill('0');
    ss << std::setw(8) << (uuid >> 32) << "-";
    ss << std::setw(4) << ((uuid >> 16) & 0xFFFF) << "-";
    ss << std::setw(4) << (uuid & 0xFFFF) << "-";
    ss << std::setw(4) << (dis(gen) >> 48) << "-";
    ss << std::setw(12) << dis(gen);
    return ss.str();
}
} // anonymous namespace

ApiKeyConfig::ApiKeyConfig()
    : id_(generateUuid())
    , endpoint_("https://api.deepseek.com")
    , createdAt_(std::chrono::duration_cast<std::chrono::milliseconds>(
          std::chrono::system_clock::now().time_since_epoch()).count())
    , updatedAt_(createdAt_) {
}

ApiKeyConfig::ApiKeyConfig(const std::string& name, const std::string& apiKey, const std::string& endpoint)
    : ApiKeyConfig() {
    name_ = name;
    apiKey_ = apiKey;
    endpoint_ = endpoint.empty() ? "https://api.deepseek.com" : endpoint;
}

std::string ApiKeyConfig::getMaskedApiKey() const {
    if (apiKey_.length() < 8) {
        return "****";
    }
    return apiKey_.substr(0, 4) + "..." + apiKey_.substr(apiKey_.length() - 4);
}

std::string ApiKeyConfig::toString() const {
    return name_.empty() ? getMaskedApiKey() : name_;
}

} // namespace model
} // namespace deepseek
