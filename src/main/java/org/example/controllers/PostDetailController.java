package org.example.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entities.ForumPost;
import org.example.entities.ForumComment;
import org.example.controllers.frontoffice.ForumPostFrontController;
import org.example.controllers.frontoffice.ForumCommentFrontController;
import org.example.controllers.frontoffice.ForumReactionFrontController;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostDetailController {
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label typeLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label contentLabel;
    
    @FXML
    private Label userLabel;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private Label viewsLabel;
    
    @FXML
    private Label ratingLabel;
    
    @FXML
    private Button viewCommentsBtn;
    
    @FXML
    private Button backBtn;
    
    @FXML
    private Button editBtn;
    
    @FXML
    private Button deleteBtn;

    private int postId = -1;
    private boolean postIdSet = false;
    private MainController mainController;
    private ForumPostFrontController postController = new ForumPostFrontController();
    private ForumCommentFrontController commentController = new ForumCommentFrontController();
    private ForumReactionFrontController reactionController = new ForumReactionFrontController();

    @FXML
    public void initialize() {
        // Will be called before setPostId, so we wait
    }

    public void setPostId(int postId) {
        this.postId = postId;
        this.postIdSet = true;
        loadPost();
        updateCommentsButtonLabel();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void loadPost() {
        if (!postIdSet || postId < 0) {
            return;
        }
        postController.getPost(postId).ifPresentOrElse(
            post -> {
                titleLabel.setText(post.getTitle());
                typeLabel.setText(post.getType());
                typeLabel.getStyleClass().clear();
                typeLabel.getStyleClass().add("post-type-" + post.getType().toLowerCase());
                statusLabel.setText(post.getStatus());
                contentLabel.setText(post.getContent());
                userLabel.setText("User ID: " + post.getUserId());
                dateLabel.setText("Posted: " + post.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                
                // Increment view count
                incrementViewCount(post);
                
                viewsLabel.setText("Views: " + post.getViewCount());
                if (post.getRating() != null && post.getRating() > 0) {
                    ratingLabel.setText("Rating: ★ " + post.getRating());
                    ratingLabel.setVisible(true);
                } else {
                    ratingLabel.setVisible(false);
                }
            },
            () -> {
                showAlert("Error", "Post not found!", Alert.AlertType.ERROR);
                mainController.loadPostList();
            }
        );
    }

    private void incrementViewCount(ForumPost post) {
        if (post.getViewCount() == null) {
            post.setViewCount(0);
        }
        post.setViewCount(post.getViewCount() + 1);
        postController.updatePost(post);
    }
    
    private void updateCommentsButtonLabel() {
        if (!postIdSet || postId < 0) {
            return;
        }
        List<ForumComment> comments = commentController.getCommentsForPost(postId);
        int count = comments.size();
        viewCommentsBtn.setText("💬 View & Add Comments (" + count + ")");
    }
    
    @FXML
    public void handleViewComments() {
        if (!postIdSet || postId < 0) {
            showAlert("Error", "Post not loaded!", Alert.AlertType.ERROR);
            return;
        }
        
        // Create modal dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Comments - Community Discussion");
        
        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setStyle("-fx-background-color: #fdfbf7; -fx-padding: 30px;");
        
        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 20px 25px; -fx-background-color: white; -fx-background-radius: 18px; " +
                       "-fx-effect: dropshadow(gaussian, #1B7B7E1F, 10, 0, 0, 4); -fx-border-color: #F5E6D3; " +
                       "-fx-border-width: 2px; -fx-border-radius: 18px;");
        
        Label headerIcon = new Label("💬");
        headerIcon.setStyle("-fx-font-size: 32px;");
        
        VBox headerText = new VBox(5);
        Label headerTitle = new Label("Community Comments");
        headerTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #1B7B7E;");
        Label headerSubtitle = new Label("Join the conversation about this hidden gem");
        headerSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568;");
        headerText.getChildren().addAll(headerTitle, headerSubtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕ Close");
        closeBtn.setStyle("-fx-background-color: #F5E6D3; -fx-text-fill: #1B7B7E; -fx-padding: 10px 24px; " +
                         "-fx-background-radius: 25px; -fx-cursor: hand; -fx-font-weight: 600; " +
                         "-fx-border-color: #D2691E; -fx-border-width: 2px; -fx-border-radius: 25px;");
        closeBtn.setOnAction(e -> dialog.close());
        
        header.getChildren().addAll(headerIcon, headerText, spacer, closeBtn);
        
        // Comments list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setPrefHeight(400);
        
        VBox commentsContainer = new VBox(15);
        commentsContainer.setStyle("-fx-padding: 15px; -fx-spacing: 15;");
        loadCommentsIntoContainer(commentsContainer);
        
        scrollPane.setContent(commentsContainer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Add comment section
        VBox addCommentSection = new VBox(15);
        addCommentSection.setStyle("-fx-padding: 25px; -fx-background-color: white; -fx-background-radius: 18px; " +
                                  "-fx-effect: dropshadow(gaussian, #1B7B7E1F, 10, 0, 0, 4); -fx-border-color: #F5E6D3; " +
                                  "-fx-border-width: 2px; -fx-border-radius: 18px;");
        
        HBox addCommentHeader = new HBox(10);
        addCommentHeader.setAlignment(Pos.CENTER_LEFT);
        Label addIcon = new Label("✍");
        addIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #D2691E;");
        Label addTitle = new Label("Share Your Thoughts");
        addTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1B7B7E;");
        addCommentHeader.getChildren().addAll(addIcon, addTitle);
        
        HBox userIdBox = new HBox(10);
        Label userIdLabel = new Label("Your ID:");
        userIdLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #1B7B7E; -fx-font-size: 13px;");
        TextField authorIdField = new TextField();
        authorIdField.setPromptText("Enter your user ID");
        authorIdField.setPrefWidth(150);
        authorIdField.setStyle("-fx-background-color: #fdfbf7; -fx-border-color: #F5E6D3; -fx-border-radius: 12px; " +
                              "-fx-background-radius: 12px; -fx-padding: 12px 15px; -fx-font-size: 14px; -fx-border-width: 2px;");
        userIdBox.getChildren().addAll(userIdLabel, authorIdField);
        
        Label commentLabel = new Label("Comment:");
        commentLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #1B7B7E; -fx-font-size: 13px;");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Share your experience, tips, or thoughts about this post...");
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-background-color: #fdfbf7; -fx-border-color: #F5E6D3; -fx-border-radius: 12px; " +
                            "-fx-background-radius: 12px; -fx-padding: 15px; -fx-font-size: 14px; -fx-border-width: 2px;");
        
        Button submitBtn = new Button("📤 Post Comment");
        submitBtn.setStyle("-fx-background-color: linear-gradient(to bottom right, #1B7B7E, #2a9a9d); " +
                          "-fx-text-fill: white; -fx-padding: 14px 38px; -fx-background-radius: 30px; " +
                          "-fx-cursor: hand; -fx-font-weight: 700; -fx-font-size: 15px; " +
                          "-fx-effect: dropshadow(gaussian, #1B7B7E66, 10, 0, 0, 4);");
        submitBtn.setOnAction(e -> handleSubmitCommentInModal(authorIdField, commentArea, commentsContainer, dialog));
        
        HBox submitBox = new HBox();
        submitBox.setAlignment(Pos.CENTER_RIGHT);
        submitBox.getChildren().add(submitBtn);
        
        addCommentSection.getChildren().addAll(addCommentHeader, userIdBox, commentLabel, commentArea, submitBox);
        
        mainContainer.getChildren().addAll(header, scrollPane, addCommentSection);
        
        Scene scene = new Scene(mainContainer, 800, 700);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load stylesheet: " + e.getMessage());
        }
        
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void loadCommentsIntoContainer(VBox commentsContainer) {
        if (!postIdSet || postId < 0) {
            return;
        }
        commentsContainer.getChildren().clear();
        List<ForumComment> comments = commentController.getCommentsForPost(postId);
        
        if (comments.isEmpty()) {
            Label emptyLabel = new Label("🌴 No comments yet! Be the first to share your thoughts.");
            emptyLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 15px; -fx-padding: 40px; " +
                               "-fx-background-color: white; -fx-background-radius: 15px; -fx-alignment: center;");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            commentsContainer.getChildren().add(emptyLabel);
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (ForumComment comment : comments) {
            VBox commentCard = createCommentCard(comment, formatter, commentsContainer);
            commentsContainer.getChildren().add(commentCard);
        }
    }

    private VBox createCommentCard(ForumComment comment, DateTimeFormatter formatter, VBox commentsContainer) {
        VBox card = new VBox(8);
        card.getStyleClass().add("comment-card");
        
        String content = comment.getContent() != null ? comment.getContent() : "";
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("comment-content");
        contentLabel.setWrapText(true);
        
        HBox footer = new HBox(10);
        footer.getStyleClass().add("comment-footer");
        Integer authorId = comment.getAuthorId() != null ? comment.getAuthorId() : 0;
        Label authorLabel = new Label("User " + authorId);
        
        String dateStr = "N/A";
        if (comment.getCreatedAt() != null) {
            dateStr = comment.getCreatedAt().format(formatter);
        }
        Label dateLabel = new Label(dateStr);
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-delete-small");
        deleteBtn.setOnAction(e -> handleDeleteComment(comment.getId(), commentsContainer));
        
        HBox.setHgrow(authorLabel, Priority.ALWAYS);
        footer.getChildren().addAll(authorLabel, dateLabel, deleteBtn);
        
        card.getChildren().addAll(contentLabel, footer);
        return card;
    }

    private void handleDeleteComment(Integer commentId, VBox commentsContainer) {
        if (commentId == null) {
            showAlert("Error", "Invalid comment!", Alert.AlertType.ERROR);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Comment");
        confirm.setContentText("Are you sure you want to delete this comment?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                org.example.controllers.backoffice.ForumCommentBackController backController = 
                    new org.example.controllers.backoffice.ForumCommentBackController();
                if (backController.deleteComment(commentId)) {
                    showAlert("Success", "Comment deleted successfully!", Alert.AlertType.INFORMATION);
                    if (commentsContainer != null) {
                        loadCommentsIntoContainer(commentsContainer);
                    }
                    updateCommentsButtonLabel();
                } else {
                    showAlert("Error", "Failed to delete comment!", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    private void handleSubmitCommentInModal(TextField authorIdField, TextArea commentArea, 
                                            VBox commentsContainer, Stage dialog) {
        if (!postIdSet || postId < 0) {
            showAlert("Error", "Post not loaded!", Alert.AlertType.ERROR);
            return;
        }
        try {
            String content = commentArea.getText().trim();
            String authorIdText = authorIdField.getText().trim();
            
            if (content.isEmpty()) {
                showAlert("Error", "Comment cannot be empty!", Alert.AlertType.ERROR);
                return;
            }
            
            if (authorIdText.isEmpty()) {
                showAlert("Error", "Please enter your user ID!", Alert.AlertType.ERROR);
                return;
            }
            
            int authorId = Integer.parseInt(authorIdText);
            
            commentController.addComment(postId, authorId, content);
            commentArea.clear();
            authorIdField.clear();
            loadCommentsIntoContainer(commentsContainer);
            updateCommentsButtonLabel();
            showAlert("Success", "Comment added successfully!", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid author ID! Please enter a number.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Failed to add comment: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleBack() {
        mainController.loadPostList();
    }

    @FXML
    public void handleEdit() {
        if (!postIdSet || postId < 0) {
            showAlert("Error", "Post not loaded!", Alert.AlertType.ERROR);
            return;
        }
        postController.getPost(postId).ifPresentOrElse(
            post -> {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/EditPostView.fxml"));
                    javafx.scene.Parent editView = loader.load();
                    EditPostController controller = loader.getController();
                    controller.setPost(post);
                    controller.setMainController(mainController);
                    mainController.getMainContainer().setCenter(editView);
                } catch (Exception e) {
                    showAlert("Error", "Failed to load edit view: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            },
            () -> showAlert("Error", "Post not found!", Alert.AlertType.ERROR)
        );
    }

    @FXML
    public void handleDelete() {
        if (!postIdSet || postId < 0) {
            showAlert("Error", "Post not loaded!", Alert.AlertType.ERROR);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Post");
        confirm.setContentText("Are you sure you want to delete this post? This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                org.example.controllers.backoffice.ForumPostBackController backController = 
                    new org.example.controllers.backoffice.ForumPostBackController();
                if (backController.deletePost(postId)) {
                    showAlert("Success", "Post deleted successfully!", Alert.AlertType.INFORMATION);
                    mainController.loadPostList();
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
