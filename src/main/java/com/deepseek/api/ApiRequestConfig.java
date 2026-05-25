package com.deepseek.api;

/**
 * Configuration for a single API request.
 */
public class ApiRequestConfig {
    private DeepSeekModel model;
    private boolean thinkingEnabled;
    private double temperature;
    private int maxTokens;
    private double topP;
    private int n; // Number of completions to generate
    private boolean stream;
    
    public ApiRequestConfig() {
        this.model = DeepSeekModel.DEEPSEEK_CHAT;
        this.thinkingEnabled = false;
        this.temperature = 0.7;
        this.maxTokens = 2048;
        this.topP = 1.0;
        this.n = 1;
        this.stream = false;
    }
    
    // Getters and Setters
    public DeepSeekModel getModel() { return model; }
    public void setModel(DeepSeekModel model) { this.model = model; }
    
    public boolean isThinkingEnabled() { return thinkingEnabled; }
    public void setThinkingEnabled(boolean thinkingEnabled) { this.thinkingEnabled = thinkingEnabled; }
    
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    
    public double getTopP() { return topP; }
    public void setTopP(double topP) { this.topP = topP; }
    
    public int getN() { return n; }
    public void setN(int n) { this.n = n; }
    
    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }
    
    /**
     * Create a copy of this config.
     */
    public ApiRequestConfig copy() {
        ApiRequestConfig copy = new ApiRequestConfig();
        copy.setModel(this.model);
        copy.setThinkingEnabled(this.thinkingEnabled);
        copy.setTemperature(this.temperature);
        copy.setMaxTokens(this.maxTokens);
        copy.setTopP(this.topP);
        copy.setN(this.n);
        copy.setStream(this.stream);
        return copy;
    }
}
