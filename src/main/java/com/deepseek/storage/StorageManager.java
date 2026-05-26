package com.deepseek.storage;

import com.deepseek.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Handles persistence of all application data to local files.
 */
public class StorageManager {
    private static final String DATA_DIR = System.getProperty("user.home") + "/.deepseek-chat-app";
    private static final String CONVERSATIONS_FILE = DATA_DIR + "/conversations.json";
    private static final String ROLE_CARDS_FILE = DATA_DIR + "/role_cards.json";
    private static final String API_KEYS_FILE = DATA_DIR + "/api_keys.json";
    private static final String WORLD_BOOKS_FILE = DATA_DIR + "/world_books.json";
    private static final String SETTINGS_FILE = DATA_DIR + "/settings.json";
    
    private final Gson gson;
    
    public StorageManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        
        // Ensure data directory exists
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Get the data directory path.
     */
    public String getDataDir() {
        return DATA_DIR;
    }
    
    // ==================== Conversations ====================
    
    /**
     * Load all conversations from file.
     */
    public List<Conversation> loadConversations() {
        return loadList(CONVERSATIONS_FILE, Conversation.class);
    }
    
    /**
     * Save all conversations to file.
     */
    public void saveConversations(List<Conversation> conversations) {
        saveList(CONVERSATIONS_FILE, conversations);
    }
    
    // ==================== Role Cards ====================
    
    /**
     * Load all role cards from file.
     */
    public List<RoleCard> loadRoleCards() {
        List<RoleCard> cards = loadList(ROLE_CARDS_FILE, RoleCard.class);
        // Ensure narrator always exists
        boolean hasNarrator = cards.stream().anyMatch(r -> RoleCard.NARRATOR_ID.equals(r.getId()));
        if (!hasNarrator) {
            cards.add(RoleCard.createNarrator());
        }
        return cards;
    }
    
    /**
     * Save all role cards to file.
     */
    public void saveRoleCards(List<RoleCard> roleCards) {
        saveList(ROLE_CARDS_FILE, roleCards);
    }
    
    // ==================== API Keys ====================
    
    /**
     * Load all API key configurations from file.
     */
    public List<ApiKeyConfig> loadApiKeys() {
        return loadList(API_KEYS_FILE, ApiKeyConfig.class);
    }
    
    /**
     * Save all API key configurations to file.
     */
    public void saveApiKeys(List<ApiKeyConfig> apiKeys) {
        saveList(API_KEYS_FILE, apiKeys);
    }
    
    // ==================== World Books ====================
    
    /**
     * Load all world books from file.
     */
    public List<WorldBook> loadWorldBooks() {
        return loadList(WORLD_BOOKS_FILE, WorldBook.class);
    }
    
    /**
     * Save all world books to file.
     */
    public void saveWorldBooks(List<WorldBook> worldBooks) {
        saveList(WORLD_BOOKS_FILE, worldBooks);
    }
    
    // ==================== Generic List Load/Save ====================
    
    @SuppressWarnings("unchecked")
    private <T> List<T> loadList(String filePath, Class<T> clazz) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("items")) {
                return gson.fromJson(root.getAsJsonArray("items"), 
                    com.google.gson.reflect.TypeToken.getParameterized(List.class, clazz).getType());
            }
        } catch (Exception e) {
            System.err.println("Error loading " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    private <T> void saveList(String filePath, List<T> items) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        
        // Ensure parent directory exists
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("Failed to create directory: " + parentDir.getAbsolutePath());
                return;
            }
        }
        
        JsonObject root = new JsonObject();
        root.add("items", gson.toJsonTree(items));
        
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            System.err.println("Error saving " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ==================== SillyTavern Import/Export ====================
    
    /**
     * Import a SillyTavern character card from JSON string.
     */
    public RoleCard importSillyTavernCharacter(String jsonContent) {
        try {
            JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();
            RoleCard card = new RoleCard();
            
            // Extract standard fields
            if (root.has("name")) card.setName(root.get("name").getAsString());
            if (root.has("description")) card.setDescription(root.get("description").getAsString());
            if (root.has("personality")) card.setPersonality(root.get("personality").getAsString());
            if (root.has("scenario")) card.setScenario(root.get("scenario").getAsString());
            if (root.has("first_mes")) card.setFirstMes(root.get("first_mes").getAsString());
            if (root.has("mes_example")) card.setMesExample(root.get("mes_example").getAsString());
            if (root.has("creator_notes")) card.setCreatorNotes(root.get("creator_notes").getAsString());
            
            // Handle avatar (may be base64 or path)
            if (root.has("avatar")) {
                card.setAvatar(root.get("avatar").getAsString());
            }
            
            // Store raw data for compatibility
            card.setData(jsonContent);
            card.setUpdatedAt(System.currentTimeMillis());
            
            return card;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Export a role card to SillyTavern format JSON.
     */
    public String exportSillyTavernCharacter(RoleCard card) {
        JsonObject root = new JsonObject();
        
        if (card.getName() != null) root.addProperty("name", card.getName());
        if (card.getDescription() != null) root.addProperty("description", card.getDescription());
        if (card.getPersonality() != null) root.addProperty("personality", card.getPersonality());
        if (card.getScenario() != null) root.addProperty("scenario", card.getScenario());
        if (card.getFirstMes() != null) root.addProperty("first_mes", card.getFirstMes());
        if (card.getMesExample() != null) root.addProperty("mes_example", card.getMesExample());
        if (card.getCreatorNotes() != null) root.addProperty("creator_notes", card.getCreatorNotes());
        if (card.getAvatar() != null) root.addProperty("avatar", card.getAvatar());
        
        // Add version info
        root.addProperty("spec", "chara_card_v3");
        root.addProperty("spec_version", "3.0");
        
        return gson.toJson(root);
    }
    
    /**
     * Import a SillyTavern world book from JSON string.
     */
    public WorldBook importSillyTavernWorldBook(String jsonContent) {
        try {
            JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();
            WorldBook book = new WorldBook();
            
            if (root.has("name")) book.setName(root.get("name").getAsString());
            if (root.has("description")) book.setDescription(root.get("description").getAsString());
            
            // Parse entries
            if (root.has("entries")) {
                JsonObject entriesObj = root.getAsJsonObject("entries");
                for (String key : entriesObj.keySet()) {
                    JsonObject entryObj = entriesObj.getAsJsonObject(key);
                    WorldBookEntry entry = new WorldBookEntry();
                    
                    if (entryObj.has("key")) entry.setKey(entryObj.get("key").getAsString());
                    if (entryObj.has("content")) entry.setContent(entryObj.get("content").getAsString());
                    if (entryObj.has("comment")) entry.setComment(entryObj.get("comment").getAsString());
                    if (entryObj.has("priority")) entry.setPriority(entryObj.get("priority").getAsInt());
                    if (entryObj.has("enabled")) entry.setEnabled(entryObj.get("enabled").getAsBoolean());
                    if (entryObj.has("position")) {
                        String pos = entryObj.get("position").getAsString();
                        entry.setPosition("after".equalsIgnoreCase(pos) ? 
                            WorldBookEntry.InsertPosition.AFTER : WorldBookEntry.InsertPosition.BEFORE);
                    }
                    
                    book.addEntry(entry);
                }
            }
            
            return book;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Export a world book to SillyTavern format JSON.
     */
    public String exportSillyTavernWorldBook(WorldBook book) {
        JsonObject root = new JsonObject();
        
        if (book.getName() != null) root.addProperty("name", book.getName());
        if (book.getDescription() != null) root.addProperty("description", book.getDescription());
        
        JsonObject entriesObj = new JsonObject();
        int index = 0;
        for (WorldBookEntry entry : book.getEntries()) {
            JsonObject entryObj = new JsonObject();
            
            if (entry.getKey() != null) entryObj.addProperty("key", entry.getKey());
            if (entry.getContent() != null) entryObj.addProperty("content", entry.getContent());
            if (entry.getComment() != null) entryObj.addProperty("comment", entry.getComment());
            entryObj.addProperty("priority", entry.getPriority());
            entryObj.addProperty("enabled", entry.isEnabled());
            entryObj.addProperty("position", entry.getPosition() == WorldBookEntry.InsertPosition.AFTER ? "after" : "before");
            
            entriesObj.add(String.valueOf(index++), entryObj);
        }
        root.add("entries", entriesObj);
        
        return gson.toJson(root);
    }
    
    // ==================== PNG Character Card (with embedded JSON) ====================
    
    /**
     * Read JSON data from a PNG file's tEXt chunk (SillyTavern format).
     */
    public String readPngCharaData(File pngFile) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(pngFile, "r")) {
            // Skip PNG signature
            byte[] sig = new byte[8];
            raf.read(sig);
            
            // Read chunks until we find tEXt with "chara" keyword
            while (raf.getFilePointer() < raf.length()) {
                int length = raf.readInt();
                byte[] typeBytes = new byte[4];
                raf.read(typeBytes);
                String type = new String(typeBytes, StandardCharsets.US_ASCII);
                
                if ("tEXt".equals(type)) {
                    byte[] data = new byte[length];
                    raf.read(data);
                    String text = new String(data, StandardCharsets.ISO_8859_1);
                    
                    // Check for chara keyword
                    if (text.startsWith("chara=")) {
                        String base64Data = text.substring(6);
                        byte[] decoded = Base64.getDecoder().decode(base64Data);
                        return new String(decoded, StandardCharsets.UTF_8);
                    }
                } else {
                    // Skip chunk data and CRC
                    raf.skipBytes(length + 4);
                }
            }
        }
        return null;
    }
}
