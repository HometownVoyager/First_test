package com.deepseek.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a role card compatible with SillyTavern format (V2/V3).
 */
public class RoleCard implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String NARRATOR_ID = "__narrator__";
    public static final String NARRATOR_NAME = "背景";
    
    private String id;
    private String name;
    private String description;
    private String personality;
    private String scenario;
    private String firstMes;
    private String mesExample;
    private String creatorNotes;
    private String systemPrompt;
    private String avatar;
    private String avatarDataType; // "image/png" base64 or file path
    private String apiKeyId; // Bound API Key ID
    private List<String> tags;
    private String characterVersion;
    private long createdAt;
    private long updatedAt;
    private boolean isSystem; // true for built-in roles like Narrator
    
    // SillyTavern V2/V3 specific fields
    private String extensions; // JSON string for extensions data
    private String data; // JSON string for full SillyTavern data
    
    public RoleCard() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.tags = new ArrayList<>();
        this.isSystem = false;
    }
    
    /**
     * Creates the special Narrator (背景) role card.
     */
    public static RoleCard createNarrator() {
        RoleCard narrator = new RoleCard();
        narrator.setId(NARRATOR_ID);
        narrator.setName(NARRATOR_NAME);
        narrator.setDescription("旁白与宏观调控者，可以看到全部对话上下文，负责场景描述和事件推进。");
        narrator.setSystemPrompt("You are the narrator and macro controller. You can see all conversation context and all characters' speeches. You provide scene descriptions, narrative transitions, and event progression. You can also mute or unmute specific characters.");
        narrator.setFirstMes("*The scene awaits your action...*");
        narrator.setSystem(true);
        return narrator;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { 
        this.id = id; 
        if (NARRATOR_ID.equals(id)) {
            this.isSystem = true;
        }
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPersonality() { return personality; }
    public void setPersonality(String personality) { this.personality = personality; }
    
    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }
    
    public String getFirstMes() { return firstMes; }
    public void setFirstMes(String firstMes) { this.firstMes = firstMes; }
    
    public String getMesExample() { return mesExample; }
    public void setMesExample(String mesExample) { this.mesExample = mesExample; }
    
    public String getCreatorNotes() { return creatorNotes; }
    public void setCreatorNotes(String creatorNotes) { this.creatorNotes = creatorNotes; }
    
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public String getAvatarDataType() { return avatarDataType; }
    public void setAvatarDataType(String avatarDataType) { this.avatarDataType = avatarDataType; }
    
    public String getApiKeyId() { return apiKeyId; }
    public void setApiKeyId(String apiKeyId) { this.apiKeyId = apiKeyId; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public String getCharacterVersion() { return characterVersion; }
    public void setCharacterVersion(String characterVersion) { this.characterVersion = characterVersion; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isSystem() { return isSystem; }
    public void setSystem(boolean system) { isSystem = system; }
    
    public String getExtensions() { return extensions; }
    public void setExtensions(String extensions) { this.extensions = extensions; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    @Override
    public String toString() {
        return name != null ? name : "Unnamed Role";
    }
}
