package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.entities.ForumPost;
import org.example.controllers.frontoffice.ForumPostFrontController;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostListController {
    @FXML
    private VBox postsContainer;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> typeFilter;
    
    @FXML
    private Button refreshBtn;

    private MainController mainController;
    private ForumPostFrontController postController = new ForumPostFrontController();

    @FXML
    public void initialize() {
        typeFilter.getItems().addAll("ALL", "AVIS", "RECLAMATION", "RECOMMANDATION", "DISCUSSION");
        typeFilter.setValue("ALL");
        loadPosts();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void loadPosts() {
        try {
            postsContainer.getChildren().clear();
            List<ForumPost> posts = postController.listPosts();
            
            if (posts == null || posts.isEmpty()) {
                VBox emptyState = new VBox(15);
                emptyState.setAlignment(javafx.geometry.Pos.CENTER);
                emptyState.setStyle("-fx-padding: 80px;");
                
                Label icon = new Label("🏝");
                icon.setStyle("-fx-font-size: 64px;");
                
                Label title = new Label("No Hidden Gems Yet!");
                title.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1B7B7E;");
                
                Label message = new Label("Be the first to share a discovery about Tunisia's hidden treasures.");
                message.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568; -fx-wrap-text: true; -fx-text-alignment: center;");
                message.setWrapText(true);
                message.setMaxWidth(400);
                
                emptyState.getChildren().addAll(icon, title, message);
                postsContainer.getChildren().add(emptyState);
                return;
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String filterType = typeFilter.getValue() != null ? typeFilter.getValue() : "ALL";
            String searchText = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
            
            int displayedCount = 0;
            for (ForumPost post : posts) {
                if (post == null) continue;
                
                // Apply type filter
                if (!filterType.equals("ALL") && post.getType() != null && !post.getType().equals(filterType)) {
                    continue;
                }
                
                // Apply search filter
                if (!searchText.isEmpty()) {
                    String title = post.getTitle() != null ? post.getTitle().toLowerCase() : "";
                    String content = post.getContent() != null ? post.getContent().toLowerCase() : "";
                    if (!title.contains(searchText) && !content.contains(searchText)) {
                        continue;
                    }
                }
                
                // Create post card
                VBox card = createPostCard(post, formatter);
                postsContainer.getChildren().add(card);
                displayedCount++;
            }
            
            if (displayedCount == 0) {
                VBox emptyState = new VBox(15);
                emptyState.setAlignment(javafx.geometry.Pos.CENTER);
                emptyState.setStyle("-fx-padding: 80px;");
                
                Label icon = new Label("🔍");
                icon.setStyle("-fx-font-size: 64px;");
                
                Label title = new Label("No Results Found");
                title.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #D2691E;");
                
                Label message = new Label("Try adjusting your search or filter criteria.");
                message.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");
                
                emptyState.getChildren().addAll(icon, title, message);
                postsContainer.getChildren().add(emptyState);
            }
        } catch (Exception e) {
            e.printStackTrace();
            VBox errorState = new VBox(15);
            errorState.setAlignment(javafx.geometry.Pos.CENTER);
            errorState.setStyle("-fx-padding: 80px;");
            
            Label icon = new Label("⚠");
            icon.setStyle("-fx-font-size: 64px; -fx-text-fill: #D2691E;");
            
            Label title = new Label("Error Loading Posts");
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #D2691E;");
            
            Label message = new Label("Unable to connect to the database. Please check your connection.");
            message.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568; -fx-wrap-text: true; -fx-text-alignment: center;");
            message.setWrapText(true);
            message.setMaxWidth(500);
            
            errorState.getChildren().addAll(icon, title, message);
            postsContainer.getChildren().add(errorState);
        }
    }

    private VBox createPostCard(ForumPost post, DateTimeFormatter formatter) {
        VBox card = new VBox(10);
        card.getStyleClass().add("post-card");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        // Header
        HBox header = new HBox(10);
        header.getStyleClass().add("post-header");
        
        String title = post.getTitle() != null ? post.getTitle() : "No Title";
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("post-title");
        titleLabel.setWrapText(true);
        
        String type = post.getType() != null ? post.getType() : "DISCUSSION";
        Label typeLabel = new Label(type);
        typeLabel.getStyleClass().add("post-type-" + type.toLowerCase());
        
        String status = post.getStatus() != null ? post.getStatus() : "EN_ATTENTE";
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("post-status");
        
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        header.getChildren().addAll(titleLabel, typeLabel, statusLabel);
        
        // Content preview
        String content = post.getContent() != null ? post.getContent() : "";
        String contentPreview = content.length() > 150 ? content.substring(0, 150) + "..." : content;
        Label contentLabel = new Label(contentPreview);
        contentLabel.getStyleClass().add("post-content");
        contentLabel.setWrapText(true);
        
        // Footer
        HBox footer = new HBox(15);
        footer.getStyleClass().add("post-footer");
        
        Integer userId = post.getUserId();
        Label userLabel = new Label("User: " + (userId != null ? userId : "N/A"));
        
        String dateStr = "Posted: N/A";
        if (post.getCreatedAt() != null) {
            dateStr = "Posted: " + post.getCreatedAt().format(formatter);
        }
        Label dateLabel = new Label(dateStr);
        
        Integer viewCount = post.getViewCount() != null ? post.getViewCount() : 0;
        Label viewsLabel = new Label("Views: " + viewCount);
        
        if (post.getRating() != null && post.getRating() > 0) {
            Label ratingLabel = new Label("★ " + post.getRating());
            ratingLabel.getStyleClass().add("post-rating");
            footer.getChildren().add(ratingLabel);
        }
        
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("btn-view");
        viewBtn.setOnAction(e -> {
            if (post.getId() != null) {
                mainController.loadPostDetail(post.getId());
            }
        });
        
        HBox.setHgrow(userLabel, Priority.ALWAYS);
        footer.getChildren().addAll(userLabel, dateLabel, viewsLabel, viewBtn);
        
        card.getChildren().addAll(header, contentLabel, footer);
        return card;
    }

    @FXML
    public void onSearch() {
        loadPosts();
    }

    @FXML
    public void onSearchKeyReleased() {
        loadPosts();
    }

    @FXML
    public void onFilterChange() {
        loadPosts();
    }

    @FXML
    public void clearSearch() {
        searchField.clear();
        typeFilter.setValue("ALL");
        loadPosts();
    }
}
