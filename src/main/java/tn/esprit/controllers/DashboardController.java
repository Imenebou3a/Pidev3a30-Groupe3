package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.esprit.MainApp;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.Session;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import tn.esprit.utils.ValidationUtils;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalHotesLabel;
    @FXML private Label totalPendingLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statutFilter;
    @FXML private Button modifyButton;  // ← NOUVEAU

    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, Integer> idColumn;
    @FXML private TableColumn<Utilisateur, String> nomColumn;
    @FXML private TableColumn<Utilisateur, String> prenomColumn;
    @FXML private TableColumn<Utilisateur, String> emailColumn;
    @FXML private TableColumn<Utilisateur, String> telephoneColumn;
    @FXML private TableColumn<Utilisateur, String> roleColumn;
    @FXML private TableColumn<Utilisateur, String> statutColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsColumn;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private ObservableList<Utilisateur> utilisateursList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Message de bienvenue
        Utilisateur currentUser = Session.getInstance().getUtilisateurConnecte();
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue " + currentUser.getNomComplet());
        }

        // Configurer les ComboBox
        roleFilter.setItems(FXCollections.observableArrayList("Tous", "USER", "HOTE", "ADMIN"));
        roleFilter.setValue("Tous");

        statutFilter.setItems(FXCollections.observableArrayList("Tous", "ACTIF", "DESACTIVE", "EN_ATTENTE"));
        statutFilter.setValue("Tous");

        // Configurer les colonnes du tableau
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Colonne des actions (SANS le bouton Modifier)
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnActiver = new Button("✓ Activer");
            private final Button btnDesactiver = new Button("✗ Désactiver");
            private final Button btnRole = new Button("🔧 Rôle");
            private final Button btnSupprimer = new Button("🗑 Supprimer");

            {
                btnActiver.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnDesactiver.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnRole.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnSupprimer.setStyle("-fx-background-color: #991b1b; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");

                btnActiver.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    activerUtilisateur(user);
                });

                btnDesactiver.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    desactiverUtilisateur(user);
                });

                btnRole.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    changerRole(user);
                });

                btnSupprimer.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    supprimerUtilisateur(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.setAlignment(Pos.CENTER);

                    Utilisateur user = getTableView().getItems().get(getIndex());

                    if ("ACTIF".equals(user.getStatut())) {
                        buttons.getChildren().addAll(btnDesactiver, btnRole, btnSupprimer);
                    } else {
                        buttons.getChildren().addAll(btnActiver, btnRole, btnSupprimer);
                    }

                    setGraphic(buttons);
                }
            }
        });

        // Charger les données
        loadData();
        updateStatistics();

        // ← NOUVEAU : Activer/désactiver le bouton Modifier selon la sélection
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            modifyButton.setDisable(newSelection == null);
        });
    }

    private void loadData() {
        try {
            List<Utilisateur> users = serviceUtilisateur.afficher();
            utilisateursList.clear();
            utilisateursList.addAll(users);
            usersTable.setItems(utilisateursList);
        } catch (SQLException e) {
            showError("Erreur lors du chargement des données : " + e.getMessage());
        }
    }

    private void updateStatistics() {
        try {
            List<Utilisateur> allUsers = serviceUtilisateur.afficher();
            int total = allUsers.size();
            int admins = (int) allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            int hotes = (int) allUsers.stream().filter(u -> "HOTE".equals(u.getRole())).count();
            int pending = (int) allUsers.stream().filter(u -> "EN_ATTENTE".equals(u.getStatut())).count();

            totalUsersLabel.setText(String.valueOf(total));
            totalAdminsLabel.setText(String.valueOf(admins));
            totalHotesLabel.setText(String.valueOf(hotes));
            totalPendingLabel.setText(String.valueOf(pending));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        String roleSelected = roleFilter.getValue();
        String statutSelected = statutFilter.getValue();

        try {
            List<Utilisateur> allUsers = serviceUtilisateur.afficher();
            ObservableList<Utilisateur> filteredList = FXCollections.observableArrayList();

            for (Utilisateur user : allUsers) {
                boolean matchSearch = searchText.isEmpty() ||
                        user.getNom().toLowerCase().contains(searchText) ||
                        user.getPrenom().toLowerCase().contains(searchText) ||
                        user.getEmail().toLowerCase().contains(searchText);

                boolean matchRole = "Tous".equals(roleSelected) || user.getRole().equals(roleSelected);
                boolean matchStatut = "Tous".equals(statutSelected) || user.getStatut().equals(statutSelected);

                if (matchSearch && matchRole && matchStatut) {
                    filteredList.add(user);
                }
            }

            usersTable.setItems(filteredList);
        } catch (SQLException e) {
            showError("Erreur lors de la recherche : " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        roleFilter.setValue("Tous");
        statutFilter.setValue("Tous");
        loadData();
    }

    private void activerUtilisateur(Utilisateur user) {
        try {
            serviceUtilisateur.activerCompte(user.getId());
            showSuccess("Compte activé avec succès !");
            loadData();
            updateStatistics();
        } catch (SQLException e) {
            showError("Erreur lors de l'activation : " + e.getMessage());
        }
    }

    private void desactiverUtilisateur(Utilisateur user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Désactiver le compte");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir désactiver ce compte ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceUtilisateur.desactiverCompte(user.getId());
                showSuccess("Compte désactivé avec succès !");
                loadData();
                updateStatistics();
            } catch (SQLException e) {
                showError("Erreur lors de la désactivation : " + e.getMessage());
            }
        }
    }

    private void changerRole(Utilisateur user) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("USER", "USER", "HOTE", "ADMIN");
        dialog.setTitle("Changer le rôle");
        dialog.setHeaderText("Modifier le rôle de " + user.getNomComplet());
        dialog.setContentText("Nouveau rôle :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(role -> {
            try {
                serviceUtilisateur.changerRole(user.getId(), role);
                showSuccess("Rôle modifié avec succès !");
                loadData();
                updateStatistics();
            } catch (SQLException e) {
                showError("Erreur lors du changement de rôle : " + e.getMessage());
            }
        });
    }

    private void supprimerUtilisateur(Utilisateur user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement cet utilisateur ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceUtilisateur.supprimer(user.getId());
                showSuccess("Utilisateur supprimé avec succès !");
                loadData();
                updateStatistics();
            } catch (SQLException e) {
                showError("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    private void modifierUtilisateur(Utilisateur user) {
        // Créer une boîte de dialogue personnalisée
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'utilisateur");
        dialog.setHeaderText("Modifier les informations de " + user.getNomComplet());

        // Boutons
        ButtonType modifierButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(modifierButtonType, ButtonType.CANCEL);

        // Créer le formulaire pré-rempli
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nomField = new TextField(user.getNom());
        TextField prenomField = new TextField(user.getPrenom());
        TextField emailField = new TextField(user.getEmail());
        TextField telephoneField = new TextField(user.getTelephone());
        TextField adresseField = new TextField(user.getAdresse() != null ? user.getAdresse() : "");
        TextArea bioField = new TextArea(user.getBio() != null ? user.getBio() : "");
        bioField.setPrefRowCount(3);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("USER", "HOTE", "ADMIN");
        roleCombo.setValue(user.getRole());

        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("ACTIF", "EN_ATTENTE", "DESACTIVE");
        statutCombo.setValue(user.getStatut());

        grid.add(new Label("Nom *:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom *:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email *:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Téléphone *:"), 0, 3);
        grid.add(telephoneField, 1, 3);
        grid.add(new Label("Adresse:"), 0, 4);
        grid.add(adresseField, 1, 4);
        grid.add(new Label("Bio:"), 0, 5);
        grid.add(bioField, 1, 5);
        grid.add(new Label("Rôle:"), 0, 6);
        grid.add(roleCombo, 1, 6);
        grid.add(new Label("Statut:"), 0, 7);
        grid.add(statutCombo, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // Convertir le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == modifierButtonType) {
                user.setNom(nomField.getText().trim());
                user.setPrenom(prenomField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setTelephone(telephoneField.getText().trim());
                user.setAdresse(adresseField.getText().trim());
                user.setBio(bioField.getText().trim());
                user.setRole(roleCombo.getValue());
                user.setStatut(statutCombo.getValue());
                return user;
            }
            return null;
        });

        // Afficher la boîte de dialogue et traiter le résultat
        Optional<Utilisateur> result = dialog.showAndWait();

        result.ifPresent(modifiedUser -> {
            try {
                // Validation
                if (!ValidationUtils.isValidEmail(modifiedUser.getEmail())) {
                    showError("Email invalide !");
                    return;
                }

                if (!ValidationUtils.isValidPhone(modifiedUser.getTelephone())) {
                    showError("Numéro de téléphone invalide ! (8 chiffres requis)");
                    return;
                }

                // Modifier l'utilisateur
                serviceUtilisateur.modifier(modifiedUser);

                // Appliquer les changements de statut si nécessaire
                if ("ACTIF".equals(modifiedUser.getStatut())) {
                    serviceUtilisateur.activerCompte(modifiedUser.getId());
                } else if ("DESACTIVE".equals(modifiedUser.getStatut())) {
                    serviceUtilisateur.desactiverCompte(modifiedUser.getId());
                }

                // Appliquer le changement de rôle
                serviceUtilisateur.changerRole(modifiedUser.getId(), modifiedUser.getRole());

                showSuccess("Utilisateur modifié avec succès !");
                loadData();
                updateStatistics();

            } catch (SQLException e) {
                showError("Erreur lors de la modification : " + e.getMessage());
            }
        });
    }

    // ← NOUVELLE MÉTHODE
    @FXML
    private void handleModifyUser() {
        // Récupérer l'utilisateur sélectionné
        Utilisateur selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur à modifier !");
            return;
        }

        // Appeler la méthode de modification existante
        modifierUtilisateur(selectedUser);
    }

    @FXML
    private void handleGoToProfil() {
        MainApp.changeScene("/fxml/Profil.fxml", "Lammetna - Mon Profil");
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().deconnecter();
        MainApp.changeScene("/fxml/Login.fxml", "Lammetna - Connexion");
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAddUser() {
        // Créer une boîte de dialogue personnalisée
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText("Créer un nouveau compte utilisateur");

        // Boutons
        ButtonType creerButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(creerButtonType, ButtonType.CANCEL);

        // Créer le formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone (8 chiffres)");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("USER", "HOTE", "ADMIN");
        roleCombo.setValue("USER");

        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("ACTIF", "EN_ATTENTE", "DESACTIVE");
        statutCombo.setValue("ACTIF");

        grid.add(new Label("Nom *:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom *:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email *:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Téléphone *:"), 0, 3);
        grid.add(telephoneField, 1, 3);
        grid.add(new Label("Mot de passe *:"), 0, 4);
        grid.add(passwordField, 1, 4);
        grid.add(new Label("Rôle:"), 0, 5);
        grid.add(roleCombo, 1, 5);
        grid.add(new Label("Statut:"), 0, 6);
        grid.add(statutCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Validation avant création
        javafx.scene.Node creerButton = dialog.getDialogPane().lookupButton(creerButtonType);
        creerButton.setDisable(true);

        // Activer le bouton seulement si les champs requis sont remplis
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            creerButton.setDisable(newValue.trim().isEmpty() ||
                    prenomField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() ||
                    telephoneField.getText().trim().isEmpty() ||
                    passwordField.getText().trim().isEmpty());
        });

        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            creerButton.setDisable(newValue.trim().isEmpty() ||
                    nomField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() ||
                    telephoneField.getText().trim().isEmpty() ||
                    passwordField.getText().trim().isEmpty());
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            creerButton.setDisable(newValue.trim().isEmpty() ||
                    nomField.getText().trim().isEmpty() ||
                    prenomField.getText().trim().isEmpty() ||
                    telephoneField.getText().trim().isEmpty() ||
                    passwordField.getText().trim().isEmpty());
        });

        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            creerButton.setDisable(newValue.trim().isEmpty() ||
                    nomField.getText().trim().isEmpty() ||
                    prenomField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() ||
                    passwordField.getText().trim().isEmpty());
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            creerButton.setDisable(newValue.trim().isEmpty() ||
                    nomField.getText().trim().isEmpty() ||
                    prenomField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() ||
                    telephoneField.getText().trim().isEmpty());
        });

        // Convertir le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == creerButtonType) {
                Utilisateur newUser = new Utilisateur(
                        nomField.getText().trim(),
                        prenomField.getText().trim(),
                        emailField.getText().trim(),
                        passwordField.getText().trim(),
                        telephoneField.getText().trim()
                );
                newUser.setRole(roleCombo.getValue());
                return newUser;
            }
            return null;
        });

        // Afficher la boîte de dialogue et traiter le résultat
        Optional<Utilisateur> result = dialog.showAndWait();

        result.ifPresent(newUser -> {
            try {
                // Validation avancée
                if (!ValidationUtils.isValidEmail(newUser.getEmail())) {
                    showError("Email invalide !");
                    return;
                }

                if (!ValidationUtils.isValidPhone(newUser.getTelephone())) {
                    showError("Numéro de téléphone invalide ! (8 chiffres requis)");
                    return;
                }

                if (!ValidationUtils.isValidPassword(passwordField.getText())) {
                    showError("Le mot de passe doit contenir au moins 8 caractères !");
                    return;
                }

                // Créer l'utilisateur
                if (serviceUtilisateur.inscrire(newUser)) {
                    // Appliquer le statut choisi
                    if ("ACTIF".equals(statutCombo.getValue())) {
                        serviceUtilisateur.activerCompte(newUser.getId());
                    } else if ("DESACTIVE".equals(statutCombo.getValue())) {
                        serviceUtilisateur.desactiverCompte(newUser.getId());
                    }

                    showSuccess("Utilisateur créé avec succès !");
                    loadData();
                    updateStatistics();
                } else {
                    showError("Cet email est déjà utilisé !");
                }
            } catch (SQLException e) {
                showError("Erreur lors de la création : " + e.getMessage());
            }
        });
    }
}