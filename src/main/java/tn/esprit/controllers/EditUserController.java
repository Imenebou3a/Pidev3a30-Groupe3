package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.MainApp;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.ValidationUtils;

import java.sql.SQLException;

public class EditUserController {

    @FXML private Label userNameLabel;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private TextArea bioField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private ComboBox<String> statutCombo;

    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label messageLabel;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private static Utilisateur currentUser;

    public static void setUser(Utilisateur user) {
        currentUser = user;
    }

    @FXML
    private void initialize() {
        // Configurer les ComboBox
        roleCombo.setItems(FXCollections.observableArrayList("USER", "HOTE", "ADMIN"));
        statutCombo.setItems(FXCollections.observableArrayList("ACTIF", "EN_ATTENTE", "DESACTIVE"));

        // Charger les données de l'utilisateur
        if (currentUser != null) {
            loadUserData();
        }

        // Validation en temps réel
        setupValidation();
    }

    private void loadUserData() {
        userNameLabel.setText(currentUser.getNomComplet());
        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone());
        adresseField.setText(currentUser.getAdresse() != null ? currentUser.getAdresse() : "");
        bioField.setText(currentUser.getBio() != null ? currentUser.getBio() : "");
        roleCombo.setValue(currentUser.getRole());
        statutCombo.setValue(currentUser.getStatut());
    }

    private void setupValidation() {
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
    }

    @FXML
    private void handleSave() {
        if (!validateAll()) {
            showMessage("Veuillez corriger les erreurs", true);
            return;
        }

        try {
            // Mettre à jour l'utilisateur
            currentUser.setNom(nomField.getText().trim());
            currentUser.setPrenom(prenomField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setTelephone(telephoneField.getText().trim());
            currentUser.setAdresse(adresseField.getText().trim());
            currentUser.setBio(bioField.getText().trim());
            currentUser.setRole(roleCombo.getValue());
            currentUser.setStatut(statutCombo.getValue());

            // Sauvegarder en base
            serviceUtilisateur.modifier(currentUser);

            // Appliquer le statut
            if ("ACTIF".equals(currentUser.getStatut())) {
                serviceUtilisateur.activerCompte(currentUser.getId());
            } else if ("DESACTIVE".equals(currentUser.getStatut())) {
                serviceUtilisateur.desactiverCompte(currentUser.getId());
            }

            // Appliquer le rôle
            serviceUtilisateur.changerRole(currentUser.getId(), currentUser.getRole());

            showSuccess("Utilisateur modifié avec succès !");

            // Retour au dashboard après 1 seconde
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(this::handleBack);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            showMessage("Erreur lors de la modification : " + e.getMessage(), true);
        }
    }

    private boolean validateAll() {
        boolean valid = true;

        if (!ValidationUtils.isValidEmail(emailField.getText())) {
            showFieldError(emailError, "Email invalide");
            valid = false;
        }

        if (!ValidationUtils.isValidPhone(telephoneField.getText())) {
            showFieldError(telephoneError, "Téléphone invalide (8 chiffres)");
            valid = false;
        }

        return valid;
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("/fxml/frontoffice/Dashboard.fxml", "Lammetna - Dashboard");
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