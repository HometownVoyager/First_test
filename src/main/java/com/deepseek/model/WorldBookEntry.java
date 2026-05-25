package com.deepseek.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a World Info / Lorebook entry compatible with SillyTavern format.
 */
public class WorldBookEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String key; // Trigger keywords (comma-separated)
    private String content; // The lore content to inject
    private String comment; // Optional comment/description
    private int priority; // Higher priority entries are processed first
    private boolean enabled;
    private InsertPosition position; // Where to insert: BEFORE or AFTER
    private double selectivity; // Match threshold (0-1)
    private boolean useRegex; // Whether key is a regex pattern
    private List<String> keys; // Alternative keys list
    
    public enum InsertPosition {
        BEFORE, // Insert before user message
        AFTER   // Insert after user message
    }
    
    public WorldBookEntry() {
        this.id = java.util.UUID.randomUUID().toString();
        this.enabled = true;
        this.priority = 0;
        this.position = InsertPosition.BEFORE;
        this.selectivity = 1.0;
        this.useRegex = false;
        this.keys = new ArrayList<>();
    }
    
    public WorldBookEntry(String key, String content) {
        this();
        this.key = key;
        this.content = content;
        if (key != null) {
            this.keys = List.of(key.split("\\s*,\\s*"));
        }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getKey() { return key; }
    public void setKey(String key) { 
        this.key = key;
        if (key != null) {
            this.keys = List.of(key.split("\\s*,\\s*"));
        }
    }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public InsertPosition getPosition() { return position; }
    public void setPosition(InsertPosition position) { this.position = position; }
    
    public double getSelectivity() { return selectivity; }
    public void setSelectivity(double selectivity) { this.selectivity = selectivity; }
    
    public boolean isUseRegex() { return useRegex; }
    public void setUseRegex(boolean useRegex) { this.useRegex = useRegex; }
    
    public List<String> getKeys() { return keys; }
    public void setKeys(List<String> keys) { this.keys = keys; }
    
    /**
     * Check if the given text matches any of the trigger keys.
     */
    public boolean matches(String text) {
        if (!enabled || text == null) {
            return false;
        }
        String lowerText = text.toLowerCase();
        for (String k : keys) {
            if (k == null || k.trim().isEmpty()) continue;
            if (useRegex) {
                try {
                    if (lowerText.matches(k.toLowerCase())) {
                        return true;
                    }
                } catch (Exception e) {
                    // Invalid regex, skip
                }
            } else {
                if (lowerText.contains(k.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return comment != null ? comment : (key != null ? key : "Unnamed Entry");
    }
}
