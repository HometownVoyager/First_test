package com.deepseek.model;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a conversation (chat room) with multiple participants.
 */
public class Conversation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum SpeakMode {
        MANUAL,      // User manually specifies next speaker
        ROUND_ROBIN, // Roles take turns in order
        AUTO         // Model decides who speaks next
    }
    
    private String id;
    private String title;
    private List<Message> messages;
    private Set<String> participantRoleIds; // IDs of roles participating in this chat
    private Set<String> mutedRoleIds; // IDs of roles that are muted
    private String worldBookId; // Bound world book ID
    private SpeakMode speakMode;
    private String nextSpeakerId; // For MANUAL mode
    private long createdAt;
    private long updatedAt;
    private Map<String, Integer> messageCounts; // Track message count per role for round-robin
    
    public Conversation() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.messages = new ArrayList<>();
        this.participantRoleIds = new LinkedHashSet<>();
        this.mutedRoleIds = new HashSet<>();
        this.speakMode = SpeakMode.AUTO;
        this.messageCounts = new HashMap<>();
        // Always include narrator by default
        this.participantRoleIds.add(RoleCard.NARRATOR_ID);
    }
    
    public Conversation(String title) {
        this();
        this.title = title;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    
    public Set<String> getParticipantRoleIds() { return participantRoleIds; }
    public void setParticipantRoleIds(Set<String> participantRoleIds) { 
        this.participantRoleIds = participantRoleIds;
        // Ensure narrator is always included
        this.participantRoleIds.add(RoleCard.NARRATOR_ID);
    }
    
    public Set<String> getMutedRoleIds() { return mutedRoleIds; }
    public void setMutedRoleIds(Set<String> mutedRoleIds) { this.mutedRoleIds = mutedRoleIds; }
    
    public String getWorldBookId() { return worldBookId; }
    public void setWorldBookId(String worldBookId) { this.worldBookId = worldBookId; }
    
    public SpeakMode getSpeakMode() { return speakMode; }
    public void setSpeakMode(SpeakMode speakMode) { this.speakMode = speakMode; }
    
    public String getNextSpeakerId() { return nextSpeakerId; }
    public void setNextSpeakerId(String nextSpeakerId) { this.nextSpeakerId = nextSpeakerId; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    public Map<String, Integer> getMessageCounts() { return messageCounts; }
    public void setMessageCounts(Map<String, Integer> messageCounts) { this.messageCounts = messageCounts; }
    
    /**
     * Add a message to this conversation.
     */
    public void addMessage(Message message) {
        if (message != null) {
            messages.add(message);
            this.updatedAt = System.currentTimeMillis();
            
            // Update message count for round-robin
            if (message.getRoleId() != null) {
                messageCounts.merge(message.getRoleId(), 1, Integer::sum);
            }
        }
    }
    
    /**
     * Remove a message by ID (for regeneration).
     */
    public boolean removeMessage(String messageId) {
        boolean removed = messages.removeIf(m -> m.getId().equals(messageId));
        if (removed) {
            this.updatedAt = System.currentTimeMillis();
        }
        return removed;
    }
    
    /**
     * Get the last message in the conversation.
     */
    public Message getLastMessage() {
        if (messages.isEmpty()) return null;
        return messages.get(messages.size() - 1);
    }
    
    /**
     * Add a participant role to this conversation.
     */
    public void addParticipant(String roleId) {
        if (roleId != null) {
            participantRoleIds.add(roleId);
            this.updatedAt = System.currentTimeMillis();
        }
    }
    
    /**
     * Remove a participant role from this conversation.
     */
    public void removeParticipant(String roleId) {
        if (roleId != null && !RoleCard.NARRATOR_ID.equals(roleId)) {
            participantRoleIds.remove(roleId);
            this.updatedAt = System.currentTimeMillis();
        }
    }
    
    /**
     * Check if a role is muted.
     */
    public boolean isMuted(String roleId) {
        return mutedRoleIds.contains(roleId);
    }
    
    /**
     * Mute a role.
     */
    public void mute(String roleId) {
        if (roleId != null && !RoleCard.NARRATOR_ID.equals(roleId)) {
            mutedRoleIds.add(roleId);
            this.updatedAt = System.currentTimeMillis();
        }
    }
    
    /**
     * Unmute a role.
     */
    public void unmute(String roleId) {
        if (roleId != null) {
            mutedRoleIds.remove(roleId);
            this.updatedAt = System.currentTimeMillis();
        }
    }
    
    /**
     * Generate an auto title from the first message.
     */
    public void generateAutoTitle() {
        if (title == null || title.trim().isEmpty()) {
            if (!messages.isEmpty()) {
                Message firstMsg = messages.get(0);
                if (firstMsg.getContent() != null) {
                    String content = firstMsg.getContent();
                    title = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                } else {
                    title = "New Conversation";
                }
            } else {
                title = "New Conversation";
            }
        }
    }
    
    @Override
    public String toString() {
        return title != null ? title : ("Conversation " + id.substring(0, 8));
    }
}
