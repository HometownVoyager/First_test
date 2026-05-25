package com.deepseek.model;

import java.io.Serializable;

/**
 * Represents an API Key configuration with metadata.
 */
public class ApiKeyConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String apiKey;
    private String endpoint;
    private long createdAt;
    private long updatedAt;
    
    public ApiKeyConfig() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.endpoint = "https://api.deepseek.com";
    }
    
    public ApiKeyConfig(String name, String apiKey, String endpoint) {
        this();
        this.name = name;
        this.apiKey = apiKey;
        this.endpoint = endpoint != null ? endpoint : "https://api.deepseek.com";
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    /**
     * Returns the API key in masked form for display purposes.
     */
    public String getMaskedApiKey() {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
    
    @Override
    public String toString() {
        return name != null ? name : getMaskedApiKey();
    }
}
