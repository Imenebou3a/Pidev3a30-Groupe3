package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.MainApp;
import tn.esprit.entities.Reclamation;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceReclamation;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    // ========== UTILISATEURS ==========
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

    // ========== RÉCLAMATIONS ==========
    @FXML private Label totalReclamationsLabel;
    @FXML private Label reclamationsEnAttenteLabel;
    @FXML private Label reclamationsEnCoursLabel;
    @FXML private Label reclamationsResoluesLabel;

    @FXML private ComboBox<String> reclamationStatutFilter;
    @FXML private ComboBox<String> reclamationCategorieFilter;
    @FXML private ComboBox<String> reclamationPrioriteFilter;

    @FXML private TableView<Reclamation> reclamationsTable;
    @FXML private TableColumn<Reclamation, Integer> reclamationIdColumn;
    @FXML private TableColumn<Reclamation, String> reclamationUtilisateurColumn;
    @FXML private TableColumn<Reclamation, String> reclamationSujetColumn;
    @FXML private TableColumn<Reclamation, String> reclamationCategorieColumn;
    @FXML private TableColumn<Reclamation, String> reclamationStatutColumn;
    @FXML private TableColumn<Reclamation, String> reclamationPrioriteColumn;
    @FXML private TableColumn<Reclamation, LocalDateTime> reclamationDateColumn;
    @FXML private TableColumn<Reclamation, Void> reclamationActionsColumn;

    // ========== SERVICES ==========
    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private ServiceReclamation serviceReclamation = new ServiceReclamation();

    private ObservableList<Utilisateur> utilisateursList = FXCollections.observableArrayList();
    private ObservableList<Reclamation> reclamationsList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        initializeUtilisateurs();
        initializeReclamations();
    }

    // ==================== UTILISATEURS ====================

    private void initializeUtilisateurs() {
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

        // Colonne des actions
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

        // Activer/désactiver le bouton Modifier
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

    @FXML
    private void handleAddUser() {
        MainApp.changeScene("/fxml/AddUser.fxml", "Lammetna - Ajouter un utilisateur");
    }

    @FXML
    private void handleModifyUser() {
        Utilisateur selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur à modifier !");
            return;
        }

        EditUserController.setUser(selectedUser);
        MainApp.changeScene("/fxml/EditUser.fxml", "Lammetna - Modifier un utilisateur");
    }

    // ==================== RÉCLAMATIONS ====================

    private void initializeReclamations() {
        // Configurer les filtres
        reclamationStatutFilter.setItems(FXCollections.observableArrayList("Tous", "EN_ATTENTE", "EN_COURS", "RESOLUE", "REJETEE"));
        reclamationStatutFilter.setValue("Tous");

        reclamationCategorieFilter.setItems(FXCollections.observableArrayList("Tous", "TECHNIQUE", "SERVICE", "HEBERGEMENT", "EVENEMENT", "AUTRE"));
        reclamationCategorieFilter.setValue("Tous");

        reclamationPrioriteFilter.setItems(FXCollections.observableArrayList("Tous", "BASSE", "MOYENNE", "HAUTE"));
        reclamationPrioriteFilter.setValue("Tous");

        // Configurer les colonnes
        reclamationIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        reclamationUtilisateurColumn.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
        reclamationSujetColumn.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        reclamationCategorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        reclamationStatutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        reclamationPrioriteColumn.setCellValueFactory(new PropertyValueFactory<>("priorite"));

        // Formatter la date
        reclamationDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        reclamationDateColumn.setCellFactory(column -> new TableCell<Reclamation, LocalDateTime>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Colonne des actions
        reclamationActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("👁 Voir");
            private final Button btnRepondre = new Button("💬 Répondre");
            private final Button btnSupprimer = new Button("🗑 Supprimer");

            {
                btnVoir.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnRepondre.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnSupprimer.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");

                btnVoir.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    voirReclamation(reclamation);
                });

                btnRepondre.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    repondreReclamation(reclamation);
                });

                btnSupprimer.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    supprimerReclamation(reclamation);
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
                    buttons.getChildren().addAll(btnVoir, btnRepondre, btnSupprimer);
                    setGraphic(buttons);
                }
            }
        });

        // Charger les données
        loadReclamations();
        updateReclamationsStatistics();

        // Appliquer le style aux colonnes
        styleReclamationsTable();

        // Listeners pour les filtres
        reclamationStatutFilter.setOnAction(e -> handleFilterReclamations());
        reclamationCategorieFilter.setOnAction(e -> handleFilterReclamations());
        reclamationPrioriteFilter.setOnAction(e -> handleFilterReclamations());
    }

    private void styleReclamationsTable() {
        // Style pour la colonne Statut
        reclamationStatutColumn.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(statut.replace("_", " "));
                    badge.getStyleClass().addAll("statut-badge");

                    switch (statut) {
                        case "EN_ATTENTE":
                            badge.getStyleClass().add("statut-en-attente");
                            break;
                        case "EN_COURS":
                            badge.getStyleClass().add("statut-en-cours");
                            break;
                        case "RESOLUE":
                            badge.getStyleClass().add("statut-resolue");
                            break;
                        case "REJETEE":
                            badge.getStyleClass().add("statut-rejetee");
                            break;
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Style pour la colonne Priorité
        reclamationPrioriteColumn.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String priorite, boolean empty) {
                super.updateItem(priorite, empty);
                if (empty || priorite == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(priorite);
                    badge.getStyleClass().addAll("priorite-badge");

                    switch (priorite) {
                        case "BASSE":
                            badge.getStyleClass().add("priorite-basse");
                            break;
                        case "MOYENNE":
                            badge.getStyleClass().add("priorite-moyenne");
                            break;
                        case "HAUTE":
                            badge.getStyleClass().add("priorite-haute");
                            break;
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Style pour la colonne Catégorie
        reclamationCategorieColumn.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String categorie, boolean empty) {
                super.updateItem(categorie, empty);
                if (empty || categorie == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(categorie);
                    badge.getStyleClass().addAll("categorie-badge");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    private void loadReclamations() {
        try {
            List<Reclamation> reclamations = serviceReclamation.afficher();
            reclamationsList.clear();
            reclamationsList.addAll(reclamations);
            reclamationsTable.setItems(reclamationsList);
        } catch (SQLException e) {
            showError("Erreur lors du chargement des réclamations : " + e.getMessage());
        }
    }

    private void updateReclamationsStatistics() {
        try {
            List<Reclamation> all = serviceReclamation.afficher();
            int total = all.size();
            int enAttente = (int) all.stream().filter(r -> "EN_ATTENTE".equals(r.getStatut())).count();
            int enCours = (int) all.stream().filter(r -> "EN_COURS".equals(r.getStatut())).count();
            int resolues = (int) all.stream().filter(r -> "RESOLUE".equals(r.getStatut())).count();

            totalReclamationsLabel.setText(String.valueOf(total));
            reclamationsEnAttenteLabel.setText(String.valueOf(enAttente));
            reclamationsEnCoursLabel.setText(String.valueOf(enCours));
            reclamationsResoluesLabel.setText(String.valueOf(resolues));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilterReclamations() {
        String statutSelected = reclamationStatutFilter.getValue();
        String categorieSelected = reclamationCategorieFilter.getValue();
        String prioriteSelected = reclamationPrioriteFilter.getValue();

        try {
            List<Reclamation> all = serviceReclamation.afficher();
            ObservableList<Reclamation> filtered = FXCollections.observableArrayList();

            for (Reclamation r : all) {
                boolean matchStatut = "Tous".equals(statutSelected) || r.getStatut().equals(statutSelected);
                boolean matchCategorie = "Tous".equals(categorieSelected) || r.getCategorie().equals(categorieSelected);
                boolean matchPriorite = "Tous".equals(prioriteSelected) || r.getPriorite().equals(prioriteSelected);

                if (matchStatut && matchCategorie && matchPriorite) {
                    filtered.add(r);
                }
            }

            reclamationsTable.setItems(filtered);
        } catch (SQLException e) {
            showError("Erreur lors du filtrage : " + e.getMessage());
        }
    }

    @FXML
    private void handleResetReclamations() {
        reclamationStatutFilter.setValue("Tous");
        reclamationCategorieFilter.setValue("Tous");
        reclamationPrioriteFilter.setValue("Tous");
        loadReclamations();
    }

    private void voirReclamation(Reclamation reclamation) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la réclamation");
        dialog.setHeaderText(null);

        // Boutons
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButtonType);

        // Contenu
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setStyle("-fx-background-color: white;");

        // Titre avec emoji
        Label titre = new Label("📋 " + reclamation.getSujet());
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // ID et Utilisateur
        HBox infoBox = new HBox(20);
        Label idLabel = new Label("ID: #" + reclamation.getId());
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        Label userLabel = new Label("👤 " + reclamation.getNomUtilisateur());
        userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        infoBox.getChildren().addAll(idLabel, userLabel);

        // Badges
        HBox badgesBox = new HBox(10);

        Label categorieLabel = new Label(reclamation.getCategorie());
        categorieLabel.getStyleClass().addAll("categorie-badge");

        Label statutLabel = new Label(reclamation.getStatut().replace("_", " "));
        statutLabel.getStyleClass().addAll("statut-badge");
        switch (reclamation.getStatut()) {
            case "EN_ATTENTE": statutLabel.getStyleClass().add("statut-en-attente"); break;
            case "EN_COURS": statutLabel.getStyleClass().add("statut-en-cours"); break;
            case "RESOLUE": statutLabel.getStyleClass().add("statut-resolue"); break;
            case "REJETEE": statutLabel.getStyleClass().add("statut-rejetee"); break;
        }

        Label prioriteLabel = new Label(reclamation.getPriorite());
        prioriteLabel.getStyleClass().addAll("priorite-badge");
        switch (reclamation.getPriorite()) {
            case "BASSE": prioriteLabel.getStyleClass().add("priorite-basse"); break;
            case "MOYENNE": prioriteLabel.getStyleClass().add("priorite-moyenne"); break;
            case "HAUTE": prioriteLabel.getStyleClass().add("priorite-haute"); break;
        }

        badgesBox.getChildren().addAll(categorieLabel, statutLabel, prioriteLabel);

        // Date
        Label dateLabel = new Label("📅 " + reclamation.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        // Separator
        Separator sep1 = new Separator();

        // Description
        Label descTitle = new Label("Description :");
        descTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        TextArea descArea = new TextArea(reclamation.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(4);
        descArea.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        content.getChildren().addAll(titre, infoBox, badgesBox, dateLabel, sep1, descTitle, descArea);

        // Réponse admin si existe
        if (reclamation.getReponseAdmin() != null && !reclamation.getReponseAdmin().isEmpty()) {
            Separator sep2 = new Separator();
            Label reponseTitle = new Label("💬 Réponse de l'administrateur :");
            reponseTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #10b981;");

            TextArea reponseArea = new TextArea(reclamation.getReponseAdmin());
            reponseArea.setWrapText(true);
            reponseArea.setEditable(false);
            reponseArea.setPrefRowCount(3);
            reponseArea.setStyle("-fx-background-color: #ecfdf5; -fx-border-color: #10b981; -fx-border-radius: 8;");

            content.getChildren().addAll(sep2, reponseTitle, reponseArea);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(600);

        dialog.showAndWait();
    }

    private void repondreReclamation(Reclamation reclamation) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Répondre à la réclamation");
        dialog.setHeaderText("Réclamation #" + reclamation.getId() + " - " + reclamation.getSujet());

        ButtonType repondreButtonType = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(repondreButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));

        Label label = new Label("Votre réponse:");
        TextArea reponseArea = new TextArea();
        reponseArea.setPrefRowCount(5);
        reponseArea.setPromptText("Saisissez votre réponse...");

        if (reclamation.getReponseAdmin() != null) {
            reponseArea.setText(reclamation.getReponseAdmin());
        }

        Label statutLabel = new Label("Nouveau statut:");
        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.setItems(FXCollections.observableArrayList("EN_ATTENTE", "EN_COURS", "RESOLUE", "REJETEE"));
        statutCombo.setValue(reclamation.getStatut());

        content.getChildren().addAll(label, reponseArea, statutLabel, statutCombo);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == repondreButtonType) {
                return reponseArea.getText() + "|" + statutCombo.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(response -> {
            try {
                String[] parts = response.split("\\|");
                String reponse = parts[0];
                String statut = parts[1];

                serviceReclamation.repondre(reclamation.getId(), reponse, statut);
                showSuccess("Réponse envoyée avec succès !");
                loadReclamations();
                updateReclamationsStatistics();
            } catch (SQLException e) {
                showError("Erreur lors de l'envoi : " + e.getMessage());
            }
        });
    }

    private void supprimerReclamation(Reclamation reclamation) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer la réclamation");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceReclamation.supprimer(reclamation.getId());
                showSuccess("Réclamation supprimée avec succès !");
                loadReclamations();
                updateReclamationsStatistics();
            } catch (SQLException e) {
                showError("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleGoToProfil() {
        MainApp.changeScene("/fxml/Profil.fxml", "Lammetna - Mon Profil");
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().deconnecter();
        MainApp.changeScene("/fxml/login.fxml", "Lammetna - Connexion");
    }

    // ==================== MESSAGES ====================

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