package com.deepseek.logic;

import com.deepseek.api.*;
import com.deepseek.model.*;
import com.deepseek.storage.StorageManager;

import java.io.IOException;
import java.util.*;

/**
 * Core business logic for conversation management, speaking strategies,
 * world book matching, and context building.
 */
public class ConversationLogic {
    private final StorageManager storageManager;
    private final DeepSeekApiService apiService;
    
    // Cached data
    private List<Conversation> conversations;
    private List<RoleCard> roleCards;
    private List<ApiKeyConfig> apiKeys;
    private List<WorldBook> worldBooks;
    
    public ConversationLogic(StorageManager storageManager) {
        this.storageManager = storageManager;
        this.apiService = new DeepSeekApiService();
        loadData();
    }
    
    /**
     * Load all data from storage.
     */
    public void loadData() {
        this.conversations = storageManager.loadConversations();
        this.roleCards = storageManager.loadRoleCards();
        this.apiKeys = storageManager.loadApiKeys();
        this.worldBooks = storageManager.loadWorldBooks();
    }
    
    /**
     * Save all data to storage.
     */
    public void saveData() {
        storageManager.saveConversations(conversations);
        storageManager.saveRoleCards(roleCards);
        storageManager.saveApiKeys(apiKeys);
        storageManager.saveWorldBooks(worldBooks);
    }
    
    // ==================== Conversations ====================
    
    /**
     * Create a new conversation.
     */
    public Conversation createConversation(String title) {
        Conversation conv = new Conversation(title);
        conv.addParticipant(RoleCard.NARRATOR_ID);
        conversations.add(conv);
        saveData();
        return conv;
    }
    
    /**
     * Delete a conversation by ID.
     */
    public boolean deleteConversation(String conversationId) {
        boolean removed = conversations.removeIf(c -> c.getId().equals(conversationId));
        if (removed) {
            saveData();
        }
        return removed;
    }
    
    /**
     * Get a conversation by ID.
     */
    public Conversation getConversation(String conversationId) {
        return conversations.stream()
                .filter(c -> c.getId().equals(conversationId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get all conversations.
     */
    public List<Conversation> getAllConversations() {
        return new ArrayList<>(conversations);
    }
    
    // ==================== Role Cards ====================
    
    /**
     * Get all role cards.
     */
    public List<RoleCard> getAllRoleCards() {
        return new ArrayList<>(roleCards);
    }
    
    /**
     * Get a role card by ID.
     */
    public RoleCard getRoleCard(String roleId) {
        return roleCards.stream()
                .filter(r -> r.getId().equals(roleId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Add or update a role card.
     */
    public void saveRoleCard(RoleCard card) {
        Optional<RoleCard> existing = roleCards.stream()
                .filter(r -> r.getId().equals(card.getId()))
                .findFirst();
        
        if (existing.isPresent()) {
            int index = roleCards.indexOf(existing.get());
            roleCards.set(index, card);
        } else {
            roleCards.add(card);
        }
        saveData();
    }
    
    /**
     * Delete a role card by ID (cannot delete narrator).
     */
    public boolean deleteRoleCard(String roleId) {
        if (RoleCard.NARRATOR_ID.equals(roleId)) {
            return false; // Cannot delete narrator
        }
        boolean removed = roleCards.removeIf(r -> r.getId().equals(roleId));
        if (removed) {
            saveData();
        }
        return removed;
    }
    
    // ==================== API Keys ====================
    
    /**
     * Get all API key configurations.
     */
    public List<ApiKeyConfig> getAllApiKeys() {
        return new ArrayList<>(apiKeys);
    }
    
    /**
     * Get an API key config by ID.
     */
    public ApiKeyConfig getApiKey(String keyId) {
        return apiKeys.stream()
                .filter(k -> k.getId().equals(keyId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Add or update an API key config.
     */
    public void saveApiKey(ApiKeyConfig config) {
        Optional<ApiKeyConfig> existing = apiKeys.stream()
                .filter(k -> k.getId().equals(config.getId()))
                .findFirst();
        
        if (existing.isPresent()) {
            config.setUpdatedAt(System.currentTimeMillis());
            int index = apiKeys.indexOf(existing.get());
            apiKeys.set(index, config);
        } else {
            apiKeys.add(config);
        }
        saveData();
    }
    
    /**
     * Delete an API key config by ID.
     */
    public boolean deleteApiKey(String keyId) {
        boolean removed = apiKeys.removeIf(k -> k.getId().equals(keyId));
        if (removed) {
            saveData();
        }
        return removed;
    }
    
    // ==================== World Books ====================
    
    /**
     * Get all world books.
     */
    public List<WorldBook> getAllWorldBooks() {
        return new ArrayList<>(worldBooks);
    }
    
    /**
     * Get a world book by ID.
     */
    public WorldBook getWorldBook(String bookId) {
        return worldBooks.stream()
                .filter(w -> w.getId().equals(bookId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Save a world book.
     */
    public void saveWorldBook(WorldBook book) {
        Optional<WorldBook> existing = worldBooks.stream()
                .filter(w -> w.getId().equals(book.getId()))
                .findFirst();
        
        if (existing.isPresent()) {
            int index = worldBooks.indexOf(existing.get());
            worldBooks.set(index, book);
        } else {
            worldBooks.add(book);
        }
        saveData();
    }
    
    /**
     * Delete a world book by ID.
     */
    public boolean deleteWorldBook(String bookId) {
        boolean removed = worldBooks.removeIf(w -> w.getId().equals(bookId));
        if (removed) {
            saveData();
        }
        return removed;
    }
    
    // ==================== Conversation Operations ====================
    
    /**
     * Add a user message to the conversation.
     */
    public void addUserMessage(Conversation conversation, String content) {
        Message msg = new Message(Message.Role.USER, content);
        conversation.addMessage(msg);
        
        // Auto-generate title if needed
        if (conversation.getTitle() == null || conversation.getTitle().trim().isEmpty()) {
            conversation.generateAutoTitle();
        }
        
        saveData();
    }
    
    /**
     * Edit an existing message.
     */
    public void editMessage(Conversation conversation, String messageId, String newContent) {
        for (Message msg : conversation.getMessages()) {
            if (msg.getId().equals(messageId)) {
                msg.setContent(newContent);
                msg.markAsEdited();
                break;
            }
        }
        saveData();
    }
    
    /**
     * Remove a message (for regeneration).
     */
    public void removeMessage(Conversation conversation, String messageId) {
        conversation.removeMessage(messageId);
        saveData();
    }
    
    /**
     * Add a participant to a conversation.
     */
    public void addParticipant(Conversation conversation, String roleId) {
        conversation.addParticipant(roleId);
        saveData();
    }
    
    /**
     * Remove a participant from a conversation.
     */
    public void removeParticipant(Conversation conversation, String roleId) {
        conversation.removeParticipant(roleId);
        saveData();
    }
    
    /**
     * Mute a role in a conversation.
     */
    public void muteRole(Conversation conversation, String roleId) {
        conversation.mute(roleId);
        saveData();
    }
    
    /**
     * Unmute a role in a conversation.
     */
    public void unmuteRole(Conversation conversation, String roleId) {
        conversation.unmute(roleId);
        saveData();
    }
    
    // ==================== Message Generation ====================
    
    /**
     * Determine the next speaker based on the speak mode.
     * Optimized with early exit and reduced stream operations.
     */
    public String determineNextSpeaker(Conversation conversation) {
        Set<String> participants = conversation.getParticipantRoleIds();
        Set<String> muted = conversation.getMutedRoleIds();
        
        // Filter out muted roles (except narrator can always speak)
        // Optimized: use loop instead of stream for better performance
        List<String> activeParticipants = new ArrayList<>(participants.size());
        for (String id : participants) {
            if (RoleCard.NARRATOR_ID.equals(id) || !muted.contains(id)) {
                activeParticipants.add(id);
            }
        }
        
        if (activeParticipants.isEmpty()) {
            return RoleCard.NARRATOR_ID;
        }
        
        switch (conversation.getSpeakMode()) {
            case MANUAL:
                String nextId = conversation.getNextSpeakerId();
                if (nextId != null && activeParticipants.contains(nextId)) {
                    return nextId;
                }
                // Fall back to first active participant
                return activeParticipants.get(0);
                
            case ROUND_ROBIN:
                // Find the role with the fewest messages - optimized without stream
                Map<String, Integer> counts = conversation.getMessageCounts();
                String minRole = null;
                int minCount = Integer.MAX_VALUE;
                for (String id : activeParticipants) {
                    int count = counts.getOrDefault(id, 0);
                    if (count < minCount) {
                        minCount = count;
                        minRole = id;
                    }
                }
                return minRole != null ? minRole : activeParticipants.get(0);
                
            case AUTO:
            default:
                // Return the last assistant speaker's role, or first active
                List<Message> messages = conversation.getMessages();
                for (int i = messages.size() - 1; i >= 0; i--) {
                    Message msg = messages.get(i);
                    if (msg.getRole() == Message.Role.ASSISTANT && 
                        activeParticipants.contains(msg.getRoleId())) {
                        // Return a different role if possible
                        for (String p : activeParticipants) {
                            if (!p.equals(msg.getRoleId())) {
                                return p;
                            }
                        }
                        return msg.getRoleId();
                    }
                }
                return activeParticipants.get(0);
        }
    }
    
    /**
     * Build the context messages for API request, including world book injection.
     * Optimized with StringBuilder and reduced object allocations.
     */
    public List<Message> buildContextMessages(Conversation conversation, String currentSpeakerId) {
        List<Message> context = new ArrayList<>();
        RoleCard speaker = getRoleCard(currentSpeakerId);
        
        if (speaker == null) {
            return context;
        }
        
        // Build system prompt
        StringBuilder systemPrompt = new StringBuilder();
        
        // Add role's system prompt
        if (speaker.getSystemPrompt() != null && !speaker.getSystemPrompt().isEmpty()) {
            systemPrompt.append(speaker.getSystemPrompt());
        }
        
        // Inject world book entries
        if (conversation.getWorldBookId() != null) {
            WorldBook book = getWorldBook(conversation.getWorldBookId());
            if (book != null) {
                // Get the last user message for matching - optimized search
                String lastUserMessage = null;
                List<Message> messages = conversation.getMessages();
                for (int i = messages.size() - 1; i >= 0; i--) {
                    Message msg = messages.get(i);
                    if (msg.getRole() == Message.Role.USER) {
                        lastUserMessage = msg.getContent();
                        break;
                    }
                }
                
                if (lastUserMessage != null) {
                    List<WorldBookEntry> matchingEntries = book.findMatchingEntries(lastUserMessage);
                    for (WorldBookEntry entry : matchingEntries) {
                        systemPrompt.append("\n\n[World Info: ");
                        systemPrompt.append(entry.getComment() != null ? 
                                entry.getComment() : entry.getKey()).append("]\n");
                        systemPrompt.append(entry.getContent());
                    }
                }
            }
        }
        
        // Add system message
        if (systemPrompt.length() > 0) {
            Message sysMsg = new Message(Message.Role.SYSTEM, systemPrompt.toString());
            context.add(sysMsg);
        }
        
        // Add conversation history (excluding SYSTEM messages to avoid duplicates)
        // Pre-size the list to avoid reallocations
        int estimatedSize = conversation.getMessages().size();
        context.ensureCapacity(estimatedSize + 1);
        
        for (Message msg : conversation.getMessages()) {
            if (msg.getRole() != Message.Role.SYSTEM) {
                context.add(msg);
            }
        }
        
        return context;
    }
    
    /**
     * Generate a response from a specific role.
     */
    public ApiResponse generateResponse(Conversation conversation, String speakerId, 
                                        ApiRequestConfig config) throws IOException {
        RoleCard speaker = getRoleCard(speakerId);
        if (speaker == null) {
            return ApiResponse.error("Role not found: " + speakerId);
        }
        
        // Get the API key bound to this role
        String apiKeyId = speaker.getApiKeyId();
        if (apiKeyId == null) {
            // Use first available API key
            if (apiKeys.isEmpty()) {
                return ApiResponse.error("No API key configured. Please add an API key first.");
            }
            apiKeyId = apiKeys.get(0).getId();
        }
        
        ApiKeyConfig apiKeyConfig = getApiKey(apiKeyId);
        if (apiKeyConfig == null) {
            return ApiResponse.error("API key not found for role: " + speaker.getName());
        }
        
        // Build context
        List<Message> context = buildContextMessages(conversation, speakerId);
        
        // Send request
        ApiResponse response = apiService.sendChatRequest(apiKeyConfig, config, context);
        
        if (response.isSuccess()) {
            // Create assistant message
            Message assistantMsg = new Message(
                    Message.Role.ASSISTANT, 
                    response.getContent(),
                    speakerId,
                    speaker.getName()
            );
            assistantMsg.setThoughtContent(response.getThoughtContent());
            conversation.addMessage(assistantMsg);
            saveData();
        }
        
        return response;
    }
    
    /**
     * Regenerate the last assistant message.
     */
    public ApiResponse regenerateLastMessage(Conversation conversation, ApiRequestConfig config) 
            throws IOException {
        // Find the last assistant message
        Message lastAssistantMsg = null;
        int lastIndex = -1;
        
        for (int i = conversation.getMessages().size() - 1; i >= 0; i--) {
            Message msg = conversation.getMessages().get(i);
            if (msg.getRole() == Message.Role.ASSISTANT) {
                lastAssistantMsg = msg;
                lastIndex = i;
                break;
            }
        }
        
        if (lastAssistantMsg == null) {
            return ApiResponse.error("No assistant message to regenerate.");
        }
        
        // Remove the last assistant message
        conversation.removeMessage(lastAssistantMsg.getId());
        
        // Generate new response with the same speaker
        return generateResponse(conversation, lastAssistantMsg.getRoleId(), config);
    }
}
