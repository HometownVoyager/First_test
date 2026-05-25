/**
 * @file ApiRequestConfig.cpp
 * @brief Implementation of the ApiRequestConfig class.
 */

#include "deepseek/api/ApiRequestConfig.hpp"

namespace deepseek {
namespace api {

ApiRequestConfig::ApiRequestConfig()
    : model_(DeepSeekModel::DEEPSEEK_CHAT)
    , thinkingEnabled_(false)
    , temperature_(0.7)
    , maxTokens_(2048)
    , topP_(1.0)
    , n_(1)
    , stream_(false) {
}

ApiRequestConfig ApiRequestConfig::copy() const {
    ApiRequestConfig copy;
    copy.setModel(model_);
    copy.setThinkingEnabled(thinkingEnabled_);
    copy.setTemperature(temperature_);
    copy.setMaxTokens(maxTokens_);
    copy.setTopP(topP_);
    copy.setN(n_);
    copy.setStream(stream_);
    return copy;
}

} // namespace api
} // namespace deepseek
