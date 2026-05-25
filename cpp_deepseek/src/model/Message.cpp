/**
 * @file Message.cpp
 * @brief Implementation of the Message class.
 * 
 * Message 类的实现。
 */

#include "deepseek/model/Message.hpp"
#include <random>
#include <chrono>
#include <sstream>
#include <iomanip>

namespace deepseek {
namespace model {

namespace {

/**
 * @brief Generate a random UUID v4 string.
 * 
 * 生成随机 UUID v4 字符串。
 */
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

Message::Message() 
    : id_(generateUuid())
    , role_(Role::ASSISTANT)
    , timestamp_(std::chrono::duration_cast<std::chrono::milliseconds>(
          std::chrono::system_clock::now().time_since_epoch()).count())
    , edited_(false) {
}

Message::Message(Role role, const std::string& content)
    : Message() {
    role_ = role;
    content_ = content;
}

Message::Message(Role role, const std::string& content, const std::string& roleId, const std::string& roleName)
    : Message(role, content) {
    roleId_ = roleId;
    roleName_ = roleName;
}

void Message::setContent(const std::string& content) {
    content_ = content;
    edited_ = true;
}

std::string Message::toString() const {
    std::string roleStr;
    switch (role_) {
        case Role::USER: roleStr = "USER"; break;
        case Role::ASSISTANT: roleStr = "ASSISTANT"; break;
        case Role::SYSTEM: roleStr = "SYSTEM"; break;
        case Role::NARRATOR: roleStr = "NARRATOR"; break;
        default: roleStr = "UNKNOWN"; break;
    }
    
    std::string displayContent = content_;
    if (displayContent.length() > 50) {
        displayContent = displayContent.substr(0, 50) + "...";
    }
    
    return roleStr + ": " + displayContent;
}

} // namespace model
} // namespace deepseek
