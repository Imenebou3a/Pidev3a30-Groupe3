package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.MainApp;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.ValidationUtils;

import java.sql.SQLException;

public class AddUserController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private ComboBox<String> statutCombo;

    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label messageLabel;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();

    @FXML
    private void initialize() {
        // Configurer les ComboBox
        roleCombo.setItems(FXCollections.observableArrayList("USER", "HOTE", "ADMIN"));
        roleCombo.setValue("USER");

        statutCombo.setItems(FXCollections.observableArrayList("ACTIF", "EN_ATTENTE", "DESACTIVE"));
        statutCombo.setValue("ACTIF");

        // Validation en temps réel
        setupValidation();
    }

    private void setupValidation() {
        nomField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(nomError, "Le nom est requis");
            } else {
                hideFieldError(nomError);
            }
        });

        prenomField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(prenomError, "Le prénom est requis");
            } else {
                hideFieldError(prenomError);
            }
        });

        emailField.textProperty().addListener((obs, old, newVal) -> {
            if (!ValidationUtils.isValidEmail(newVal)) {
                showFieldError(emailError, "Email invalide");
            } else {
                hideFieldError(emailError);
            }
        });

        telephoneField.textProperty().addListener((obs, old, newVal) -> {
            if (!ValidationUtils.isValidPhone(newVal)) {
                showFieldError(telephoneError, "Téléphone invalide (8 chiffres)");
            } else {
                hideFieldError(telephoneError);
            }
        });

        passwordField.textProperty().addListener((obs, old, newVal) -> {
            if (!ValidationUtils.isValidPassword(newVal)) {
                showFieldError(passwordError, "Min 8 caractères requis");
            } else {
                hideFieldError(passwordError);
            }
        });

        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.equals(passwordField.getText())) {
                showFieldError(confirmPasswordError, "Les mots de passe ne correspondent pas");
            } else {
                hideFieldError(confirmPasswordError);
            }
        });
    }

    @FXML
    private void handleCreate() {
        // Valider tous les champs
        if (!validateAll()) {
            showMessage("Veuillez corriger les erreurs", true);
            return;
        }

        try {
            // Créer l'utilisateur
            Utilisateur newUser = new Utilisateur(
                    nomField.getText().trim(),
                    prenomField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText(),
                    telephoneField.getText().trim()
            );
            newUser.setRole(roleCombo.getValue());

            if (serviceUtilisateur.inscrire(newUser)) {
                // Appliquer le statut choisi
                if ("ACTIF".equals(statutCombo.getValue())) {
                    serviceUtilisateur.activerCompte(newUser.getId());
                } else if ("DESACTIVE".equals(statutCombo.getValue())) {
                    serviceUtilisateur.desactiverCompte(newUser.getId());
                }

                showSuccess("Utilisateur créé avec succès !");

                // Retour au dashboard après 1 seconde
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        javafx.application.Platform.runLater(this::handleBack);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            } else {
                showMessage("Cet email est déjà utilisé !", true);
            }

        } catch (SQLException e) {
            showMessage("Erreur lors de la création : " + e.getMessage(), true);
        }
    }

    private boolean validateAll() {
        boolean valid = true;

        if (nomField.getText().trim().isEmpty()) {
            showFieldError(nomError, "Le nom est requis");
            valid = false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            showFieldError(prenomError, "Le prénom est requis");
            valid = false;
        }

        if (!ValidationUtils.isValidEmail(emailField.getText())) {
            showFieldError(emailError, "Email invalide");
            valid = false;
        }

        if (!ValidationUtils.isValidPhone(telephoneField.getText())) {
            showFieldError(telephoneError, "Téléphone invalide (8 chiffres)");
            valid = false;
        }

        if (!ValidationUtils.isValidPassword(passwordField.getText())) {
            showFieldError(passwordError, "Min 8 caractères requis");
            valid = false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showFieldError(confirmPasswordError, "Les mots de passe ne correspondent pas");
            valid = false;
        }

        return valid;
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("/fxml/Dashboard.fxml", "Lammetna - Dashboard");
    }

    private void showFieldError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideFieldError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().clear();
        messageLabel.getStyleClass().add(isError ? "error-label" : "success-label");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}