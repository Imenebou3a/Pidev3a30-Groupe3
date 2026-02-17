package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entities.ForumPost;
import org.example.controllers.frontoffice.ForumPostFrontController;

public class EditPostController {
    @FXML
    private TextField titleField;
    
    @FXML
    private TextArea contentArea;
    
    @FXML
    private ComboBox<String> typeCombo;
    
    @FXML
    private ComboBox<String> statusCombo;
    
    @FXML
    private Spinner<Integer> ratingSpinner;
    
    @FXML
    private Button saveBtn;
    
    @FXML
    private Button cancelBtn;

    private MainController mainController;
    private ForumPostFrontController postController = new ForumPostFrontController();
    private ForumPost currentPost;

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("AVIS", "RECLAMATION", "RECOMMANDATION", "DISCUSSION");
        statusCombo.getItems().addAll("EN_ATTENTE", "ACTIVE", "CLOSED", "ARCHIVED");
        
        SpinnerValueFactory<Integer> ratingFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0);
        ratingSpinner.setValueFactory(ratingFactory);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setPost(ForumPost post) {
        this.currentPost = post;
        if (post != null) {
            titleField.setText(post.getTitle());
            contentArea.setText(post.getContent());
            typeCombo.setValue(post.getType());
            statusCombo.setValue(post.getStatus());
            if (post.getRating() != null) {
                ratingSpinner.getValueFactory().setValue(post.getRating());
            }
        }
    }

    @FXML
    public void handleSave() {
        try {
            if (currentPost == null || currentPost.getId() == null) {
                showAlert("Error", "No post selected for editing!", Alert.AlertType.ERROR);
                return;
            }

            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String type = typeCombo.getValue();
            String status = statusCombo.getValue();
            Integer rating = ratingSpinner.getValue() > 0 ? ratingSpinner.getValue() : null;
            
            if (title.isEmpty() || content.isEmpty()) {
                showAlert("Error", "Title and content are required!", Alert.AlertType.ERROR);
                return;
            }
            
            currentPost.setTitle(title);
            currentPost.setContent(content);
            currentPost.setType(type);
            currentPost.setStatus(status);
            currentPost.setRating(rating);
            
            if (postController.updatePost(currentPost)) {
                showAlert("Success", "Post updated successfully!", Alert.AlertType.INFORMATION);
                mainController.loadPostDetail(currentPost.getId());
            } else {
                showAlert("Error", "Failed to update post!", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to update post: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleCancel() {
        if (currentPost != null && currentPost.getId() != null) {
            mainController.loadPostDetail(currentPost.getId());
        } else {
            mainController.loadPostList();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
