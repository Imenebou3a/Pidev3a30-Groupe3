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

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalHotesLabel;
    @FXML private Label totalPendingLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statutFilter;
    @FXML private Button modifyButton;

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

        // Colonne des actions (SANS le bouton Rôle)
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnActiver = new Button("✓ Activer");
            private final Button btnDesactiver = new Button("✗ Désactiver");
            private final Button btnSupprimer = new Button("🗑 Supprimer");

            {
                btnActiver.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnDesactiver.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnSupprimer.setStyle("-fx-background-color: #991b1b; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");

                btnActiver.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    activerUtilisateur(user);
                });

                btnDesactiver.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    desactiverUtilisateur(user);
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
                        buttons.getChildren().addAll(btnDesactiver, btnSupprimer);
                    } else {
                        buttons.getChildren().addAll(btnActiver, btnSupprimer);
                    }

                    setGraphic(buttons);
                }
            }
        });

        // Charger les données
        loadData();
        updateStatistics();

        // Activer/désactiver le bouton Modifier selon la sélection
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

    // NOUVELLE MÉTHODE - Rediriger vers la page d'ajout
    @FXML
    private void handleAddUser() {
        MainApp.changeScene("/fxml/AddUser.fxml", "Lammetna - Ajouter un utilisateur");
    }

    // NOUVELLE MÉTHODE - Rediriger vers la page de modification
    @FXML
    private void handleModifyUser() {
        Utilisateur selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur à modifier !");
            return;
        }

        // Passer l'utilisateur au controller
        EditUserController.setUser(selectedUser);
        MainApp.changeScene("/fxml/EditUser.fxml", "Lammetna - Modifier un utilisateur");
    }

    @FXML
    private void handleGoToProfil() {
        MainApp.changeScene("/fxml/Profil.fxml", "Lammetna - Mon Profil");
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().deconnecter();
        MainApp.changeScene("/fxml/login.fxml", "Lammetna - Connexion");
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
}