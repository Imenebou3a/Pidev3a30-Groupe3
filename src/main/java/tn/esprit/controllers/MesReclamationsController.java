package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.esprit.MainApp;
import tn.esprit.entities.Reclamation;
import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceReclamation;
import tn.esprit.utils.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MesReclamationsController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalReclamationsLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label enCoursLabel;
    @FXML private Label resoluesLabel;

    @FXML private ComboBox<String> statutFilter;
    @FXML private ComboBox<String> categorieFilter;

    @FXML private TableView<Reclamation> reclamationsTable;
    @FXML private TableColumn<Reclamation, Integer> idColumn;
    @FXML private TableColumn<Reclamation, String> sujetColumn;
    @FXML private TableColumn<Reclamation, String> categorieColumn;
    @FXML private TableColumn<Reclamation, String> statutColumn;
    @FXML private TableColumn<Reclamation, String> prioriteColumn;
    @FXML private TableColumn<Reclamation, LocalDateTime> dateColumn;
    @FXML private TableColumn<Reclamation, Void> actionsColumn;

    private ServiceReclamation serviceReclamation = new ServiceReclamation();
    private ObservableList<Reclamation> reclamationsList = FXCollections.observableArrayList();
    private Utilisateur currentUser;

    @FXML
    private void initialize() {
        currentUser = Session.getInstance().getUtilisateurConnecte();
        if (currentUser != null) {
            welcomeLabel.setText(currentUser.getNomComplet());
        }

        // Configurer les ComboBox
        statutFilter.setItems(FXCollections.observableArrayList("Tous", "EN_ATTENTE", "EN_COURS", "RESOLUE", "REJETEE"));
        statutFilter.setValue("Tous");

        categorieFilter.setItems(FXCollections.observableArrayList("Tous", "TECHNIQUE", "SERVICE", "HEBERGEMENT", "EVENEMENT", "AUTRE"));
        categorieFilter.setValue("Tous");

        // Configurer les colonnes du tableau
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        sujetColumn.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        prioriteColumn.setCellValueFactory(new PropertyValueFactory<>("priorite"));

        // Formatter la date
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        dateColumn.setCellFactory(column -> new TableCell<Reclamation, LocalDateTime>() {
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
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("👁 Voir");
            private final Button btnSupprimer = new Button("🗑 Supprimer");

            {
                btnVoir.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                btnSupprimer.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");

                btnVoir.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    voirReclamation(reclamation);
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
                    buttons.getChildren().addAll(btnVoir, btnSupprimer);
                    setGraphic(buttons);
                }
            }
        });

        // Charger les données
        loadData();
        updateStatistics();

        // Listeners pour les filtres
        statutFilter.setOnAction(e -> handleFilter());
        categorieFilter.setOnAction(e -> handleFilter());
    }

    private void loadData() {
        try {
            if (currentUser != null) {
                List<Reclamation> reclamations = serviceReclamation.getReclamationsByUser(currentUser.getId());
                reclamationsList.clear();
                reclamationsList.addAll(reclamations);
                reclamationsTable.setItems(reclamationsList);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement : " + e.getMessage());
        }
    }

    private void updateStatistics() {
        try {
            if (currentUser != null) {
                List<Reclamation> all = serviceReclamation.getReclamationsByUser(currentUser.getId());
                int total = all.size();
                int enAttente = (int) all.stream().filter(r -> "EN_ATTENTE".equals(r.getStatut())).count();
                int enCours = (int) all.stream().filter(r -> "EN_COURS".equals(r.getStatut())).count();
                int resolues = (int) all.stream().filter(r -> "RESOLUE".equals(r.getStatut())).count();

                totalReclamationsLabel.setText(String.valueOf(total));
                enAttenteLabel.setText(String.valueOf(enAttente));
                enCoursLabel.setText(String.valueOf(enCours));
                resoluesLabel.setText(String.valueOf(resolues));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilter() {
        String statutSelected = statutFilter.getValue();
        String categorieSelected = categorieFilter.getValue();

        try {
            List<Reclamation> all = serviceReclamation.getReclamationsByUser(currentUser.getId());
            ObservableList<Reclamation> filtered = FXCollections.observableArrayList();

            for (Reclamation r : all) {
                boolean matchStatut = "Tous".equals(statutSelected) || r.getStatut().equals(statutSelected);
                boolean matchCategorie = "Tous".equals(categorieSelected) || r.getCategorie().equals(categorieSelected);

                if (matchStatut && matchCategorie) {
                    filtered.add(r);
                }
            }

            reclamationsTable.setItems(filtered);
        } catch (SQLException e) {
            showError("Erreur lors du filtrage : " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        statutFilter.setValue("Tous");
        categorieFilter.setValue("Tous");
        loadData();
    }

    @FXML
    private void handleAddReclamation() {
        MainApp.changeScene("/fxml/AddReclamation.fxml", "Lammetna - Nouvelle Réclamation");
    }

    private void voirReclamation(Reclamation reclamation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la réclamation");
        alert.setHeaderText("Réclamation #" + reclamation.getId() + " - " + reclamation.getSujet());

        StringBuilder content = new StringBuilder();
        content.append("Catégorie: ").append(reclamation.getCategorie()).append("\n");
        content.append("Statut: ").append(reclamation.getStatut()).append("\n");
        content.append("Priorité: ").append(reclamation.getPriorite()).append("\n\n");
        content.append("Description:\n").append(reclamation.getDescription()).append("\n");

        if (reclamation.getReponseAdmin() != null && !reclamation.getReponseAdmin().isEmpty()) {
            content.append("\n--- Réponse de l'administrateur ---\n");
            content.append(reclamation.getReponseAdmin());
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
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
                loadData();
                updateStatistics();
            } catch (SQLException e) {
                showError("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("/fxml/Dashboard.fxml", "Lammetna - Dashboard");
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