package com.deepseek.api;

/**
 * Available DeepSeek models.
 */
public enum DeepSeekModel {
    DEEPSEEK_V4_FLASH("deepseek-v4-flash", "DeepSeek V4 Flash"),
    DEEPSEEK_V4_PRO("deepseek-v4-pro", "DeepSeek V4 Pro"),
    DEEPSEEK_CHAT("deepseek-chat", "DeepSeek Chat"),
    DEEPSEEK_REASONER("deepseek-reasoner", "DeepSeek Reasoner");
    
    private final String modelId;
    private final String displayName;
    
    DeepSeekModel(String modelId, String displayName) {
        this.modelId = modelId;
        this.displayName = displayName;
    }
    
    public String getModelId() {
        return modelId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static DeepSeekModel fromString(String s) {
        for (DeepSeekModel m : values()) {
            if (m.modelId.equals(s)) return m;
        }
        return DEEPSEEK_CHAT;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
