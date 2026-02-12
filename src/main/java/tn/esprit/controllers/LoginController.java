package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.MainApp;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.Session;
import tn.esprit.utils.ValidationUtils;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label errorMessage;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();

    @FXML
    private void initialize() {
        // Validation en temps réel de l'email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !ValidationUtils.isValidEmail(newValue)) {
                emailError.setText("Email invalide");
                emailError.setVisible(true);
                emailError.setManaged(true);
                emailField.getStyleClass().add("error");
            } else {
                emailError.setVisible(false);
                emailError.setManaged(false);
                emailField.getStyleClass().remove("error");
            }
        });

        // Validation en temps réel du mot de passe
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.length() < 8) {
                passwordError.setText("Le mot de passe doit contenir au moins 8 caractères");
                passwordError.setVisible(true);
                passwordError.setManaged(true);
                passwordField.getStyleClass().add("error");
            } else {
                passwordError.setVisible(false);
                passwordError.setManaged(false);
                passwordField.getStyleClass().remove("error");
            }
        });
    }

    @FXML
    private void handleLogin() {
        // Réinitialiser les erreurs
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (!ValidationUtils.isValidEmail(email)) {
            showError("Veuillez entrer une adresse email valide");
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            showError("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        // Tentative de connexion
        try {
            Utilisateur user = serviceUtilisateur.login(email, password);

            if (user != null) {
                // Enregistrer la session
                Session.getInstance().setUtilisateurConnecte(user);

                // Afficher un message de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Connexion réussie");
                alert.setHeaderText(null);
                alert.setContentText("Bienvenue " + user.getNomComplet() + " !");
                alert.showAndWait();

                // Rediriger selon le rôle
                if (user.isAdmin()) {
                    MainApp.changeScene("/fxml/Dashboard.fxml", "Lammetna - Dashboard Admin");
                } else {
                    MainApp.changeScene("/fxml/Profil.fxml", "Lammetna - Mon Profil");
                }
            } else {
                showError("Email ou mot de passe incorrect");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Compte désactivé")) {
                showError("Votre compte a été désactivé. Contactez l'administrateur.");
            } else if (e.getMessage().contains("en attente d'activation")) {
                showError("Votre compte est en attente d'activation par un administrateur.");
            } else {
                showError("Erreur de connexion : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGoToRegister() {
        MainApp.changeScene("/fxml/Register.fxml", "Lammetna - Inscription");
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe oublié");
        alert.setHeaderText(null);
        alert.setContentText("Contactez l'administrateur à : admin@lammetna.tn");
        alert.showAndWait();
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
    }
}