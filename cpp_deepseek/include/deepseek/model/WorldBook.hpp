/**
 * @file WorldBook.hpp
 * @brief Represents a World Book (Lorebook) containing multiple entries.
 */

#ifndef DEEPSEEK_WORLD_BOOK_HPP
#define DEEPSEEK_WORLD_BOOK_HPP

#include <string>
#include <vector>
#include <cstdint>
#include "deepseek/model/WorldBookEntry.hpp"

namespace deepseek {
namespace model {

class WorldBook {
public:
    WorldBook();
    explicit WorldBook(const std::string& name);

    [[nodiscard]] const std::string& getId() const { return id_; }
    void setId(const std::string& id) { id_ = id; }
    
    [[nodiscard]] const std::string& getName() const { return name_; }
    void setName(const std::string& name) { name_ = name; }
    
    [[nodiscard]] const std::string& getDescription() const { return description_; }
    void setDescription(const std::string& description) { description_ = description; }
    
    [[nodiscard]] const std::vector<WorldBookEntry>& getEntries() const { return entries_; }
    std::vector<WorldBookEntry>& getEntries() { return entries_; }
    void setEntries(const std::vector<WorldBookEntry>& entries) { entries_ = entries; }
    
    [[nodiscard]] int64_t getCreatedAt() const { return createdAt_; }
    void setCreatedAt(int64_t createdAt) { createdAt_ = createdAt; }
    
    [[nodiscard]] int64_t getUpdatedAt() const { return updatedAt_; }
    void setUpdatedAt(int64_t updatedAt) { updatedAt_ = updatedAt; }

    void addEntry(const WorldBookEntry& entry);
    bool removeEntry(const std::string& entryId);
    [[nodiscard]] std::vector<WorldBookEntry> findMatchingEntries(const std::string& text) const;
    [[nodiscard]] std::string toString() const;

private:
    std::string id_;
    std::string name_;
    std::string description_;
    std::vector<WorldBookEntry> entries_;
    int64_t createdAt_;
    int64_t updatedAt_;
};

} // namespace model
} // namespace deepseek

#endif // DEEPSEEK_WORLD_BOOK_HPP
