package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.entities.ForumPost;
import org.example.controllers.frontoffice.ForumPostFrontController;

public class CreatePostController {

    @FXML private TextField        titleField;
    @FXML private TextArea         contentArea;
    @FXML private TextField        userIdField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Spinner<Integer> ratingSpinner;
    @FXML private Button           submitBtn;
    @FXML private Button           cancelBtn;

    // Inline error labels (inserted programmatically under each field)
    private Label titleError;
    private Label contentError;
    private Label userIdError;

    private static final String ERROR_BORDER =
            "-fx-border-color: #e53e3e; -fx-border-width: 2px; -fx-border-radius: 6px;";
    private static final String ERROR_LABEL_STYLE =
            "-fx-text-fill: #e53e3e; -fx-font-size: 11px; -fx-font-style: italic;";

    private MainController mainController;
    private ForumPostFrontController postController = new ForumPostFrontController();

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("AVIS", "RECLAMATION", "RECOMMANDATION", "DISCUSSION");
        typeCombo.setValue("DISCUSSION");

        SpinnerValueFactory<Integer> ratingFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0);
        ratingSpinner.setValueFactory(ratingFactory);

        // Create and inject inline error labels
        titleError   = makeErrorLabel();
        contentError = makeErrorLabel();
        userIdError  = makeErrorLabel();

        insertAfter(titleField,   titleError);
        insertAfter(contentArea,  contentError);
        insertAfter(userIdField,  userIdError);

        // Auto-clear red state when user starts correcting a field
        titleField.textProperty().addListener((o, ov, nv)   -> clearError(titleField,  titleError));
        contentArea.textProperty().addListener((o, ov, nv)  -> clearError(contentArea, contentError));
        userIdField.textProperty().addListener((o, ov, nv)  -> clearError(userIdField, userIdError));
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handleSubmit() {
        // Reset all fields first
        clearError(titleField,  titleError);
        clearError(contentArea, contentError);
        clearError(userIdField, userIdError);

        boolean valid = true;

        // ── User ID ──────────────────────────────────────────────────────
        int userId = -1;
        String rawId = userIdField.getText().trim();
        if (rawId.isEmpty()) {
            showError(userIdField, userIdError, "User ID is required.");
            valid = false;
        } else {
            try {
                userId = Integer.parseInt(rawId);
            } catch (NumberFormatException e) {
                showError(userIdField, userIdError, "User ID must be a valid number.");
                valid = false;
            }
        }

        // ── Title ────────────────────────────────────────────────────────
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showError(titleField, titleError, "Title is required.");
            valid = false;
        }

        // ── Content ──────────────────────────────────────────────────────
        String content = contentArea.getText().trim();
        if (content.isEmpty()) {
            showError(contentArea, contentError, "Content is required.");
            valid = false;
        }

        if (!valid) return;

        // ── All good → persist ───────────────────────────────────────────
        try {
            String  type   = typeCombo.getValue();
            Integer rating = ratingSpinner.getValue() > 0 ? ratingSpinner.getValue() : null;

            ForumPost post = postController.createPost(title, content, userId, type, rating);
            mainController.loadPostList();          // navigate away on success
        } catch (Exception e) {
            // Truly unexpected error – an alert is acceptable here
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to create post: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleCancel() {
        mainController.loadPostList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showError(Control field, Label errorLabel, String message) {
        field.setStyle(ERROR_BORDER);
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError(Control field, Label errorLabel) {
        field.setStyle("");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private Label makeErrorLabel() {
        Label lbl = new Label();
        lbl.setStyle(ERROR_LABEL_STYLE);
        lbl.setVisible(false);
        lbl.setManaged(false);
        return lbl;
    }

    /** Inserts errorLabel into the parent VBox right after the target field. */
    private void insertAfter(Control field, Label errorLabel) {
        if (field.getParent() instanceof VBox parent) {
            int idx = parent.getChildren().indexOf(field);
            if (idx >= 0 && !parent.getChildren().contains(errorLabel)) {
                parent.getChildren().add(idx + 1, errorLabel);
            }
        }
    }
}