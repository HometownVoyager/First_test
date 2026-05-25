/**
 * @file RoleCard.hpp
 * @brief Represents a role card compatible with SillyTavern format.
 * 
 * 表示与 SillyTavern 格式兼容的角色卡片。
 */

#ifndef DEEPSEEK_ROLE_CARD_HPP
#define DEEPSEEK_ROLE_CARD_HPP

#include <string>
#include <vector>
#include <cstdint>

namespace deepseek {
namespace model {

/**
 * @brief Represents a role card for character-based conversations.
 * 
 * 表示用于基于角色的对话的角色卡片。
 */
class RoleCard {
public:
    /// Special Narrator role ID / 特殊旁白角色 ID
    static constexpr const char* NARRATOR_ID = "__narrator__";
    /// Special Narrator role name / 特殊旁白角色名称
    static constexpr const char* NARRATOR_NAME = "背景";

    /**
     * @brief Default constructor.
     * 
     * 默认构造函数。
     */
    RoleCard();

    /**
     * @brief Creates the special Narrator (背景) role card.
     * 
     * 创建特殊的旁白（背景）角色卡片。
     * 
     * @return The narrator role card / 旁白角色卡片
     */
    static RoleCard createNarrator();

    // Getters and Setters / 获取器和设置器
    [[nodiscard]] const std::string& getId() const { return id_; }
    void setId(const std::string& id);
    
    [[nodiscard]] const std::string& getName() const { return name_; }
    void setName(const std::string& name) { name_ = name; }
    
    [[nodiscard]] const std::string& getDescription() const { return description_; }
    void setDescription(const std::string& description) { description_ = description; }
    
    [[nodiscard]] const std::string& getPersonality() const { return personality_; }
    void setPersonality(const std::string& personality) { personality_ = personality; }
    
    [[nodiscard]] const std::string& getScenario() const { return scenario_; }
    void setScenario(const std::string& scenario) { scenario_ = scenario; }
    
    [[nodiscard]] const std::string& getFirstMes() const { return firstMes_; }
    void setFirstMes(const std::string& firstMes) { firstMes_ = firstMes; }
    
    [[nodiscard]] const std::string& getMesExample() const { return mesExample_; }
    void setMesExample(const std::string& mesExample) { mesExample_ = mesExample; }
    
    [[nodiscard]] const std::string& getCreatorNotes() const { return creatorNotes_; }
    void setCreatorNotes(const std::string& creatorNotes) { creatorNotes_ = creatorNotes; }
    
    [[nodiscard]] const std::string& getSystemPrompt() const { return systemPrompt_; }
    void setSystemPrompt(const std::string& systemPrompt) { systemPrompt_ = systemPrompt; }
    
    [[nodiscard]] const std::string& getAvatar() const { return avatar_; }
    void setAvatar(const std::string& avatar) { avatar_ = avatar; }
    
    [[nodiscard]] const std::string& getAvatarDataType() const { return avatarDataType_; }
    void setAvatarDataType(const std::string& avatarDataType) { avatarDataType_ = avatarDataType; }
    
    [[nodiscard]] const std::string& getApiKeyId() const { return apiKeyId_; }
    void setApiKeyId(const std::string& apiKeyId) { apiKeyId_ = apiKeyId; }
    
    [[nodiscard]] const std::vector<std::string>& getTags() const { return tags_; }
    void setTags(const std::vector<std::string>& tags) { tags_ = tags; }
    
    [[nodiscard]] const std::string& getCharacterVersion() const { return characterVersion_; }
    void setCharacterVersion(const std::string& characterVersion) { characterVersion_ = characterVersion; }
    
    [[nodiscard]] int64_t getCreatedAt() const { return createdAt_; }
    void setCreatedAt(int64_t createdAt) { createdAt_ = createdAt; }
    
    [[nodiscard]] int64_t getUpdatedAt() const { return updatedAt_; }
    void setUpdatedAt(int64_t updatedAt) { updatedAt_ = updatedAt; }
    
    [[nodiscard]] bool isSystem() const { return isSystem_; }
    void setSystem(bool isSystem) { isSystem_ = isSystem; }
    
    [[nodiscard]] const std::string& getExtensions() const { return extensions_; }
    void setExtensions(const std::string& extensions) { extensions_ = extensions; }
    
    [[nodiscard]] const std::string& getData() const { return data_; }
    void setData(const std::string& data) { data_ = data; }

    /**
     * @brief Get string representation for debugging.
     * 
     * 获取用于调试的字符串表示。
     */
    [[nodiscard]] std::string toString() const;

private:
    std::string id_;                  ///< Unique role ID / 唯一角色 ID
    std::string name_;                ///< Character name / 角色名称
    std::string description_;         ///< Character description / 角色描述
    std::string personality_;         ///< Personality traits / 性格特征
    std::string scenario_;            ///< Scenario/setting / 场景设定
    std::string firstMes_;            ///< First message / 第一条消息
    std::string mesExample_;          ///< Example messages / 示例消息
    std::string creatorNotes_;        ///< Creator's notes / 创作者备注
    std::string systemPrompt_;        ///< System prompt for this role / 此角色的系统提示
    std::string avatar_;              ///< Avatar image (path or base64) / 头像图片
    std::string avatarDataType_;      ///< Avatar data type / 头像数据类型
    std::string apiKeyId_;            ///< Bound API Key ID / 绑定的 API 密钥 ID
    std::vector<std::string> tags_;   ///< Tags for categorization / 分类标签
    std::string characterVersion_;    ///< Character version / 角色版本
    int64_t createdAt_;               ///< Creation timestamp / 创建时间戳
    int64_t updatedAt_;               ///< Last update timestamp / 最后更新时间戳
    bool isSystem_;                   ///< True for built-in roles / 是否为内置角色
    std::string extensions_;          ///< JSON string for extensions data / 扩展数据 JSON
    std::string data_;                ///< JSON string for full SillyTavern data / 完整 SillyTavern 数据 JSON
};

} // namespace model
} // namespace deepseek

#endif // DEEPSEEK_ROLE_CARD_HPP
