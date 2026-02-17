package tn.esprit.controllers.backoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class UserBackController {

    @FXML private TableView<Utilisateur> tableUsers;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom, colPrenom, colEmail, colTelephone, colRole, colStatut;
    @FXML private TableColumn<Utilisateur, LocalDateTime> colDateInscription;
    @FXML private TableColumn<Utilisateur, Void> colActions;
    
    @FXML private ComboBox<String> comboRole, comboStatut;
    @FXML private TextField txtRecherche;
    @FXML private Label lblStats, lblTotalUsers, lblUsersActifs, lblUsersEnAttente;

    private ServiceUtilisateur service = new ServiceUtilisateur();
    private ObservableList<Utilisateur> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurerTableau();
        configurerFiltres();
        chargerUtilisateurs();
        mettreAJourStatistiques();
    }

    private void configurerTableau() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateInscription.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        
        colDateInscription.setCellFactory(col -> new TableCell<>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date));
            }
        });

        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    setStyle(switch (statut) {
                        case "ACTIF" -> "-fx-text-fill: #10b981; -fx-font-weight: bold;";
                        case "EN_ATTENTE" -> "-fx-text-fill: #f59e0b; -fx-font-weight: bold;";
                        case "DESACTIVE" -> "-fx-text-fill: #ef4444; -fx-font-weight: bold;";
                        default -> "";
                    });
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnActiver = new Button("✓");
            private final Button btnDesactiver = new Button("✗");
            private final Button btnRole = new Button("👤");
            private final Button btnSupprimer = new Button("🗑");
            private final HBox box = new HBox(5, btnActiver, btnDesactiver, btnRole, btnSupprimer);

            {
                btnActiver.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px;");
                btnDesactiver.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px;");
                btnRole.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 11px;");
                btnSupprimer.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-size: 11px;");

                btnActiver.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    activerUtilisateur(u);
                });

                btnDesactiver.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    desactiverUtilisateur(u);
                });

                btnRole.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    changerRole(u);
                });

                btnSupprimer.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    supprimerUtilisateur(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void configurerFiltres() {
        comboRole.setItems(FXCollections.observableArrayList("Tous", "USER", "HOTE", "ADMIN"));
        comboRole.setValue("Tous");
        comboStatut.setItems(FXCollections.observableArrayList("Tous", "ACTIF", "EN_ATTENTE", "DESACTIVE"));
        comboStatut.setValue("Tous");
        
        comboRole.setOnAction(e -> appliquerFiltres());
        comboStatut.setOnAction(e -> appliquerFiltres());
        txtRecherche.textProperty().addListener((obs, old, val) -> appliquerFiltres());
    }

    private void chargerUtilisateurs() {
        try {
            users.setAll(service.afficher());
            tableUsers.setItems(users);
        } catch (SQLException e) {
            showError("Erreur chargement: " + e.getMessage());
        }
    }

    private void appliquerFiltres() {
        try {
            List<Utilisateur> all = service.afficher();
            String recherche = txtRecherche.getText().toLowerCase();
            String role = comboRole.getValue();
            String statut = comboStatut.getValue();

            ObservableList<Utilisateur> filtered = FXCollections.observableArrayList();
            for (Utilisateur u : all) {
                boolean match = (recherche.isEmpty() || 
                        u.getNom().toLowerCase().contains(recherche) ||
                        u.getPrenom().toLowerCase().contains(recherche) ||
                        u.getEmail().toLowerCase().contains(recherche))
                        && ("Tous".equals(role) || u.getRole().equals(role))
                        && ("Tous".equals(statut) || u.getStatut().equals(statut));
                if (match) filtered.add(u);
            }
            tableUsers.setItems(filtered);
            lblStats.setText("Affichés: " + filtered.size() + " / Total: " + all.size());
        } catch (SQLException e) {
            showError("Erreur filtrage: " + e.getMessage());
        }
    }

    private void activerUtilisateur(Utilisateur u) {
        try {
            service.activerCompte(u.getId());
            showSuccess("Compte activé: " + u.getNomComplet());
            chargerUtilisateurs();
            mettreAJourStatistiques();
        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void desactiverUtilisateur(Utilisateur u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Désactiver le compte de " + u.getNomComplet());
        confirm.setContentText("L'utilisateur ne pourra plus se connecter");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.desactiverCompte(u.getId());
                showSuccess("Compte désactivé");
                chargerUtilisateurs();
                mettreAJourStatistiques();
            } catch (SQLException e) {
                showError("Erreur: " + e.getMessage());
            }
        }
    }

    private void changerRole(Utilisateur u) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(u.getRole(), "USER", "HOTE", "ADMIN");
        dialog.setTitle("Changer le rôle");
        dialog.setHeaderText("Utilisateur: " + u.getNomComplet());
        dialog.setContentText("Nouveau rôle:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(role -> {
            try {
                service.changerRole(u.getId(), role);
                showSuccess("Rôle modifié: " + role);
                chargerUtilisateurs();
            } catch (SQLException e) {
                showError("Erreur: " + e.getMessage());
            }
        });
    }

    private void supprimerUtilisateur(Utilisateur u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur " + u.getNomComplet());
        confirm.setContentText("Cette action est irréversible. Toutes les réclamations associées seront également supprimées.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(u.getId());
                showSuccess("Utilisateur supprimé");
                chargerUtilisateurs();
                mettreAJourStatistiques();
            } catch (SQLException e) {
                showError("Erreur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleReinitialiser() {
        comboRole.setValue("Tous");
        comboStatut.setValue("Tous");
        txtRecherche.clear();
        chargerUtilisateurs();
    }

    private void mettreAJourStatistiques() {
        try {
            List<Utilisateur> all = service.afficher();
            long actifs = all.stream().filter(u -> "ACTIF".equals(u.getStatut())).count();
            long enAttente = all.stream().filter(u -> "EN_ATTENTE".equals(u.getStatut())).count();
            
            lblTotalUsers.setText(String.valueOf(all.size()));
            lblUsersActifs.setText(String.valueOf(actifs));
            lblUsersEnAttente.setText(String.valueOf(enAttente));
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
