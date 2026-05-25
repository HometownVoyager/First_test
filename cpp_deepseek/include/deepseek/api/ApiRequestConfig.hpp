/**
 * @file ApiRequestConfig.hpp
 * @brief Configuration for a single API request.
 */

#ifndef DEEPSEEK_API_REQUEST_CONFIG_HPP
#define DEEPSEEK_API_REQUEST_CONFIG_HPP

#include "deepseek/api/DeepSeekModel.hpp"

namespace deepseek {
namespace api {

class ApiRequestConfig {
public:
    ApiRequestConfig();

    [[nodiscard]] DeepSeekModel getModel() const { return model_; }
    void setModel(DeepSeekModel model) { model_ = model; }
    
    [[nodiscard]] bool isThinkingEnabled() const { return thinkingEnabled_; }
    void setThinkingEnabled(bool enabled) { thinkingEnabled_ = enabled; }
    
    [[nodiscard]] double getTemperature() const { return temperature_; }
    void setTemperature(double temp) { temperature_ = temp; }
    
    [[nodiscard]] int getMaxTokens() const { return maxTokens_; }
    void setMaxTokens(int tokens) { maxTokens_ = tokens; }
    
    [[nodiscard]] double getTopP() const { return topP_; }
    void setTopP(double topP) { topP_ = topP; }
    
    [[nodiscard]] int getN() const { return n_; }
    void setN(int n) { n_ = n; }
    
    [[nodiscard]] bool isStream() const { return stream_; }
    void setStream(bool stream) { stream_ = stream; }

    [[nodiscard]] ApiRequestConfig copy() const;

private:
    DeepSeekModel model_;
    bool thinkingEnabled_;
    double temperature_;
    int maxTokens_;
    double topP_;
    int n_;
    bool stream_;
};

} // namespace api
} // namespace deepseek

#endif // DEEPSEEK_API_REQUEST_CONFIG_HPP
