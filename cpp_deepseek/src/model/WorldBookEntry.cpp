/**
 * @file WorldBookEntry.cpp
 * @brief Implementation of the WorldBookEntry class.
 */

#include "deepseek/model/WorldBookEntry.hpp"
#include <algorithm>
#include <cctype>
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

std::string toLower(const std::string& s) {
    std::string result = s;
    std::transform(result.begin(), result.end(), result.begin(),
                   [](unsigned char c) { return std::tolower(c); });
    return result;
}

std::vector<std::string> splitKeys(const std::string& keyStr) {
    std::vector<std::string> keys;
    size_t start = 0;
    size_t end = keyStr.find(',');
    
    while (end != std::string::npos) {
        std::string part = keyStr.substr(start, end - start);
        // Trim whitespace
        size_t first = part.find_first_not_of(" \t");
        size_t last = part.find_last_not_of(" \t");
        if (first != std::string::npos) {
            keys.push_back(part.substr(first, last - first + 1));
        }
        start = end + 1;
        end = keyStr.find(',', start);
    }
    
    // Last part
    std::string part = keyStr.substr(start);
    size_t first = part.find_first_not_of(" \t");
    size_t last = part.find_last_not_of(" \t");
    if (first != std::string::npos) {
        keys.push_back(part.substr(first, last - first + 1));
    }
    
    return keys;
}
} // anonymous namespace

WorldBookEntry::WorldBookEntry()
    : id_(generateUuid())
    , enabled_(true)
    , priority_(0)
    , position_(InsertPosition::BEFORE)
    , selectivity_(1.0)
    , useRegex_(false) {
}

WorldBookEntry::WorldBookEntry(const std::string& key, const std::string& content)
    : WorldBookEntry() {
    key_ = key;
    content_ = content;
    if (!key.empty()) {
        keys_ = splitKeys(key);
    }
}

void WorldBookEntry::setKey(const std::string& key) {
    key_ = key;
    if (!key.empty()) {
        keys_ = splitKeys(key);
    }
}

bool WorldBookEntry::matches(const std::string& text) const {
    if (!enabled_ || text.empty()) {
        return false;
    }
    
    std::string lowerText = toLower(text);
    
    for (const auto& k : keys_) {
        if (k.empty()) continue;
        
        if (useRegex_) {
            try {
                std::regex pattern(toLower(k), std::regex_constants::icase);
                if (std::regex_search(lowerText, pattern)) {
                    return true;
                }
            } catch (...) {
                // Invalid regex, skip
            }
        } else {
            if (lowerText.find(toLower(k)) != std::string::npos) {
                return true;
            }
        }
    }
    
    return false;
}

std::string WorldBookEntry::toString() const {
    return comment_.empty() ? (key_.empty() ? "Unnamed Entry" : key_) : comment_;
}

} // namespace model
} // namespace deepseek
