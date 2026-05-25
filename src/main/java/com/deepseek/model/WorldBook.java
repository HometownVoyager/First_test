package com.deepseek.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a World Book (Lorebook) containing multiple entries.
 */
public class WorldBook implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String description;
    private List<WorldBookEntry> entries;
    private long createdAt;
    private long updatedAt;
    
    public WorldBook() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.entries = new ArrayList<>();
    }
    
    public WorldBook(String name) {
        this();
        this.name = name;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<WorldBookEntry> getEntries() { return entries; }
    public void setEntries(List<WorldBookEntry> entries) { this.entries = entries; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    /**
     * Add an entry to this world book.
     */
    public void addEntry(WorldBookEntry entry) {
        if (entry != null) {
            entries.add(entry);
            this.updatedAt = System.currentTimeMillis();
        }
    }
    
    /**
     * Remove an entry by ID.
     */
    public boolean removeEntry(String entryId) {
        boolean removed = entries.removeIf(e -> e.getId().equals(entryId));
        if (removed) {
            this.updatedAt = System.currentTimeMillis();
        }
        return removed;
    }
    
    /**
     * Find all enabled entries that match the given text.
     */
    public List<WorldBookEntry> findMatchingEntries(String text) {
        List<WorldBookEntry> matching = new ArrayList<>();
        for (WorldBookEntry entry : entries) {
            if (entry.matches(text)) {
                matching.add(entry);
            }
        }
        // Sort by priority (higher first)
        matching.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        return matching;
    }
    
    @Override
    public String toString() {
        return name != null ? name : "Unnamed World Book";
    }
}
