/**
 * @file main_console.cpp
 * @brief Console application entry point for DeepSeek Chat.
 * 
 * 控制台应用程序入口。
 */

#include <iostream>
#include <string>
#include <sstream>
#include "deepseek/model/Message.hpp"
#include "deepseek/model/RoleCard.hpp"
#include "deepseek/model/Conversation.hpp"
#include "deepseek/model/WorldBook.hpp"
#include "deepseek/model/ApiKeyConfig.hpp"
#include "deepseek/api/ApiResponse.hpp"

using namespace deepseek;

void printWelcome() {
    std::cout << "========================================\n";
    std::cout << "   DeepSeek AI Chat - Console Edition\n";
    std::cout << "========================================\n\n";
}

void printCommands() {
    std::cout << "\nAvailable commands:\n";
    std::cout << "  exit     - Quit the application\n";
    std::cout << "  clear    - Clear conversation history\n";
    std::cout << "  help     - Show this help message\n\n";
}

int main(int argc, char* argv[]) {
    printWelcome();
    
    // Create a test conversation
    model::Conversation conv("Test Conversation");
    
    // Add narrator participant (already included by default)
    std::cout << "Created conversation: " << conv.getTitle() << "\n";
    std::cout << "Conversation ID: " << conv.getId() << "\n\n";
    
    // Demonstrate model classes
    std::cout << "=== Testing Model Classes ===\n\n";
    
    // Test Message
    model::Message userMsg(model::Role::USER, "Hello, this is a test message!");
    std::cout << "Created message: " << userMsg.toString() << "\n";
    std::cout << "Message ID: " << userMsg.getId() << "\n";
    std::cout << "Message Role: " << model::roleToString(userMsg.getRole()) << "\n\n";
    
    // Test RoleCard
    model::RoleCard role = model::RoleCard::createNarrator();
    std::cout << "Created role card: " << role.toString() << "\n";
    std::cout << "Role ID: " << role.getId() << "\n";
    std::cout << "Role Name: " << role.getName() << "\n\n";
    
    // Test WorldBook
    model::WorldBook book("Test World Book");
    model::WorldBookEntry entry("test", "This is test content for the world book.");
    entry.setComment("Test Entry");
    book.addEntry(entry);
    std::cout << "Created world book: " << book.toString() << "\n";
    std::cout << "Number of entries: " << book.getEntries().size() << "\n\n";
    
    // Test ApiKeyConfig
    model::ApiKeyConfig apiKey("Test Key", "sk-test1234567890abcdef", "");
    std::cout << "Created API key config: " << apiKey.toString() << "\n";
    std::cout << "Masked key: " << apiKey.getMaskedApiKey() << "\n";
    std::cout << "Endpoint: " << apiKey.getEndpoint() << "\n\n";
    
    // Test ApiResponse
    api::ApiResponse response = api::ApiResponse::success(
        "test-response-id",
        "This is a test response content.",
        "",
        10,
        20
    );
    std::cout << "Created API response:\n";
    std::cout << "  Success: " << (response.isSuccess() ? "yes" : "no") << "\n";
    std::cout << "  Content: " << response.getContent() << "\n";
    std::cout << "  Total tokens: " << response.getTotalTokens() << "\n\n";
    
    // Interactive loop
    printCommands();
    
    std::string input;
    while (true) {
        std::cout << "> ";
        if (!std::getline(std::cin, input)) {
            break;
        }
        
        if (input == "exit" || input == "quit") {
            std::cout << "Goodbye!\n";
            break;
        } else if (input == "help") {
            printCommands();
        } else if (input == "clear") {
            std::cout << "Conversation cleared.\n";
        } else if (input.empty()) {
            continue;
        } else {
            // Echo input as demonstration
            std::cout << "You said: " << input << "\n";
            
            // Add to conversation
            model::Message msg(model::Role::USER, input);
            conv.addMessage(msg);
            std::cout << "(Message added to conversation)\n";
        }
    }
    
    return 0;
}
