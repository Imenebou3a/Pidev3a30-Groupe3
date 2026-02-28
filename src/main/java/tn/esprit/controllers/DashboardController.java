package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.esprit.entities.Reclamation;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceReclamation;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.services.ProduitLocalService;
import tn.esprit.services.KitHobbiesService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    // Labels statistiques
    @FXML private Label lblTotalProduits;
    @FXML private Label lblTotalKits;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalReclamations;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalHotesLabel;
    @FXML private Label totalPendingLabel;
    @FXML private Label totalReclamationsLabel;
    @FXML private Label reclamationsEnAttenteLabel;
    @FXML private Label reclamationsEnCoursLabel;
    @FXML private Label reclamationsResoluesLabel;
    @FXML private Label welcomeLabel;

    // Filtres et recherche
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statutFilter;
    @FXML private ComboBox<String> reclamationStatutFilter;
    @FXML private ComboBox<String> reclamationCategorieFilter;
    @FXML private ComboBox<String> reclamationPrioriteFilter;

    // Tables
    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, Integer> idColumn;
    @FXML private TableColumn<Utilisateur, String> nomColumn;
    @FXML private TableColumn<Utilisateur, String> prenomColumn;
    @FXML private TableColumn<Utilisateur, String> emailColumn;
    @FXML private TableColumn<Utilisateur, String> telephoneColumn;
    @FXML private TableColumn<Utilisateur, String> roleColumn;
    @FXML private TableColumn<Utilisateur, String> statutColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsColumn;

    @FXML private TableView<Reclamation> reclamationsTable;
    @FXML private TableColumn<Reclamation, Integer> reclamationIdColumn;
    @FXML private TableColumn<Reclamation, String> reclamationUtilisateurColumn;
    @FXML private TableColumn<Reclamation, String> reclamationSujetColumn;
    @FXML private TableColumn<Reclamation, String> reclamationCategorieColumn;
    @FXML private TableColumn<Reclamation, String> reclamationStatutColumn;
    @FXML private TableColumn<Reclamation, String> reclamationPrioriteColumn;
    @FXML private TableColumn<Reclamation, LocalDateTime> reclamationDateColumn;
    @FXML private TableColumn<Reclamation, Void> reclamationActionsColumn;

    @FXML private Button modifyButton;

    private ServiceReclamation serviceReclamation = new ServiceReclamation();
    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private ProduitLocalService produitService = new ProduitLocalService();
    private KitHobbiesService kitService = new KitHobbiesService();

    private ObservableList<Utilisateur> usersList = FXCollections.observableArrayList();
    private ObservableList<Reclamation> reclamationsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            // Charger statistiques
            chargerStatistiques();
            
            // Configurer filtres
            configurerFiltres();
            
            // Configurer tables
            configurerTableUtilisateurs();
            configurerTableReclamations();
            
            // Charger données
            chargerUtilisateurs();
            chargerReclamations();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerStatistiques() {
        try {
            List<Utilisateur> users = serviceUtilisateur.afficher();
            List<Reclamation> reclamations = serviceReclamation.getAll();
            
            int totalUsers = users.size();
            int totalReclamations = reclamations.size();
            int totalProduits = produitService.afficher().size();
            int totalKits = kitService.afficher().size();
            
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(totalUsers));
            if (totalReclamationsLabel != null) totalReclamationsLabel.setText(String.valueOf(totalReclamations));
            if (lblTotalProduits != null) lblTotalProduits.setText(String.valueOf(totalProduits));
            if (lblTotalKits != null) lblTotalKits.setText(String.valueOf(totalKits));
            
            // Statistiques utilisateurs
            long admins = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            long hotes = users.stream().filter(u -> "HOTE".equals(u.getRole())).count();
            long pending = users.stream().filter(u -> "EN_ATTENTE".equals(u.getStatut())).count();
            
            if (totalAdminsLabel != null) totalAdminsLabel.setText(String.valueOf(admins));
            if (totalHotesLabel != null) totalHotesLabel.setText(String.valueOf(hotes));
            if (totalPendingLabel != null) totalPendingLabel.setText(String.valueOf(pending));
            
            // Statistiques réclamations
            int enAttente = serviceReclamation.countByStatut("EN_ATTENTE");
            int enCours = serviceReclamation.countByStatut("EN_COURS");
            int resolues = serviceReclamation.countByStatut("RESOLUE");
            
            if (reclamationsEnAttenteLabel != null) reclamationsEnAttenteLabel.setText(String.valueOf(enAttente));
            if (reclamationsEnCoursLabel != null) reclamationsEnCoursLabel.setText(String.valueOf(enCours));
            if (reclamationsResoluesLabel != null) reclamationsResoluesLabel.setText(String.valueOf(resolues));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void configurerFiltres() {
        if (roleFilter != null) {
            roleFilter.setItems(FXCollections.observableArrayList("Tous", "USER", "HOTE", "ADMIN"));
            roleFilter.setValue("Tous");
        }
        
        if (statutFilter != null) {
            statutFilter.setItems(FXCollections.observableArrayList("Tous", "ACTIF", "EN_ATTENTE", "DESACTIVE"));
            statutFilter.setValue("Tous");
        }
        
        if (reclamationStatutFilter != null) {
            reclamationStatutFilter.setItems(FXCollections.observableArrayList("Tous", "EN_ATTENTE", "EN_COURS", "RESOLUE", "REJETEE"));
            reclamationStatutFilter.setValue("Tous");
        }
        
        if (reclamationCategorieFilter != null) {
            reclamationCategorieFilter.setItems(FXCollections.observableArrayList("Tous", "TECHNIQUE", "SERVICE", "HEBERGEMENT", "EVENEMENT", "AUTRE"));
            reclamationCategorieFilter.setValue("Tous");
        }
        
        if (reclamationPrioriteFilter != null) {
            reclamationPrioriteFilter.setItems(FXCollections.observableArrayList("Tous", "BASSE", "MOYENNE", "HAUTE"));
            reclamationPrioriteFilter.setValue("Tous");
        }
    }

    private void configurerTableUtilisateurs() {
        if (usersTable == null) return;
        
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btnActiver = new Button("✓");
            private final Button btnDesactiver = new Button("✗");
            private final HBox box = new HBox(5, btnActiver, btnDesactiver);
            
            {
                btnActiver.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
                btnDesactiver.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                
                btnActiver.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    activerUtilisateur(u);
                });
                
                btnDesactiver.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    desactiverUtilisateur(u);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void configurerTableReclamations() {
        if (reclamationsTable == null) return;
        
        reclamationIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        reclamationUtilisateurColumn.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
        reclamationSujetColumn.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        reclamationCategorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        reclamationStatutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        reclamationPrioriteColumn.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        reclamationDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        
        reclamationDateColumn.setCellFactory(col -> new TableCell<>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date));
            }
        });
        
        reclamationActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btnVoir = new Button("👁");
            private final Button btnRepondre = new Button("✉");
            private final HBox box = new HBox(5, btnVoir, btnRepondre);
            
            {
                btnVoir.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
                btnRepondre.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
                
                btnVoir.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    voirReclamation(r);
                });
                
                btnRepondre.setOnAction(e -> {
                    Reclamation r = getTableView().getItems().get(getIndex());
                    repondreReclamation(r);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void chargerUtilisateurs() {
        try {
            if (usersTable != null) {
                usersList.setAll(serviceUtilisateur.afficher());
                usersTable.setItems(usersList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chargerReclamations() {
        try {
            if (reclamationsTable != null) {
                reclamationsList.setAll(serviceReclamation.getAll());
                reclamationsTable.setItems(reclamationsList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        // Recherche utilisateurs
        String search = searchField != null ? searchField.getText().toLowerCase() : "";
        String role = roleFilter != null ? roleFilter.getValue() : "Tous";
        String statut = statutFilter != null ? statutFilter.getValue() : "Tous";
        
        try {
            List<Utilisateur> all = serviceUtilisateur.afficher();
            ObservableList<Utilisateur> filtered = FXCollections.observableArrayList();
            
            for (Utilisateur u : all) {
                boolean matchSearch = search.isEmpty() || 
                    u.getNom().toLowerCase().contains(search) ||
                    u.getPrenom().toLowerCase().contains(search) ||
                    u.getEmail().toLowerCase().contains(search);
                boolean matchRole = "Tous".equals(role) || u.getRole().equals(role);
                boolean matchStatut = "Tous".equals(statut) || u.getStatut().equals(statut);
                
                if (matchSearch && matchRole && matchStatut) {
                    filtered.add(u);
                }
            }
            
            if (usersTable != null) {
                usersTable.setItems(filtered);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReset() {
        if (searchField != null) searchField.clear();
        if (roleFilter != null) roleFilter.setValue("Tous");
        if (statutFilter != null) statutFilter.setValue("Tous");
        chargerUtilisateurs();
    }

    @FXML
    private void handleResetReclamations() {
        if (reclamationStatutFilter != null) reclamationStatutFilter.setValue("Tous");
        if (reclamationCategorieFilter != null) reclamationCategorieFilter.setValue("Tous");
        if (reclamationPrioriteFilter != null) reclamationPrioriteFilter.setValue("Tous");
        chargerReclamations();
    }

    @FXML
    private void handleModifyUser() {
        // TODO: Implement modify user
    }

    @FXML
    private void handleAddUser() {
        // TODO: Implement add user
    }

    @FXML
    private void handleGoToProfil() {
        tn.esprit.MainApp.changeScene("/fxml/frontoffice/Profil.fxml", "Lammetna - Mon Profil");
    }

    @FXML
    private void handleLogout() {
        tn.esprit.utils.Session.getInstance().deconnecter();
        tn.esprit.MainApp.changeScene("/fxml/frontoffice/login.fxml", "Lammetna - Connexion");
    }

    private void activerUtilisateur(Utilisateur u) {
        try {
            serviceUtilisateur.activerCompte(u.getId());
            showSuccess("Compte activé: " + u.getNomComplet());
            chargerUtilisateurs();
            chargerStatistiques();
        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void desactiverUtilisateur(Utilisateur u) {
        try {
            serviceUtilisateur.desactiverCompte(u.getId());
            showSuccess("Compte désactivé");
            chargerUtilisateurs();
            chargerStatistiques();
        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void voirReclamation(Reclamation r) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails");
        alert.setHeaderText("Réclamation #" + r.getId());
        alert.setContentText("Sujet: " + r.getSujet() + "\n\nDescription: " + r.getDescription());
        alert.showAndWait();
    }

    private void repondreReclamation(Reclamation r) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Répondre");
        dialog.setHeaderText("Réclamation #" + r.getId());
        dialog.setContentText("Votre réponse:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reponse -> {
            try {
                serviceReclamation.repondre(r.getId(), reponse, "EN_COURS");
                showSuccess("Réponse envoyée");
                chargerReclamations();
                chargerStatistiques();
            } catch (SQLException e) {
                showError("Erreur: " + e.getMessage());
            }
        });
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}