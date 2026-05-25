/**
 * @file Conversation.cpp
 * @brief Implementation of the Conversation class.
 * 
 * Conversation 类的实现。
 */

#include "deepseek/model/Conversation.hpp"
#include "deepseek/model/RoleCard.hpp"
#include <chrono>
#include <algorithm>

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

Conversation::Conversation()
    : id_(generateUuid())
    , createdAt_(std::chrono::duration_cast<std::chrono::milliseconds>(
          std::chrono::system_clock::now().time_since_epoch()).count())
    , updatedAt_(createdAt_)
    , speakMode_(SpeakMode::AUTO) {
    // Always include narrator by default
    participantRoleIds_.insert(RoleCard::NARRATOR_ID);
}

Conversation::Conversation(const std::string& title)
    : Conversation() {
    title_ = title;
}

void Conversation::setParticipantRoleIds(std::set<std::string> participantRoleIds) {
    participantRoleIds_ = std::move(participantRoleIds);
    // Ensure narrator is always included
    participantRoleIds_.insert(RoleCard::NARRATOR_ID);
}

void Conversation::addMessage(const Message& message) {
    messages_.push_back(message);
    updatedAt_ = std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::system_clock::now().time_since_epoch()).count();
    
    // Update message count for round-robin
    if (!message.getRoleId().empty()) {
        messageCounts_[message.getRoleId()]++;
    }
}

bool Conversation::removeMessage(const std::string& messageId) {
    auto it = std::remove_if(messages_.begin(), messages_.end(),
        [&messageId](const Message& m) { return m.getId() == messageId; });
    
    bool removed = (it != messages_.end());
    if (removed) {
        messages_.erase(it, messages_.end());
        updatedAt_ = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
    }
    return removed;
}

const Message* Conversation::getLastMessage() const {
    if (messages_.empty()) {
        return nullptr;
    }
    return &messages_.back();
}

void Conversation::addParticipant(const std::string& roleId) {
    if (!roleId.empty()) {
        participantRoleIds_.insert(roleId);
        updatedAt_ = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
    }
}

void Conversation::removeParticipant(const std::string& roleId) {
    if (!roleId.empty() && roleId != RoleCard::NARRATOR_ID) {
        participantRoleIds_.erase(roleId);
        updatedAt_ = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
    }
}

bool Conversation::isMuted(const std::string& roleId) const {
    return mutedRoleIds_.count(roleId) > 0;
}

void Conversation::mute(const std::string& roleId) {
    if (!roleId.empty() && roleId != RoleCard::NARRATOR_ID) {
        mutedRoleIds_.insert(roleId);
        updatedAt_ = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
    }
}

void Conversation::unmute(const std::string& roleId) {
    if (!roleId.empty()) {
        mutedRoleIds_.erase(roleId);
        updatedAt_ = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
    }
}

void Conversation::generateAutoTitle() {
    if (title_.empty() || title_.find_first_not_of(" \t\n\r") == std::string::npos) {
        if (!messages_.empty()) {
            const std::string& content = messages_[0].getContent();
            if (content.length() > 30) {
                title_ = content.substr(0, 30) + "...";
            } else {
                title_ = content;
            }
        } else {
            title_ = "New Conversation";
        }
    }
}

std::string Conversation::toString() const {
    return title_.empty() ? ("Conversation " + id_.substr(0, 8)) : title_;
}

} // namespace model
} // namespace deepseek
