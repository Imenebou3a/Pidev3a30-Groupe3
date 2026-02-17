package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.entities.ForumPost;
import org.example.controllers.backoffice.ForumPostBackController;
import org.example.controllers.backoffice.ForumCommentBackController;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class BackofficeController {
    @FXML
    private VBox postsContainer;
    
    @FXML
    private Button refreshBtn;

    private MainController mainController;
    private ForumPostBackController postBackController = new ForumPostBackController();
    private ForumCommentBackController commentBackController = new ForumCommentBackController();

    @FXML
    public void initialize() {
        loadPosts();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void loadPosts() {
        postsContainer.getChildren().clear();
        List<ForumPost> posts = postBackController.listAllPosts();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (ForumPost post : posts) {
            VBox card = createPostCard(post, formatter);
            postsContainer.getChildren().add(card);
        }
        
        if (postsContainer.getChildren().isEmpty()) {
            Label noPosts = new Label("No posts found.");
            noPosts.getStyleClass().add("no-posts-label");
            postsContainer.getChildren().add(noPosts);
        }
    }

    private VBox createPostCard(ForumPost post, DateTimeFormatter formatter) {
        VBox card = new VBox(10);
        card.getStyleClass().add("post-card");
        
        HBox header = new HBox(10);
        header.getStyleClass().add("post-header");
        
        Label titleLabel = new Label(post.getTitle());
        titleLabel.getStyleClass().add("post-title");
        
        Label typeLabel = new Label(post.getType());
        typeLabel.getStyleClass().add("post-type-" + post.getType().toLowerCase());
        
        Label statusLabel = new Label(post.getStatus());
        statusLabel.getStyleClass().add("post-status");
        
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        header.getChildren().addAll(titleLabel, typeLabel, statusLabel);
        
        HBox footer = new HBox(15);
        footer.getStyleClass().add("post-footer");
        
        Label userLabel = new Label("User: " + post.getUserId());
        Label dateLabel = new Label("Posted: " + post.getCreatedAt().format(formatter));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setOnAction(e -> handleDeletePost(post.getId()));
        
        HBox.setHgrow(userLabel, Priority.ALWAYS);
        footer.getChildren().addAll(userLabel, dateLabel, deleteBtn);
        
        card.getChildren().addAll(header, footer);
        return card;
    }

    private void handleDeletePost(int postId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Post");
        confirm.setContentText("Are you sure you want to delete this post?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (postBackController.deletePost(postId)) {
                    showAlert("Success", "Post deleted successfully!", Alert.AlertType.INFORMATION);
                    loadPosts();
                } else {
                    showAlert("Error", "Failed to delete post!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
