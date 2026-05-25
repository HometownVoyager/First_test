package com.deepseek.api;

/**
 * Represents the response from a DeepSeek API call.
 */
public class ApiResponse {
    private String id;
    private String content;
    private String thoughtContent; // Content from thinking mode
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private long timestamp;
    private boolean success;
    private String errorMessage;
    
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
        this.success = true;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getThoughtContent() { return thoughtContent; }
    public void setThoughtContent(String thoughtContent) { this.thoughtContent = thoughtContent; }
    
    public int getPromptTokens() { return promptTokens; }
    public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
    
    public int getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
    
    public int getTotalTokens() { return totalTokens; }
    public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    /**
     * Create a successful response.
     */
    public static ApiResponse success(String id, String content, String thoughtContent, 
                                      int promptTokens, int completionTokens) {
        ApiResponse response = new ApiResponse();
        response.setId(id);
        response.setContent(content);
        response.setThoughtContent(thoughtContent);
        response.setPromptTokens(promptTokens);
        response.setCompletionTokens(completionTokens);
        response.setTotalTokens(promptTokens + completionTokens);
        return response;
    }
    
    /**
     * Create an error response.
     */
    public static ApiResponse error(String errorMessage) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
