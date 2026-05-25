/**
 * @file WorldBook.cpp
 * @brief Implementation of the WorldBook class.
 */

#include "deepseek/model/WorldBook.hpp"
#include <chrono>
#include <algorithm>
#include <sstream>

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

WorldBook::WorldBook()
    : id_(generateUuid())
    , createdAt_(std::chrono::duration_cast<std::chrono::milliseconds>(
          std::chrono::system_clock::now().time_since_epoch()).count())
    , updatedAt_(createdAt_) {
}

WorldBook::WorldBook(const std::string& name)
    : WorldBook() {
    name_ = name;
}

void WorldBook::addEntry(const WorldBookEntry& entry) {
    entries_.push_back(entry);
    updatedAt_ = std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::system_clock::now().time_since_epoch()).count();
}

bool WorldBook::removeEntry(const std::string& entryId) {
    auto it = std::remove_if(entries_.begin(), entries_.end(),
        [&entryId](const WorldBookEntry& e) { return e.getId() == entryId; });
    
    bool removed = (it != entries_.end());
    if (removed) {
        entries_.erase(it, entries_.end());
        updatedAt_ = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
    }
    return removed;
}

std::vector<WorldBookEntry> WorldBook::findMatchingEntries(const std::string& text) const {
    std::vector<WorldBookEntry> matching;
    
    for (const auto& entry : entries_) {
        if (entry.matches(text)) {
            matching.push_back(entry);
        }
    }
    
    // Sort by priority (higher first)
    std::sort(matching.begin(), matching.end(),
        [](const WorldBookEntry& a, const WorldBookEntry& b) {
            return a.getPriority() > b.getPriority();
        });
    
    return matching;
}

std::string WorldBook::toString() const {
    return name_.empty() ? "Unnamed World Book" : name_;
}

} // namespace model
} // namespace deepseek
