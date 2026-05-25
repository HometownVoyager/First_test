package com.deepseek.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single message in a conversation.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Role {
        USER("user"),
        ASSISTANT("assistant"),
        SYSTEM("system"),
        NARRATOR("narrator");
        
        private final String value;
        Role(String value) { this.value = value; }
        public String getValue() { return value; }
        
        public static Role fromString(String s) {
            for (Role r : values()) {
                if (r.value.equalsIgnoreCase(s)) return r;
            }
            return ASSISTANT;
        }
    }
    
    private String id;
    private Role role;
    private String content;
    private String roleId; // For assistant messages, which role card sent this
    private String roleName; // Display name of the role
    private long timestamp;
    private boolean edited; // Whether this message was manually edited
    private String thoughtContent; // Content from "thinking" mode
    
    public Message() {
        this.id = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.edited = false;
    }
    
    public Message(Role role, String content) {
        this();
        this.role = role;
        this.content = content;
    }
    
    public Message(Role role, String content, String roleId, String roleName) {
        this(role, content);
        this.roleId = roleId;
        this.roleName = roleName;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content;
        this.edited = true;
    }
    
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }
    
    public String getThoughtContent() { return thoughtContent; }
    public void setThoughtContent(String thoughtContent) { this.thoughtContent = thoughtContent; }
    
    /**
     * Mark this message as edited due to manual user modification.
     */
    public void markAsEdited() {
        this.edited = true;
    }
    
    @Override
    public String toString() {
        return (role != null ? role.name() : "UNKNOWN") + ": " + 
               (content != null ? (content.length() > 50 ? content.substring(0, 50) + "..." : content) : "");
    }
}
