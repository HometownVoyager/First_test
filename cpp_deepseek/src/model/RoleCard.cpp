/**
 * @file RoleCard.cpp
 * @brief Implementation of the RoleCard class.
 * 
 * RoleCard 类的实现。
 */

#include "deepseek/model/RoleCard.hpp"
#include "deepseek/model/Message.hpp"
#include <chrono>

namespace deepseek {
namespace model {

// Helper to generate UUID (same as in Message.cpp)
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

RoleCard::RoleCard()
    : id_(generateUuid())
    , createdAt_(std::chrono::duration_cast<std::chrono::milliseconds>(
          std::chrono::system_clock::now().time_since_epoch()).count())
    , updatedAt_(createdAt_)
    , isSystem_(false) {
}

RoleCard RoleCard::createNarrator() {
    RoleCard narrator;
    narrator.setId(NARRATOR_ID);
    narrator.setName(NARRATOR_NAME);
    narrator.setDescription("旁白与宏观调控者，可以看到全部对话上下文，负责场景描述和事件推进。");
    narrator.setSystemPrompt("You are the narrator and macro controller. You can see all conversation context and all characters' speeches. You provide scene descriptions, narrative transitions, and event progression. You can also mute or unmute specific characters.");
    narrator.setFirstMes("*The scene awaits your action...*");
    narrator.setSystem(true);
    return narrator;
}

void RoleCard::setId(const std::string& id) {
    id_ = id;
    if (NARRATOR_ID == id) {
        isSystem_ = true;
    }
}

std::string RoleCard::toString() const {
    return name_.empty() ? "Unnamed Role" : name_;
}

} // namespace model
} // namespace deepseek
