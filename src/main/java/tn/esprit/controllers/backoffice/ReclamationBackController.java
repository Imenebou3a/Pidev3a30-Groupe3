package tn.esprit.controllers.backoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Reclamation;
import tn.esprit.services.ServiceReclamation;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ReclamationBackController {

    @FXML private TableView<Reclamation> tableReclamations;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colSujet, colCategorie, colStatut, colPriorite, colUtilisateur;
    @FXML private TableColumn<Reclamation, LocalDateTime> colDate;
    
    @FXML private ComboBox<String> comboStatut, comboCategorie, comboPriorite;
    @FXML private TextField txtRecherche;
    @FXML private Label lblStats, lblEnAttente, lblEnCours, lblResolues;
    
    @FXML private TextArea txtDescription, txtReponse;
    @FXML private Label lblDetailsId, lblDetailsSujet, lblDetailsUtilisateur, lblDetailsDate;
    @FXML private ComboBox<String> comboNouveauStatut;
    @FXML private Button btnRepondre;

    private ServiceReclamation service = new ServiceReclamation();
    private ObservableList<Reclamation> reclamations = FXCollections.observableArrayList();
    private Reclamation reclamationSelectionnee;

    @FXML
    public void initialize() {
        configurerTableau();
        configurerFiltres();
        chargerReclamations();
        mettreAJourStatistiques();
    }

    private void configurerTableau() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSujet.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colUtilisateur.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        
        colDate.setCellFactory(col -> new TableCell<>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date));
            }
        });

        tableReclamations.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) afficherDetails(newVal);
        });
    }

    private void configurerFiltres() {
        comboStatut.setItems(FXCollections.observableArrayList("Tous", "EN_ATTENTE", "EN_COURS", "RESOLUE", "REJETEE"));
        comboStatut.setValue("Tous");
        comboCategorie.setItems(FXCollections.observableArrayList("Tous", "TECHNIQUE", "SERVICE", "HEBERGEMENT", "EVENEMENT", "AUTRE"));
        comboCategorie.setValue("Tous");
        comboPriorite.setItems(FXCollections.observableArrayList("Tous", "BASSE", "MOYENNE", "HAUTE"));
        comboPriorite.setValue("Tous");
        
        comboNouveauStatut.setItems(FXCollections.observableArrayList("EN_ATTENTE", "EN_COURS", "RESOLUE", "REJETEE"));
        
        comboStatut.setOnAction(e -> appliquerFiltres());
        comboCategorie.setOnAction(e -> appliquerFiltres());
        comboPriorite.setOnAction(e -> appliquerFiltres());
        txtRecherche.textProperty().addListener((obs, old, val) -> appliquerFiltres());
    }

    private void chargerReclamations() {
        try {
            reclamations.setAll(service.getAll());
            tableReclamations.setItems(reclamations);
        } catch (SQLException e) {
            showError("Erreur chargement: " + e.getMessage());
        }
    }

    private void appliquerFiltres() {
        try {
            List<Reclamation> all = service.getAll();
            String recherche = txtRecherche.getText().toLowerCase();
            String statut = comboStatut.getValue();
            String categorie = comboCategorie.getValue();
            String priorite = comboPriorite.getValue();

            ObservableList<Reclamation> filtered = FXCollections.observableArrayList();
            for (Reclamation r : all) {
                boolean match = (recherche.isEmpty() || r.getSujet().toLowerCase().contains(recherche))
                        && ("Tous".equals(statut) || r.getStatut().equals(statut))
                        && ("Tous".equals(categorie) || r.getCategorie().equals(categorie))
                        && ("Tous".equals(priorite) || r.getPriorite().equals(priorite));
                if (match) filtered.add(r);
            }
            tableReclamations.setItems(filtered);
            lblStats.setText("Affichées: " + filtered.size() + " / Total: " + all.size());
        } catch (SQLException e) {
            showError("Erreur filtrage: " + e.getMessage());
        }
    }

    private void afficherDetails(Reclamation r) {
        reclamationSelectionnee = r;
        lblDetailsId.setText("#" + r.getId());
        lblDetailsSujet.setText(r.getSujet());
        lblDetailsUtilisateur.setText(r.getNomUtilisateur());
        lblDetailsDate.setText(r.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtDescription.setText(r.getDescription());
        txtReponse.setText(r.getReponseAdmin() != null ? r.getReponseAdmin() : "");
        comboNouveauStatut.setValue(r.getStatut());
    }

    @FXML
    private void handleRepondre() {
        if (reclamationSelectionnee == null) {
            showError("Sélectionnez une réclamation");
            return;
        }

        String reponse = txtReponse.getText().trim();
        String statut = comboNouveauStatut.getValue();

        if (reponse.isEmpty()) {
            showError("Veuillez saisir une réponse");
            return;
        }

        try {
            service.repondre(reclamationSelectionnee.getId(), reponse, statut);
            showSuccess("Réponse enregistrée avec succès");
            chargerReclamations();
            mettreAJourStatistiques();
        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        if (reclamationSelectionnee == null) {
            showError("Sélectionnez une réclamation");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la réclamation #" + reclamationSelectionnee.getId());
        confirm.setContentText("Cette action est irréversible");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(reclamationSelectionnee.getId());
                showSuccess("Réclamation supprimée");
                chargerReclamations();
                mettreAJourStatistiques();
                reclamationSelectionnee = null;
            } catch (SQLException e) {
                showError("Erreur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleReinitialiser() {
        comboStatut.setValue("Tous");
        comboCategorie.setValue("Tous");
        comboPriorite.setValue("Tous");
        txtRecherche.clear();
        chargerReclamations();
    }

    private void mettreAJourStatistiques() {
        try {
            int enAttente = service.countByStatut("EN_ATTENTE");
            int enCours = service.countByStatut("EN_COURS");
            int resolues = service.countByStatut("RESOLUE");
            
            lblEnAttente.setText(String.valueOf(enAttente));
            lblEnCours.setText(String.valueOf(enCours));
            lblResolues.setText(String.valueOf(resolues));
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
