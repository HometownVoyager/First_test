/**
 * @file DeepSeekModel.hpp
 * @brief Available DeepSeek models.
 */

#ifndef DEEPSEEK_MODEL_ENUM_HPP
#define DEEPSEEK_MODEL_ENUM_HPP

#include <string>

namespace deepseek {
namespace api {

enum class DeepSeekModel {
    DEEPSEEK_V4_FLASH,
    DEEPSEEK_V4_PRO,
    DEEPSEEK_CHAT,
    DEEPSEEK_REASONER
};

[[nodiscard]] inline std::string modelToString(DeepSeekModel model) {
    switch (model) {
        case DeepSeekModel::DEEPSEEK_V4_FLASH: return "deepseek-v4-flash";
        case DeepSeekModel::DEEPSEEK_V4_PRO: return "deepseek-v4-pro";
        case DeepSeekModel::DEEPSEEK_CHAT: return "deepseek-chat";
        case DeepSeekModel::DEEPSEEK_REASONER: return "deepseek-reasoner";
        default: return "deepseek-chat";
    }
}

[[nodiscard]] inline std::string modelDisplayName(DeepSeekModel model) {
    switch (model) {
        case DeepSeekModel::DEEPSEEK_V4_FLASH: return "DeepSeek V4 Flash";
        case DeepSeekModel::DEEPSEEK_V4_PRO: return "DeepSeek V4 Pro";
        case DeepSeekModel::DEEPSEEK_CHAT: return "DeepSeek Chat";
        case DeepSeekModel::DEEPSEEK_REASONER: return "DeepSeek Reasoner";
        default: return "DeepSeek Chat";
    }
}

[[nodiscard]] inline DeepSeekModel stringToModel(const std::string& s) {
    if (s == "deepseek-v4-flash") return DeepSeekModel::DEEPSEEK_V4_FLASH;
    if (s == "deepseek-v4-pro") return DeepSeekModel::DEEPSEEK_V4_PRO;
    if (s == "deepseek-chat") return DeepSeekModel::DEEPSEEK_CHAT;
    if (s == "deepseek-reasoner") return DeepSeekModel::DEEPSEEK_REASONER;
    return DeepSeekModel::DEEPSEEK_CHAT;
}

} // namespace api
} // namespace deepseek

#endif // DEEPSEEK_MODEL_ENUM_HPP
