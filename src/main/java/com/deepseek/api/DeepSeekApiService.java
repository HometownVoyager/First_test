package com.deepseek.api;

import com.deepseek.model.ApiKeyConfig;
import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for interacting with the DeepSeek API.
 */
public class DeepSeekApiService {
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    public DeepSeekApiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().create();
    }
    
    /**
     * Send a chat completion request to the DeepSeek API.
     * 
     * @param config API configuration (endpoint, API key)
     * @param requestConfig Request parameters (model, thinking mode, etc.)
     * @param messages List of messages in the conversation
     * @return ApiResponse containing the response content and metadata
     * @throws IOException if the request fails
     */
    public ApiResponse sendChatRequest(ApiKeyConfig config, ApiRequestConfig requestConfig, 
                                       List<com.deepseek.model.Message> messages) throws IOException {
        String endpoint = config.getEndpoint();
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }
        String url = endpoint + "chat/completions";
        
        // Build request body
        JsonObject body = new JsonObject();
        body.addProperty("model", requestConfig.getModel().getModelId());
        body.addProperty("stream", requestConfig.isStream());
        body.addProperty("temperature", requestConfig.getTemperature());
        body.addProperty("max_tokens", requestConfig.getMaxTokens());
        body.addProperty("top_p", requestConfig.getTopP());
        body.addProperty("n", requestConfig.getN());
        
        // Add thinking/reasoning mode parameter if enabled
        if (requestConfig.isThinkingEnabled()) {
            // DeepSeek specific: enable reasoning/thinking mode
            JsonObject extraBody = new JsonObject();
            extraBody.addProperty("enable_thinking", true);
            body.add("extra_body", extraBody);
            
            // For reasoner model, also add reasoning_effort
            if (requestConfig.getModel() == DeepSeekModel.DEEPSEEK_REASONER) {
                extraBody.addProperty("reasoning_effort", "high");
            }
        }
        
        // Build messages array
        JsonArray messagesArray = new JsonArray();
        for (com.deepseek.model.Message msg : messages) {
            if (msg.getContent() == null) continue;
            
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.getRole().getValue());
            
            // Handle thought content for assistant messages
            if (msg.getThoughtContent() != null && !msg.getThoughtContent().isEmpty()) {
                // Some models support separate thought field
                msgObj.addProperty("content", msg.getThoughtContent() + "\n\n" + msg.getContent());
            } else {
                msgObj.addProperty("content", msg.getContent());
            }
            
            messagesArray.add(msgObj);
        }
        body.add("messages", messagesArray);
        
        // Build HTTP request
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), JSON_MEDIA_TYPE))
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "null body";
                throw new IOException("API request failed with status " + response.code() + ": " + errorBody);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            // Extract choices
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                return ApiResponse.error("No choices in API response.");
            }
            
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            if (message == null) {
                return ApiResponse.error("No message in API response.");
            }
            
            String assistantContent = message.has("content") ? message.get("content").getAsString() : "";
            String thoughtContent = null;
            
            // Try to extract thought/reasoning content if present
            if (message.has("reasoning_content")) {
                thoughtContent = message.get("reasoning_content").getAsString();
            } else if (message.has("thought")) {
                thoughtContent = message.get("thought").getAsString();
            }
            
            // Extract usage statistics
            int promptTokens = 0;
            int completionTokens = 0;
            JsonObject usage = jsonResponse.getAsJsonObject("usage");
            if (usage != null) {
                if (usage.has("prompt_tokens")) {
                    promptTokens = usage.get("prompt_tokens").getAsInt();
                }
                if (usage.has("completion_tokens")) {
                    completionTokens = usage.get("completion_tokens").getAsInt();
                }
            }
            
            String responseId = jsonResponse.has("id") ? jsonResponse.get("id").getAsString() : null;
            
            return ApiResponse.success(responseId, assistantContent, thoughtContent, 
                                       promptTokens, completionTokens);
        }
    }
    
    /**
     * Fetch the current balance from the DeepSeek API.
     * 
     * @param config API configuration
     * @return Balance amount, or -1 if fetch failed
     */
    public double getBalance(ApiKeyConfig config) throws IOException {
        String endpoint = config.getEndpoint();
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }
        String url = endpoint + "user/balance";
        
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + config.getApiKey())
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return -1;
            }
            
            String body = response.body().string();
            JsonObject balanceObj = JsonParser.parseString(body).getAsJsonObject();
            
            if (balanceObj.has("balance")) {
                return balanceObj.get("balance").getAsDouble();
            } else if (balanceObj.has("total_balance")) {
                return balanceObj.get("total_balance").getAsDouble();
            }
        }
        
        return -1;
    }
}
