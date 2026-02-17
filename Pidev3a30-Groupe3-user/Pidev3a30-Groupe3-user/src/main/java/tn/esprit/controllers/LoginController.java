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
        emailField.textProperty().addListener((obs, ov, nv) -> {
            if (!nv.isEmpty() && !ValidationUtils.isValidEmail(nv)) {
                emailError.setText("Email invalide");
                emailError.setVisible(true); emailError.setManaged(true);
                emailField.getStyleClass().add("error");
            } else {
                emailError.setVisible(false); emailError.setManaged(false);
                emailField.getStyleClass().remove("error");
            }
        });

        passwordField.textProperty().addListener((obs, ov, nv) -> {
            if (!nv.isEmpty() && nv.length() < 8) {
                passwordError.setText("Minimum 8 caracteres");
                passwordError.setVisible(true); passwordError.setManaged(true);
                passwordField.getStyleClass().add("error");
            } else {
                passwordError.setVisible(false); passwordError.setManaged(false);
                passwordField.getStyleClass().remove("error");
            }
        });
    }

    @FXML
    private void handleLogin() {
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);

        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Veuillez entrer une adresse email valide"); return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            showError("Le mot de passe doit contenir au moins 8 caracteres"); return;
        }

        try {
            Utilisateur user = serviceUtilisateur.login(email, password);

            if (user != null) {
                Session.getInstance().setUtilisateurConnecte(user);

                // ─── REDIRECTION SELON LE RÔLE ───
                if ("ADMIN".equals(user.getRole())) {
                    // Admin → Backoffice (produits, kits, dashboard)
                    MainApp.changeScene("/fxml/backoffice/main_back.fxml",
                            "Lammetna - Backoffice Admin");
                } else {
                    // User / Hote → Frontoffice Main
                    MainApp.changeScene("/fxml/frontoffice/main_front.fxml",
                            "Lammetna - Accueil");
                }

            } else {
                showError("Email ou mot de passe incorrect");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Compte desactive")) {
                showError("Votre compte a ete desactive.");
            } else if (e.getMessage().contains("en attente")) {
                showError("Compte en attente d'activation.");
            } else {
                showError("Erreur : " + e.getMessage());
            }
        }
    }

    @FXML private void handleGoToRegister() {
        MainApp.changeScene("/fxml/frontoffice/Register.fxml", "Lammetna - Inscription");
    }

    @FXML private void handleForgotPassword() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Mot de passe oublie");
        a.setHeaderText(null);
        a.setContentText("Contactez : admin@lammetna.tn");
        a.showAndWait();
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
    }
}