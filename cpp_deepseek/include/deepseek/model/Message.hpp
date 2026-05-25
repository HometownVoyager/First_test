/**
 * @file Message.hpp
 * @brief Represents a single message in a conversation.
 * 
 * 表示对话中的单条消息。
 */

#ifndef DEEPSEEK_MESSAGE_HPP
#define DEEPSEEK_MESSAGE_HPP

#include <string>
#include <cstdint>

namespace deepseek {
namespace model {

/**
 * @brief Message role types.
 * 
 * 消息角色类型。
 */
enum class Role {
    USER,       ///< User message / 用户消息
    ASSISTANT,  ///< Assistant/AI message / 助手消息
    SYSTEM,     ///< System prompt / 系统提示
    NARRATOR    ///< Narrator (background) / 旁白
};

/**
 * @brief Convert Role to string value for API.
 * 
 * 将角色转换为 API 使用的字符串值。
 */
inline std::string roleToString(Role role) {
    switch (role) {
        case Role::USER: return "user";
        case Role::ASSISTANT: return "assistant";
        case Role::SYSTEM: return "system";
        case Role::NARRATOR: return "narrator";
        default: return "assistant";
    }
}

/**
 * @brief Parse Role from string value.
 * 
 * 从字符串解析角色。
 */
inline Role stringToRole(const std::string& s) {
    if (s == "user") return Role::USER;
    if (s == "assistant") return Role::ASSISTANT;
    if (s == "system") return Role::SYSTEM;
    if (s == "narrator") return Role::NARRATOR;
    return Role::ASSISTANT;
}

/**
 * @brief Represents a single message in a conversation.
 * 
 * 表示对话中的单条消息，包含角色、内容、时间戳等信息。
 */
class Message {
public:
    /**
     * @brief Default constructor.
     * 
     * 默认构造函数，生成随机 ID 和当前时间戳。
     */
    Message();
    
    /**
     * @brief Construct a message with role and content.
     * 
     * 构造指定角色和内容的消息。
     * 
     * @param role Message role / 消息角色
     * @param content Message content / 消息内容
     */
    Message(Role role, const std::string& content);
    
    /**
     * @brief Construct a message with role, content, and role info.
     * 
     * 构造包含角色信息的消息。
     * 
     * @param role Message role / 消息角色
     * @param content Message content / 消息内容
     * @param roleId ID of the role that sent this message / 发送此消息的角色 ID
     * @param roleName Display name of the role / 角色显示名称
     */
    Message(Role role, const std::string& content, const std::string& roleId, const std::string& roleName);

    // Getters and Setters / 获取器和设置器
    [[nodiscard]] const std::string& getId() const { return id_; }
    void setId(const std::string& id) { id_ = id; }
    
    [[nodiscard]] Role getRole() const { return role_; }
    void setRole(Role role) { role_ = role; }
    
    [[nodiscard]] const std::string& getContent() const { return content_; }
    void setContent(const std::string& content);
    
    [[nodiscard]] const std::string& getRoleId() const { return roleId_; }
    void setRoleId(const std::string& roleId) { roleId_ = roleId; }
    
    [[nodiscard]] const std::string& getRoleName() const { return roleName_; }
    void setRoleName(const std::string& roleName) { roleName_ = roleName; }
    
    [[nodiscard]] int64_t getTimestamp() const { return timestamp_; }
    void setTimestamp(int64_t timestamp) { timestamp_ = timestamp; }
    
    [[nodiscard]] bool isEdited() const { return edited_; }
    void setEdited(bool edited) { edited_ = edited; }
    
    [[nodiscard]] const std::string& getThoughtContent() const { return thoughtContent_; }
    void setThoughtContent(const std::string& thoughtContent) { thoughtContent_ = thoughtContent; }
    
    /**
     * @brief Mark this message as edited due to manual user modification.
     * 
     * 标记此消息因用户手动修改而被编辑。
     */
    void markAsEdited() { edited_ = true; }
    
    /**
     * @brief Get a short string representation for debugging.
     * 
     * 获取用于调试的简短字符串表示。
     */
    [[nodiscard]] std::string toString() const;

private:
    std::string id_;           ///< Unique message ID / 唯一消息 ID
    Role role_;                ///< Message role / 消息角色
    std::string content_;      ///< Message content / 消息内容
    std::string roleId_;       ///< ID of the sending role / 发送角色 ID
    std::string roleName_;     ///< Display name of the role / 角色显示名称
    int64_t timestamp_;        ///< Creation timestamp / 创建时间戳
    bool edited_;              ///< Whether manually edited / 是否被手动编辑
    std::string thoughtContent_; ///< Content from thinking mode / 思考模式内容
};

} // namespace model
} // namespace deepseek

#endif // DEEPSEEK_MESSAGE_HPP
