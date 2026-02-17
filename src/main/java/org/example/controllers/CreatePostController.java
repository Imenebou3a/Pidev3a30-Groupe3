package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entities.ForumPost;
import org.example.controllers.frontoffice.ForumPostFrontController;

public class CreatePostController {
    @FXML
    private TextField titleField;
    
    @FXML
    private TextArea contentArea;
    
    @FXML
    private TextField userIdField;
    
    @FXML
    private ComboBox<String> typeCombo;
    
    @FXML
    private Spinner<Integer> ratingSpinner;
    
    @FXML
    private Button submitBtn;
    
    @FXML
    private Button cancelBtn;

    private MainController mainController;
    private ForumPostFrontController postController = new ForumPostFrontController();

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("AVIS", "RECLAMATION", "RECOMMANDATION", "DISCUSSION");
        typeCombo.setValue("DISCUSSION");
        
        SpinnerValueFactory<Integer> ratingFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0);
        ratingSpinner.setValueFactory(ratingFactory);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handleSubmit() {
        try {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            int userId = Integer.parseInt(userIdField.getText().trim());
            String type = typeCombo.getValue();
            Integer rating = ratingSpinner.getValue() > 0 ? ratingSpinner.getValue() : null;
            
            if (title.isEmpty() || content.isEmpty()) {
                showAlert("Error", "Title and content are required!", Alert.AlertType.ERROR);
                return;
            }
            
            ForumPost post = postController.createPost(title, content, userId, type, rating);
            showAlert("Success", "Post created successfully! ID: " + post.getId(), Alert.AlertType.INFORMATION);
            mainController.loadPostList();
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid user ID!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Failed to create post: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleCancel() {
        mainController.loadPostList();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
