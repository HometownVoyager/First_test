package com.deepseek.chat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import okhttp3
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit
/**
 * DeepSeekChat - a console-based AI chatbot that connects to DeepSeek API.
 * Supports custom system prompt, displays token usage and balance after each exchange.
 */
public class DeepSeekChat {
public class
    private static final String DEEPSEEK_CHAT_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEEPSEEK_BALANCE_URL = "https://api.deepseek.com/user/balance";
    private static final String D
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final List<Message> conversationHistory;
    private String systemPrompt;
    private String
    private int totalPromptTokens = 0;
    private int totalCompletionTokens = 0;
   
    public DeepSeekChat(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.conversationHistory = new ArrayList<>();
        this.systemPrompt = "You are a helpful assistant.";
    }
    }
    public static void main(String[] args) {
        System.out.println("=======================================");
        System.out.println("   DeepSeek AI Chat - Console Edition");
        System.out.println("=======================================");
        System.out
        Scanner scanner = new Scanner(System.in);
        Scanner scanner = new Scanner
        // Prompt for API Key
        System.out.print("Enter your DeepSeek API Key: ");
        String apiKey = scanner.nextLine().trim();
        if (apiKey.isEmpty()) {
            System.err.println("Error: API Key cannot be empty. Exiting.");
            System.exit(1);
        }
       
        DeepSeekChat chat = new DeepSeekChat(apiKey);
        DeepSeekChat
        // Optional custom system prompt
        System.out.println("\nDefault system prompt: \"" + chat.systemPrompt + "\"");
        System.out.print("Enter a custom system prompt (or press Enter to keep default): ");
        String customPrompt = scanner.nextLine().trim();
        if (!customPrompt.isEmpty()) {
            chat.setSystemPrompt(customPrompt);
            System.out.println("System prompt updated.");
        }
        }
        // Show initial balance
        chat.printBalance();
        chat
        System.out.println("\nChat started. Type 'exit' to quit, 'balance' to check balance, 'clear' to reset conversation.\n");
        System.out.println
        // Main chat loop
        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine().trim();
            String userInput = scanner
            if (userInput.isEmpty()) continue;
            if (userInput.isEmpty
            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Exiting chat. Goodbye!");
                break;
            }
            }
            if (userInput.equalsIgnoreCase("balance")) {
                chat.printBalance();
                continue;
            }
            }
            if (userInput.equalsIgnoreCase("clear")) {
                chat.clearHistory();
                System.out.println("Conversation history cleared.");
                continue;
            }
           
            // Send message and get response
            try {
                String response = chat.sendMessage(userInput);
                System.out.println("Bot: " + response);
                // Print token usage for this turn and overall
                chat.printUsageStats();
            } catch (IOException e) {
                System.err.println("Error communicating with DeepSeek API: " + e.getMessage());
                if (e.getMessage().contains("401")) {
                    System.err.println("Authentication failed. Please check your API Key.");
                }
            }
        }
       
        scanner.close();
    }
    }
    public void setSystemPrompt(String prompt) {
        this.systemPrompt = prompt;
        // If there is no conversation history or the first message is a system message, replace it.
        // Otherwise, we may need to rebuild. For simplicity, just reset history.
        clearHistory();
    }
    }
    public void clearHistory() {
        conversationHistory.clear();
        // We do not reset token counters because they represent total consumption of the API key.
        // Only session-level tokens could be reset, but we want to show total billable usage.
    }
    }
    /**
     * Send a user message, receive the assistant's reply, and update conversation history.
     */
    public String sendMessage(String userMessage) throws IOException {
        // Add user message to history
        conversationHistory.add(new Message("user", userMessage));
        conversationHistory
        // Build request body
        JsonObject body = new JsonObject();
        body.addProperty("model", "deepseek-chat");
        body.addProperty("stream", false);
        body.addProperty("stream
        // Messages array
        JsonArray messages = new JsonArray();
        // System prompt
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemPrompt);
        messages.add(sysMsg);
        messages
        // Conversation history
        for (Message m : conversationHistory) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", m.role);
            msgObj.addProperty("content", m.content);
            messages.add(msgObj);
        }
        body.add("messages", messages);
        body.add("messages",
        // Build HTTP request
        Request request = new Request.Builder()
                .url(DEEPSEEK_CHAT_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();
                .build
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "null body";
                throw new IOException("API request failed with status " + response.code() + ": " + errorBody);
            }
           
            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            Json
            // Extract assistant message
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                throw new IOException("No choices in API response.");
            }
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            String assistantContent = message.get("content").getAsString();
            String assistantContent = message.get("content").getAsString();
            // Update conversation history
            conversationHistory.add(new Message("assistant", assistantContent));
            conversation
            // Update token counters
            JsonObject usage = jsonResponse.getAsJsonObject("usage");
            if (usage != null) {
                int promptTokens = usage.get("prompt_tokens").getAsInt();
                int completionTokens = usage.get("completion_tokens").getAsInt();
                totalPromptTokens += promptTokens;
                totalCompletionTokens += completionTokens;
            }
            }
            return assistantContent;
        }
    }
    }
    /**
     * Fetch and print the current balance (in CNY or USD based on API).
     */
    public void printBalance() {
        try {
            Request request = new Request.Builder()
                    .url(DEEPSEEK_BALANCE_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .get()
                    .build();
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Could not fetch balance: HTTP " + response.code());
                    return;
                }
                }
                String body = response.body().string();
                JsonObject balanceObj = JsonParser.parseString(body).getAsJsonObject();
                // The balance API may return 'balance' or 'total_balance' etc.
                // According to DeepSeek docs, the field is "balance" (in CNY).
                if (balanceObj.has("balance")) {
                    double balance = balanceObj.get("balance").getAsDouble();
                    System.out.printf("Current balance: ¥%.4f%n", balance);
                } else if (balanceObj.has("total_balance")) {
                    double balance = balanceObj.get("total_balance").getAsDouble();
                    System.out.printf("Current balance: ¥%.4f%n", balance);
                } else {
                    System.out.println("Balance information not found in response.");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not fetch balance: " + e.getMessage());
        }
    }
   
    /**
     * Print token usage statistics for the current session and estimated cost.
     * DeepSeek pricing (as of 2025):
     *   - Input: $0.14 / 1M tokens (or ¥1 / 1M tokens)
     *   - Output: $0.28 / 1M tokens (or ¥2 / 1M tokens)
     * We display both USD and CNY estimates.
     */
    public void printUsageStats() {
        System.out.println("--- Usage Stats ---");
        System.out.println("Prompt tokens:     " + totalPromptTokens);
        System.out.println("Completion tokens: " + totalCompletionTokens);
        int totalTokens = totalPromptTokens + totalCompletionTokens;
        System.out.println("Total tokens:      " + totalTokens);
       
        // Cost estimation in USD (approximate based on public pricing)
        double costUSD = (totalPromptTokens / 1_000_000.0) * 0.14 +
                         (totalCompletionTokens / 1_000_000.0) * 0.28;
        // Convert to CNY (assuming rate 1 USD = 7.2 CNY, but this is rough)
        double costCNY = costUSD * 7.2;
        System.out.printf("Estimated cost:    $%.6f (≈ ¥%.6f)%n", costUSD, costCNY);
        System.out.println("--------------------");
    }
    }
    /**
     * Simple inner record for messages.
     */
    private static class Message {
        String role;
        String content;
        String content
        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}