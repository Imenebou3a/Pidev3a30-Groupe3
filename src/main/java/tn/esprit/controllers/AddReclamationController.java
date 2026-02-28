package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.MainApp;
import tn.esprit.entities.Reclamation;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceReclamation;
import tn.esprit.utils.Session;

import java.sql.SQLException;

public class AddReclamationController {

    @FXML private TextField sujetField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private ComboBox<String> prioriteCombo;
    @FXML private TextArea descriptionArea;

    @FXML private Label sujetError;
    @FXML private Label categorieError;
    @FXML private Label descriptionError;
    @FXML private Label messageLabel;

    private ServiceReclamation serviceReclamation = new ServiceReclamation();
    private Utilisateur currentUser;

    @FXML
    private void initialize() {
        currentUser = Session.getInstance().getUtilisateurConnecte();

        // Configurer les ComboBox
        categorieCombo.setItems(FXCollections.observableArrayList("TECHNIQUE", "SERVICE", "HEBERGEMENT", "EVENEMENT", "AUTRE"));
        categorieCombo.setValue("AUTRE");

        prioriteCombo.setItems(FXCollections.observableArrayList("BASSE", "MOYENNE", "HAUTE"));
        prioriteCombo.setValue("MOYENNE");

        // Validation en temps réel
        setupValidation();
    }

    private void setupValidation() {
        sujetField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(sujetError, "Le sujet est requis");
            } else if (newVal.length() < 5) {
                showFieldError(sujetError, "Le sujet doit contenir au moins 5 caractères");
            } else {
                hideFieldError(sujetError);
            }
        });

        descriptionArea.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(descriptionError, "La description est requise");
            } else if (newVal.length() < 20) {
                showFieldError(descriptionError, "La description doit contenir au moins 20 caractères");
            } else {
                hideFieldError(descriptionError);
            }
        });
    }

    @FXML
    private void handleSubmit() {
        if (!validateAll()) {
            showMessage("Veuillez corriger les erreurs", true);
            return;
        }

        try {
            // Créer la réclamation
            Reclamation reclamation = new Reclamation(
                    currentUser.getId(),
                    sujetField.getText().trim(),
                    descriptionArea.getText().trim(),
                    categorieCombo.getValue()
            );
            reclamation.setPriorite(prioriteCombo.getValue());

            serviceReclamation.ajouter(reclamation);

            showSuccess("Réclamation envoyée avec succès !");

            // Retour à la liste après 1 seconde
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(this::handleBack);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            showMessage("Erreur lors de l'envoi : " + e.getMessage(), true);
        }
    }

    private boolean validateAll() {
        boolean valid = true;

        if (sujetField.getText().trim().isEmpty()) {
            showFieldError(sujetError, "Le sujet est requis");
            valid = false;
        } else if (sujetField.getText().length() < 5) {
            showFieldError(sujetError, "Le sujet doit contenir au moins 5 caractères");
            valid = false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            showFieldError(descriptionError, "La description est requise");
            valid = false;
        } else if (descriptionArea.getText().length() < 20) {
            showFieldError(descriptionError, "La description doit contenir au moins 20 caractères");
            valid = false;
        }

        if (categorieCombo.getValue() == null) {
            showFieldError(categorieError, "La catégorie est requise");
            valid = false;
        }

        return valid;
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("/fxml/frontoffice/MesReclamations.fxml", "Lammetna - Mes Réclamations");
    }
    @FXML
    private void handleRetourAccueil() {
        MainApp.changeScene("/fxml/frontoffice/main_front.fxml", "Lammetna - Accueil");
    }

    @FXML
    private void handleProfil() {
        MainApp.changeScene("/fxml/frontoffice/Profil.fxml", "Lammetna - Mon Profil");
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().deconnecter();
        MainApp.changeScene("/fxml/frontoffice/login.fxml", "Lammetna - Connexion");
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