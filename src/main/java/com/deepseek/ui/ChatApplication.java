package com.deepseek.ui;

import com.deepseek.api.ApiRequestConfig;
import com.deepseek.api.DeepSeekModel;
import com.deepseek.logic.ConversationLogic;
import com.deepseek.model.*;
import com.deepseek.storage.StorageManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Main application entry point for the JavaFX desktop application.
 */
public class ChatApplication extends Application {
    private ConversationLogic logic;
    private StorageManager storageManager;
    
    // UI Components
    private ListView<Conversation> conversationListView;
    private ListView<RoleCard> roleCardListView;
    private VBox chatArea;
    private TextArea messageInput;
    private Label currentModelLabel;
    private ComboBox<DeepSeekModel> modelComboBox;
    private CheckBox thinkingModeCheckBox;
    private VBox participantsPanel;
    
    private Conversation currentConversation;
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize backend
        storageManager = new StorageManager();
        logic = new ConversationLogic(storageManager);
        
        // Build UI
        BorderPane root = buildMainLayout();
        
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("DeepSeek AI Chat - Role Play Edition");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Load initial data
        refreshConversationList();
        refreshRoleCardList();
        
        // Create first conversation if none exists
        if (logic.getAllConversations().isEmpty()) {
            currentConversation = logic.createConversation("New Conversation");
            loadConversation(currentConversation);
        } else {
            currentConversation = logic.getAllConversations().get(0);
            loadConversation(currentConversation);
        }
    }
    
    private BorderPane buildMainLayout() {
        BorderPane root = new BorderPane();
        
        // Top toolbar
        root.setTop(buildToolbar());
        
        // Left sidebar
        root.setLeft(buildSidebar());
        
        // Center chat area
        root.setCenter(buildChatArea());
        
        // Right participants panel
        root.setRight(buildParticipantsPanel());
        
        return root;
    }
    
    private HBox buildToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
        
        // Model selection
        Label modelLabel = new Label("Model:");
        modelComboBox = new ComboBox<>();
        modelComboBox.getItems().addAll(DeepSeekModel.values());
        modelComboBox.setValue(DeepSeekModel.DEEPSEEK_CHAT);
        modelComboBox.setOnAction(e -> onModelChanged());
        
        // Thinking mode toggle
        thinkingModeCheckBox = new CheckBox("Thinking Mode");
        thinkingModeCheckBox.setOnAction(e -> onThinkingModeChanged());
        
        // World book button
        Button worldBookBtn = new Button("World Books");
        worldBookBtn.setOnAction(e -> openWorldBookManager());
        
        // API Keys button
        Button apiKeysBtn = new Button("API Keys");
        apiKeysBtn.setOnAction(e -> openApiKeyManager());
        
        // Settings button
        Button settingsBtn = new Button("Settings");
        settingsBtn.setOnAction(e -> openSettings());
        
        toolbar.getChildren().addAll(
            modelLabel, modelComboBox, 
            new Separator(), 
            thinkingModeCheckBox,
            new Separator(),
            worldBookBtn, apiKeysBtn, settingsBtn
        );
        
        return toolbar;
    }
    
    private VBox buildSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setStyle("-fx-padding: 10; -fx-min-width: 250; -fx-max-width: 250; -fx-background-color: #fafafa;");
        
        // Conversations section
        Label convLabel = new Label("Conversations");
        convLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        conversationListView = new ListView<>();
        conversationListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });
        conversationListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> onConversationSelected(newVal)
        );
        
        Button newConvBtn = new Button("+ New Conversation");
        newConvBtn.setOnAction(e -> createNewConversation());
        
        // Role cards section
        Label roleLabel = new Label("Role Cards");
        roleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-margin-top: 15px;");
        
        roleCardListView = new ListView<>();
        roleCardListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(RoleCard item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        roleCardListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> onRoleCardSelected(newVal)
        );
        
        Button newRoleBtn = new Button("+ New Role Card");
        newRoleBtn.setOnAction(e -> openRoleCardEditor(null));
        
        Button importRoleBtn = new Button("Import Character");
        importRoleBtn.setOnAction(e -> importSillyTavernCharacter());
        
        sidebar.getChildren().addAll(
            convLabel, conversationListView, newConvBtn,
            new Separator(),
            roleLabel, roleCardListView, newRoleBtn, importRoleBtn
        );
        
        return sidebar;
    }
    
    private BorderPane buildChatArea() {
        BorderPane chatPane = new BorderPane();
        
        // Messages display area
        chatArea = new VBox(10);
        chatArea.setStyle("-fx-padding: 15; -fx-background-color: #ffffff;");
        
        ScrollPane scrollPane = new ScrollPane(chatArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        chatPane.setCenter(scrollPane);
        
        // Input area
        VBox inputBox = new VBox(5);
        inputBox.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");
        
        messageInput = new TextArea();
        messageInput.setPromptText("Type your message...");
        messageInput.setPrefRowCount(3);
        messageInput.setWrapText(true);
        
        HBox buttonsBox = new HBox(10);
        
        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 8 20;");
        sendBtn.setOnAction(e -> sendMessage());
        
        Button regenerateBtn = new Button("Regenerate");
        regenerateBtn.setOnAction(e -> regenerateLastMessage());
        
        buttonsBox.getChildren().addAll(sendBtn, regenerateBtn);
        
        inputBox.getChildren().addAll(messageInput, buttonsBox);
        chatPane.setBottom(inputBox);
        
        return chatPane;
    }
    
    private VBox buildParticipantsPanel() {
        participantsPanel = new VBox(10);
        participantsPanel.setStyle("-fx-padding: 10; -fx-min-width: 200; -fx-max-width: 200; -fx-background-color: #fafafa; -fx-border-color: #ddd; -fx-border-width: 0 0 0 1;");
        
        Label label = new Label("Participants");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        participantsPanel.getChildren().add(label);
        
        return participantsPanel;
    }
    
    // ==================== Event Handlers ====================
    
    private void onModelChanged() {
        // Model changed, will be used for next request
    }
    
    private void onThinkingModeChanged() {
        // Thinking mode toggled
    }
    
    private void onConversationSelected(Conversation conversation) {
        if (conversation != null) {
            currentConversation = conversation;
            loadConversation(conversation);
        }
    }
    
    private void onRoleCardSelected(RoleCard roleCard) {
        // Can open editor or show details
    }
    
    private void sendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || currentConversation == null) {
            return;
        }
        
        // Add user message
        logic.addUserMessage(currentConversation, content);
        messageInput.clear();
        
        // Refresh chat display
        loadConversation(currentConversation);
        
        // Determine next speaker and generate response
        try {
            String nextSpeakerId = logic.determineNextSpeaker(currentConversation);
            ApiRequestConfig config = buildRequestConfig();
            logic.generateResponse(currentConversation, nextSpeakerId, config);
            loadConversation(currentConversation);
        } catch (Exception e) {
            showError("Failed to generate response: " + e.getMessage());
        }
    }
    
    private void regenerateLastMessage() {
        if (currentConversation == null) {
            return;
        }
        
        try {
            ApiRequestConfig config = buildRequestConfig();
            logic.regenerateLastMessage(currentConversation, config);
            loadConversation(currentConversation);
        } catch (Exception e) {
            showError("Failed to regenerate: " + e.getMessage());
        }
    }
    
    private void createNewConversation() {
        Conversation conv = logic.createConversation("New Conversation");
        refreshConversationList();
        currentConversation = conv;
        loadConversation(conv);
    }
    
    private void loadConversation(Conversation conversation) {
        chatArea.getChildren().clear();
        
        for (Message msg : conversation.getMessages()) {
            addMessageBubble(msg);
        }
        
        // Update participants panel
        updateParticipantsPanel(conversation);
    }
    
    private void addMessageBubble(Message msg) {
        HBox bubbleBox = new HBox();
        bubbleBox.setStyle("-fx-padding: 5;");
        
        VBox contentBox = new VBox(3);
        contentBox.setStyle("-fx-padding: 10; -fx-background-radius: 10; -fx-background-color: " + 
            (msg.getRole() == Message.Role.USER ? "#dcf8c6" : "#ffffff") + ";");
        contentBox.setMaxWidth(400);
        
        Label nameLabel = new Label(
            msg.getRoleName() != null ? msg.getRoleName() : msg.getRole().name()
        );
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        Label contentLabel = new Label(msg.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px;");
        
        contentBox.getChildren().addAll(nameLabel, contentLabel);
        
        if (msg.isEdited()) {
            Label editedLabel = new Label("(edited)");
            editedLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
            contentBox.getChildren().add(editedLabel);
        }
        
        bubbleBox.getChildren().add(contentBox);
        
        if (msg.getRole() == Message.Role.USER) {
            bubbleBox.setStyle("-fx-alignment: center-right;");
        }
        
        chatArea.getChildren().add(bubbleBox);
    }
    
    private void updateParticipantsPanel(Conversation conversation) {
        participantsPanel.getChildren().clear();
        
        Label label = new Label("Participants");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        participantsPanel.getChildren().add(label);
        
        for (String roleId : conversation.getParticipantRoleIds()) {
            RoleCard role = logic.getRoleCard(roleId);
            if (role == null) continue;
            
            HBox row = new HBox(5);
            row.setStyle("-fx-padding: 5; -fx-alignment: center-left;");
            
            Label nameLabel = new Label(role.getName());
            nameLabel.setStyle("-fx-font-size: 13px;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            if (conversation.isMuted(roleId)) {
                Button unmuteBtn = new Button("🔇");
                unmuteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");
                unmuteBtn.setOnAction(e -> {
                    logic.unmuteRole(conversation, roleId);
                    updateParticipantsPanel(conversation);
                });
                row.getChildren().addAll(nameLabel, spacer, unmuteBtn);
            } else {
                Button muteBtn = new Button("🔊");
                muteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");
                muteBtn.setOnAction(e -> {
                    logic.muteRole(conversation, roleId);
                    updateParticipantsPanel(conversation);
                });
                row.getChildren().addAll(nameLabel, spacer, muteBtn);
            }
            
            if (!RoleCard.NARRATOR_ID.equals(roleId)) {
                Button removeBtn = new Button("✕");
                removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 12px;");
                removeBtn.setOnAction(e -> {
                    logic.removeParticipant(conversation, roleId);
                    updateParticipantsPanel(conversation);
                });
                row.getChildren().add(removeBtn);
            }
            
            participantsPanel.getChildren().add(row);
        }
        
        // Add participant button
        Button addBtn = new Button("+ Add Role");
        addBtn.setOnAction(e -> addParticipant(conversation));
        participantsPanel.getChildren().add(addBtn);
    }
    
    private void addParticipant(Conversation conversation) {
        ChoiceDialog<RoleCard> dialog = new ChoiceDialog<>(logic.getAllRoleCards().get(0), logic.getAllRoleCards());
        dialog.setTitle("Add Participant");
        dialog.setHeaderText("Select a role to add:");
        
        dialog.showAndWait().ifPresent(role -> {
            logic.addParticipant(conversation, role.getId());
            updateParticipantsPanel(conversation);
        });
    }
    
    private void refreshConversationList() {
        conversationListView.getItems().setAll(logic.getAllConversations());
    }
    
    private void refreshRoleCardList() {
        roleCardListView.getItems().setAll(logic.getAllRoleCards());
    }
    
    private ApiRequestConfig buildRequestConfig() {
        ApiRequestConfig config = new ApiRequestConfig();
        config.setModel(modelComboBox.getValue());
        config.setThinkingEnabled(thinkingModeCheckBox.isSelected());
        return config;
    }
    
    // ==================== Dialogs ====================
    
    private void openWorldBookManager() {
        // TODO: Implement world book manager dialog
        showAlert("World Books", "World Book management coming soon.");
    }
    
    private void openApiKeyManager() {
        // TODO: Implement API key manager dialog
        showAlert("API Keys", "API Key management coming soon.");
    }
    
    private void openSettings() {
        // TODO: Implement settings dialog
        showAlert("Settings", "Settings dialog coming soon.");
    }
    
    private void openRoleCardEditor(RoleCard existingCard) {
        // TODO: Implement role card editor dialog
        showAlert("Role Card Editor", "Role card editor coming soon.");
    }
    
    private void importSillyTavernCharacter() {
        // TODO: Implement file picker and import
        showAlert("Import", "Character import coming soon.");
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
