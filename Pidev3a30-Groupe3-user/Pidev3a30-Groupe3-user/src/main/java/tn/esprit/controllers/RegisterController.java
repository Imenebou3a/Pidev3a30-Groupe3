package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.MainApp;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.ValidationUtils;

import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckbox;

    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label passwordError;
    @FXML private Label passwordStrengthLabel;
    @FXML private Label confirmPasswordError;
    @FXML private Label termsError;
    @FXML private Label messageLabel;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();

    @FXML
    private void initialize() {
        setupValidation();
    }

    private void setupValidation() {
        // Validation Nom
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.length() < 2) {
                showFieldError(nomError, nomField, "Le nom doit contenir au moins 2 caractères");
            } else {
                hideFieldError(nomError, nomField);
            }
        });

        // Validation Prénom
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.length() < 2) {
                showFieldError(prenomError, prenomField, "Le prénom doit contenir au moins 2 caractères");
            } else {
                hideFieldError(prenomError, prenomField);
            }
        });

        // Validation Email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !ValidationUtils.isValidEmail(newValue)) {
                showFieldError(emailError, emailField, "Format d'email invalide");
            } else {
                hideFieldError(emailError, emailField);
            }
        });

        // Validation Téléphone
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Limiter à 8 chiffres
            if (newValue.length() > 8) {
                telephoneField.setText(oldValue);
                return;
            }
            // Autoriser uniquement les chiffres
            if (!newValue.matches("\\d*")) {
                telephoneField.setText(oldValue);
                return;
            }

            if (!newValue.isEmpty() && !ValidationUtils.isValidPhone(newValue)) {
                showFieldError(telephoneError, telephoneField, "Le numéro doit contenir 8 chiffres");
            } else {
                hideFieldError(telephoneError, telephoneField);
            }
        });

        // Validation Mot de passe avec indicateur de force
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                passwordStrengthLabel.setText("—");
                passwordStrengthLabel.setStyle("");
                hideFieldError(passwordError, passwordField);
            } else {
                String strength = ValidationUtils.getPasswordStrength(newValue);
                passwordStrengthLabel.setText(strength);

                switch (strength) {
                    case "Trop faible":
                        passwordStrengthLabel.setStyle("-fx-text-fill: #ef4444;");
                        showFieldError(passwordError, passwordField, "Mot de passe trop faible (min. 8 caractères)");
                        break;
                    case "Faible":
                        passwordStrengthLabel.setStyle("-fx-text-fill: #f59e0b;");
                        hideFieldError(passwordError, passwordField);
                        break;
                    case "Moyen":
                        passwordStrengthLabel.setStyle("-fx-text-fill: #3b82f6;");
                        hideFieldError(passwordError, passwordField);
                        break;
                    case "Fort":
                        passwordStrengthLabel.setStyle("-fx-text-fill: #10b981;");
                        hideFieldError(passwordError, passwordField);
                        break;
                }
            }

            // Vérifier aussi la confirmation
            checkPasswordMatch();
        });

        // Validation Confirmation mot de passe
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkPasswordMatch();
        });
    }

    private void checkPasswordMatch() {
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!confirm.isEmpty() && !password.equals(confirm)) {
            showFieldError(confirmPasswordError, confirmPasswordField, "Les mots de passe ne correspondent pas");
        } else {
            hideFieldError(confirmPasswordError, confirmPasswordField);
        }
    }

    @FXML
    private void handleRegister() {
        // Réinitialiser les messages
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        // Récupérer les valeurs
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String adresse = adresseField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation complète
        boolean isValid = true;

        if (!ValidationUtils.isNotEmpty(nom) || nom.length() < 2) {
            showFieldError(nomError, nomField, "Le nom est requis (min. 2 caractères)");
            isValid = false;
        }

        if (!ValidationUtils.isNotEmpty(prenom) || prenom.length() < 2) {
            showFieldError(prenomError, prenomField, "Le prénom est requis (min. 2 caractères)");
            isValid = false;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showFieldError(emailError, emailField, "Email invalide");
            isValid = false;
        }

        if (!ValidationUtils.isValidPhone(telephone)) {
            showFieldError(telephoneError, telephoneField, "Numéro de téléphone invalide (8 chiffres)");
            isValid = false;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            showFieldError(passwordError, passwordField, "Le mot de passe doit contenir au moins 8 caractères");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            showFieldError(confirmPasswordError, confirmPasswordField, "Les mots de passe ne correspondent pas");
            isValid = false;
        }

        if (!termsCheckbox.isSelected()) {
            showFieldError(termsError, null, "Vous devez accepter les conditions d'utilisation");
            isValid = false;
        } else {
            hideFieldError(termsError, null);
        }

        if (!isValid) {
            showMessage("Veuillez corriger les erreurs ci-dessus", true);
            return;
        }

        // Tentative d'inscription
        try {
            Utilisateur nouvelUtilisateur = new Utilisateur(nom, prenom, email, password, telephone);
            nouvelUtilisateur.setAdresse(adresse.isEmpty() ? null : adresse);

            if (serviceUtilisateur.inscrire(nouvelUtilisateur)) {
                // Succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Inscription réussie");
                alert.setHeaderText("Bienvenue " + prenom + " !");
                alert.setContentText("Votre compte a été créé avec succès.\nIl sera activé par un administrateur sous peu.");
                alert.showAndWait();

                // Rediriger vers la page de connexion
                MainApp.changeScene("/fxml/frontoffice/login.fxml", "Lammetna - Connexion");
            } else {
                showMessage("Cette adresse email est déjà utilisée", true);
            }

        } catch (SQLException e) {
            showMessage("Erreur lors de l'inscription : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleGoToLogin() {
        MainApp.changeScene("/fxml/frontoffice/login.fxml", "Lammetna - Connexion");
    }

    private void showFieldError(Label errorLabel, Control field, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
        if (field != null) {
            field.getStyleClass().add("error");
        }
    }

    private void hideFieldError(Label errorLabel, Control field) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        if (field != null) {
            field.getStyleClass().remove("error");
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        // Corriger les styles
        messageLabel.getStyleClass().clear();
        if (isError) {
            messageLabel.getStyleClass().add("error-label");
        } else {
            messageLabel.getStyleClass().add("success-label");
        }
    }
}