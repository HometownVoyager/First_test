/**
 * @file Conversation.hpp
 * @brief Represents a conversation (chat room) with multiple participants.
 * 
 * 表示包含多个参与者的对话（聊天室）。
 */

#ifndef DEEPSEEK_CONVERSATION_HPP
#define DEEPSEEK_CONVERSATION_HPP

#include <string>
#include <vector>
#include <set>
#include <map>
#include <cstdint>
#include "deepseek/model/Message.hpp"

namespace deepseek {
namespace model {

/**
 * @brief Conversation speaking modes.
 * 
 * 对话发言模式。
 */
enum class SpeakMode {
    MANUAL,       ///< User manually specifies next speaker / 用户手动指定下一个发言者
    ROUND_ROBIN,  ///< Roles take turns in order / 角色按顺序轮流发言
    AUTO          ///< Model decides who speaks next / 模型决定下一个发言者
};

/**
 * @brief Represents a conversation with multiple participants.
 * 
 * 表示包含多个参与者的对话，支持多种发言模式和角色管理。
 */
class Conversation {
public:
    /**
     * @brief Default constructor.
     * 
     * 默认构造函数。
     */
    Conversation();
    
    /**
     * @brief Construct a conversation with a title.
     * 
     * 构造带标题的对话。
     * 
     * @param title Conversation title / 对话标题
     */
    explicit Conversation(const std::string& title);

    // Getters and Setters / 获取器和设置器
    [[nodiscard]] const std::string& getId() const { return id_; }
    void setId(const std::string& id) { id_ = id; }
    
    [[nodiscard]] const std::string& getTitle() const { return title_; }
    void setTitle(const std::string& title) { title_ = title; }
    
    [[nodiscard]] const std::vector<Message>& getMessages() const { return messages_; }
    std::vector<Message>& getMessages() { return messages_; }
    void setMessages(const std::vector<Message>& messages) { messages_ = messages; }
    
    [[nodiscard]] const std::set<std::string>& getParticipantRoleIds() const { return participantRoleIds_; }
    std::set<std::string>& getParticipantRoleIds() { return participantRoleIds_; }
    void setParticipantRoleIds(std::set<std::string> participantRoleIds);
    
    [[nodiscard]] const std::set<std::string>& getMutedRoleIds() const { return mutedRoleIds_; }
    std::set<std::string>& getMutedRoleIds() { return mutedRoleIds_; }
    void setMutedRoleIds(const std::set<std::string>& mutedRoleIds) { mutedRoleIds_ = mutedRoleIds; }
    
    [[nodiscard]] const std::string& getWorldBookId() const { return worldBookId_; }
    void setWorldBookId(const std::string& worldBookId) { worldBookId_ = worldBookId; }
    
    [[nodiscard]] SpeakMode getSpeakMode() const { return speakMode_; }
    void setSpeakMode(SpeakMode speakMode) { speakMode_ = speakMode; }
    
    [[nodiscard]] const std::string& getNextSpeakerId() const { return nextSpeakerId_; }
    void setNextSpeakerId(const std::string& nextSpeakerId) { nextSpeakerId_ = nextSpeakerId; }
    
    [[nodiscard]] int64_t getCreatedAt() const { return createdAt_; }
    void setCreatedAt(int64_t createdAt) { createdAt_ = createdAt; }
    
    [[nodiscard]] int64_t getUpdatedAt() const { return updatedAt_; }
    void setUpdatedAt(int64_t updatedAt) { updatedAt_ = updatedAt; }
    
    [[nodiscard]] const std::map<std::string, int>& getMessageCounts() const { return messageCounts_; }
    std::map<std::string, int>& getMessageCounts() { return messageCounts_; }
    void setMessageCounts(const std::map<std::string, int>& messageCounts) { messageCounts_ = messageCounts; }

    /**
     * @brief Add a message to this conversation.
     * 
     * 添加消息到此对话。
     * 
     * @param message The message to add / 要添加的消息
     */
    void addMessage(const Message& message);
    
    /**
     * @brief Remove a message by ID (for regeneration).
     * 
     * 按 ID 移除消息（用于重新生成）。
     * 
     * @param messageId The message ID to remove / 要移除的消息 ID
     * @return true if removed / 如果已移除返回 true
     */
    bool removeMessage(const std::string& messageId);
    
    /**
     * @brief Get the last message in the conversation.
     * 
     * 获取对话中的最后一条消息。
     * 
     * @return Pointer to last message, or nullptr if empty / 指向最后一条消息的指针，如果为空则返回 nullptr
     */
    [[nodiscard]] const Message* getLastMessage() const;
    
    /**
     * @brief Add a participant role to this conversation.
     * 
     * 添加参与者角色到此对话。
     * 
     * @param roleId The role ID to add / 要添加的角色 ID
     */
    void addParticipant(const std::string& roleId);
    
    /**
     * @brief Remove a participant role from this conversation.
     * 
     * 从此对话移除参与者角色。
     * 
     * @param roleId The role ID to remove / 要移除的角色 ID
     */
    void removeParticipant(const std::string& roleId);
    
    /**
     * @brief Check if a role is muted.
     * 
     * 检查角色是否被禁言。
     * 
     * @param roleId The role ID to check / 要检查的角色 ID
     * @return true if muted / 如果被禁言返回 true
     */
    [[nodiscard]] bool isMuted(const std::string& roleId) const;
    
    /**
     * @brief Mute a role.
     * 
     * 禁言角色。
     * 
     * @param roleId The role ID to mute / 要禁言的角色 ID
     */
    void mute(const std::string& roleId);
    
    /**
     * @brief Unmute a role.
     * 
     * 取消禁言角色。
     * 
     * @param roleId The role ID to unmute / 要取消禁言的角色 ID
     */
    void unmute(const std::string& roleId);
    
    /**
     * @brief Generate an auto title from the first message.
     * 
     * 从第一条消息生成自动标题。
     */
    void generateAutoTitle();
    
    /**
     * @brief Get string representation for debugging.
     * 
     * 获取用于调试的字符串表示。
     */
    [[nodiscard]] std::string toString() const;

private:
    std::string id_;                          ///< Unique conversation ID / 唯一对话 ID
    std::string title_;                       ///< Conversation title / 对话标题
    std::vector<Message> messages_;           ///< Messages in this conversation / 对话中的消息列表
    std::set<std::string> participantRoleIds_; ///< IDs of roles participating / 参与角色的 ID 集合
    std::set<std::string> mutedRoleIds_;      ///< IDs of roles that are muted / 被禁言角色的 ID 集合
    std::string worldBookId_;                 ///< Bound world book ID / 绑定的世界书 ID
    SpeakMode speakMode_;                     ///< Current speak mode / 当前发言模式
    std::string nextSpeakerId_;               ///< For MANUAL mode / 手动模式下的下一个发言者
    int64_t createdAt_;                       ///< Creation timestamp / 创建时间戳
    int64_t updatedAt_;                       ///< Last update timestamp / 最后更新时间戳
    std::map<std::string, int> messageCounts_; ///< Track message count per role / 跟踪每个角色的消息计数
};

} // namespace model
} // namespace deepseek

#endif // DEEPSEEK_CONVERSATION_HPP
