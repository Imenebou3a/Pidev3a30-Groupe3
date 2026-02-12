package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.MainApp;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.Session;
import tn.esprit.utils.ValidationUtils;

import java.sql.SQLException;

public class ProfilController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private TextArea bioField;
    @FXML private Label roleLabel;
    @FXML private Label statutLabel;

    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label messageLabel;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Label passwordStrengthLabel;
    @FXML private Label currentPasswordError;
    @FXML private Label newPasswordError;
    @FXML private Label confirmNewPasswordError;
    @FXML private Label passwordMessageLabel;

    @FXML private Button backButton;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private Utilisateur currentUser;

    @FXML
    private void initialize() {
        currentUser = Session.getInstance().getUtilisateurConnecte();

        if (currentUser == null) {
            // Rediriger vers login si pas connecté
            MainApp.changeScene("/fxml/Login.fxml", "Lammetna - Connexion");
            return;
        }

        // Afficher le bouton retour seulement pour les admins
        if (currentUser.isAdmin()) {
            backButton.setVisible(true);
        }

        loadUserData();
        setupValidation();
    }

    private void loadUserData() {
        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone());
        adresseField.setText(currentUser.getAdresse() != null ? currentUser.getAdresse() : "");
        bioField.setText(currentUser.getBio() != null ? currentUser.getBio() : "");

        roleLabel.setText(currentUser.getRole());
        statutLabel.setText(currentUser.getStatut());

        // Style des badges selon le rôle/statut
        updateBadgeStyle();
    }

    private void updateBadgeStyle() {
        roleLabel.getStyleClass().clear();
        roleLabel.getStyleClass().add("badge");

        switch (currentUser.getRole()) {
            case "ADMIN":
                roleLabel.getStyleClass().add("badge-danger");
                break;
            case "HOTE":
                roleLabel.getStyleClass().add("badge-warning");
                break;
            default:
                roleLabel.getStyleClass().add("badge-success");
        }

        statutLabel.getStyleClass().clear();
        statutLabel.getStyleClass().add("badge");

        switch (currentUser.getStatut()) {
            case "ACTIF":
                statutLabel.getStyleClass().add("badge-success");
                break;
            case "DESACTIVE":
                statutLabel.getStyleClass().add("badge-danger");
                break;
            default:
                statutLabel.getStyleClass().add("badge-warning");
        }
    }

    private void setupValidation() {
        // Validation téléphone
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 8) {
                telephoneField.setText(oldValue);
                return;
            }
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

        // Validation email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !ValidationUtils.isValidEmail(newValue)) {
                showFieldError(emailError, emailField, "Format d'email invalide");
            } else {
                hideFieldError(emailError, emailField);
            }
        });

        // Validation nouveau mot de passe
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                passwordStrengthLabel.setText("—");
                passwordStrengthLabel.setStyle("");
                hideFieldError(newPasswordError, newPasswordField);
            } else {
                String strength = ValidationUtils.getPasswordStrength(newValue);
                passwordStrengthLabel.setText(strength);

                switch (strength) {
                    case "Trop faible":
                        passwordStrengthLabel.setStyle("-fx-text-fill: #ef4444;");
                        showFieldError(newPasswordError, newPasswordField, "Mot de passe trop faible (min. 8 caractères)");
                        break;
                    case "Faible":
                        passwordStrengthLabel.setStyle("-fx-text-fill: #f59e0b;");
                        hideFieldError(newPasswordError, newPasswordField);
                        break;
                    case "Moyen":
                        passwordStrengthLabel.setStyle("-fx-text-fill: #3b82f6;");
                        hideFieldError(newPasswordError, newPasswordField);
                        break;
                    case "Fort":
                        passwordStrengthLabel.setStyle("-fx-text-fill: #10b981;");
                        hideFieldError(newPasswordError, newPasswordField);
                        break;
                }
            }
            checkPasswordMatch();
        });

        confirmNewPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkPasswordMatch();
        });
    }

    private void checkPasswordMatch() {
        String newPassword = newPasswordField.getText();
        String confirm = confirmNewPasswordField.getText();

        if (!confirm.isEmpty() && !newPassword.equals(confirm)) {
            showFieldError(confirmNewPasswordError, confirmNewPasswordField, "Les mots de passe ne correspondent pas");
        } else {
            hideFieldError(confirmNewPasswordError, confirmNewPasswordField);
        }
    }

    @FXML
    private void handleSaveProfile() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        // Validation
        boolean isValid = true;

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();

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
            showFieldError(telephoneError, telephoneField, "Numéro invalide (8 chiffres)");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Mise à jour
        try {
            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setEmail(email);
            currentUser.setTelephone(telephone);
            currentUser.setAdresse(adresseField.getText().trim());
            currentUser.setBio(bioField.getText().trim());

            serviceUtilisateur.modifier(currentUser);

            showMessage("Profil mis à jour avec succès !", false);

        } catch (SQLException e) {
            showMessage("Erreur lors de la mise à jour : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleChangePassword() {
        passwordMessageLabel.setVisible(false);
        passwordMessageLabel.setManaged(false);

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmNewPasswordField.getText();

        // Validation
        boolean isValid = true;

        if (!ValidationUtils.isNotEmpty(currentPassword)) {
            showFieldError(currentPasswordError, currentPasswordField, "Mot de passe actuel requis");
            isValid = false;
        }

        if (!ValidationUtils.isValidPassword(newPassword)) {
            showFieldError(newPasswordError, newPasswordField, "Le nouveau mot de passe doit contenir au moins 8 caractères");
            isValid = false;
        }

        if (!newPassword.equals(confirmPassword)) {
            showFieldError(confirmNewPasswordError, confirmNewPasswordField, "Les mots de passe ne correspondent pas");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Vérifier le mot de passe actuel et changer
        try {
            Utilisateur verification = serviceUtilisateur.login(currentUser.getEmail(), currentPassword);

            if (verification == null) {
                showFieldError(currentPasswordError, currentPasswordField, "Mot de passe actuel incorrect");
                return;
            }

            serviceUtilisateur.changerMotDePasse(currentUser.getId(), newPassword);

            showPasswordMessage("Mot de passe modifié avec succès !", false);

            // Réinitialiser les champs
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmNewPasswordField.clear();
            passwordStrengthLabel.setText("—");
            passwordStrengthLabel.setStyle("");

        } catch (SQLException e) {
            showPasswordMessage("Erreur lors du changement de mot de passe : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleCancel() {
        loadUserData();
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    @FXML
    private void handleBack() {
        if (currentUser.isAdmin()) {
            MainApp.changeScene("/fxml/Dashboard.fxml", "Lammetna - Dashboard Admin");
        }
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().deconnecter();
        MainApp.changeScene("/fxml/Login.fxml", "Lammetna - Connexion");
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

        messageLabel.getStyleClass().clear();
        if (isError) {
            messageLabel.getStyleClass().add("error-label");
        } else {
            messageLabel.getStyleClass().add("success-label");
        }
    }

    private void showPasswordMessage(String message, boolean isError) {
        passwordMessageLabel.setText(message);
        passwordMessageLabel.setVisible(true);
        passwordMessageLabel.setManaged(true);

        passwordMessageLabel.getStyleClass().clear();
        if (isError) {
            passwordMessageLabel.getStyleClass().add("error-label");
        } else {
            passwordMessageLabel.getStyleClass().add("success-label");
        }
    }
}