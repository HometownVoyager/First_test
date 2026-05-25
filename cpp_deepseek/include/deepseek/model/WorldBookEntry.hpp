/**
 * @file WorldBookEntry.hpp
 * @brief Represents a World Info / Lorebook entry.
 * 
 * 表示世界信息/ lorebook 条目。
 */

#ifndef DEEPSEEK_WORLD_BOOK_ENTRY_HPP
#define DEEPSEEK_WORLD_BOOK_ENTRY_HPP

#include <string>
#include <vector>
#include <regex>

namespace deepseek {
namespace model {

/**
 * @brief Insert position for world book entries.
 * 
 * 世界书条目的插入位置。
 */
enum class InsertPosition {
    BEFORE,  ///< Insert before user message / 在用户消息之前插入
    AFTER    ///< Insert after user message / 在用户消息之后插入
};

/**
 * @brief Represents a single world book entry with trigger keys.
 * 
 * 表示带有触发键的单个世界书条目。
 */
class WorldBookEntry {
public:
    /**
     * @brief Default constructor.
     * 
     * 默认构造函数。
     */
    WorldBookEntry();
    
    /**
     * @brief Construct with key and content.
     * 
     * 使用键和内容构造。
     */
    WorldBookEntry(const std::string& key, const std::string& content);

    // Getters and Setters
    [[nodiscard]] const std::string& getId() const { return id_; }
    void setId(const std::string& id) { id_ = id; }
    
    [[nodiscard]] const std::string& getKey() const { return key_; }
    void setKey(const std::string& key);
    
    [[nodiscard]] const std::string& getContent() const { return content_; }
    void setContent(const std::string& content) { content_ = content; }
    
    [[nodiscard]] const std::string& getComment() const { return comment_; }
    void setComment(const std::string& comment) { comment_ = comment; }
    
    [[nodiscard]] int getPriority() const { return priority_; }
    void setPriority(int priority) { priority_ = priority; }
    
    [[nodiscard]] bool isEnabled() const { return enabled_; }
    void setEnabled(bool enabled) { enabled_ = enabled; }
    
    [[nodiscard]] InsertPosition getPosition() const { return position_; }
    void setPosition(InsertPosition position) { position_ = position; }
    
    [[nodiscard]] double getSelectivity() const { return selectivity_; }
    void setSelectivity(double selectivity) { selectivity_ = selectivity; }
    
    [[nodiscard]] bool isUseRegex() const { return useRegex_; }
    void setUseRegex(bool useRegex) { useRegex_ = useRegex; }
    
    [[nodiscard]] const std::vector<std::string>& getKeys() const { return keys_; }
    void setKeys(const std::vector<std::string>& keys) { keys_ = keys; }

    /**
     * @brief Check if the given text matches any trigger keys.
     * 
     * 检查给定文本是否匹配任何触发键。
     */
    [[nodiscard]] bool matches(const std::string& text) const;
    
    [[nodiscard]] std::string toString() const;

private:
    std::string id_;
    std::string key_;
    std::string content_;
    std::string comment_;
    int priority_;
    bool enabled_;
    InsertPosition position_;
    double selectivity_;
    bool useRegex_;
    std::vector<std::string> keys_;
};

} // namespace model
} // namespace deepseek

#endif // DEEPSEEK_WORLD_BOOK_ENTRY_HPP
