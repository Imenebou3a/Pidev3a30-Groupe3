package tn.esprit.controllers.backoffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import tn.esprit.MainApp;
import tn.esprit.entities.Utilisateur;
import tn.esprit.utils.Session;

import java.io.IOException;

public class MainBackController {

    @FXML private BorderPane mainPane;
    @FXML private Label lblNomAdmin;
    @FXML private Label lblInitiales;

    @FXML private Button btnDashboard;
    @FXML private Button btnProduits;
    @FXML private Button btnKits;
    @FXML private Button btnCommandes;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnReclamations;
    @FXML private Button btnProfil;

    @FXML
    public void initialize() {
        Utilisateur admin = Session.getInstance().getUtilisateurConnecte();
        if (admin != null) {
            lblNomAdmin.setText(admin.getPrenom() + " " + admin.getNom());
            String initiales = String.valueOf(admin.getPrenom().charAt(0)).toUpperCase()
                    + String.valueOf(admin.getNom().charAt(0)).toUpperCase();
            lblInitiales.setText(initiales);
        }
        chargerDashboard();
    }

    private void chargerPage(String fxmlPath, Button btnActif) {
        // Log du chemin pour debug
        System.out.println("Chargement: " + fxmlPath);
        System.out.println("Ressource trouvée: " + getClass().getResource(fxmlPath));

        var url = getClass().getResource(fxmlPath);
        if (url == null) {
            System.err.println("FICHIER INTROUVABLE: " + fxmlPath);
            new Alert(Alert.AlertType.ERROR,
                    "Fichier introuvable :\n" + fxmlPath +
                            "\n\nVérifiez que le fichier existe dans src/main/resources").showAndWait();
            return;
        }

        try {
            // Remove active class from all sidebar buttons
            for (Button b : new Button[]{btnDashboard, btnProduits, btnKits, btnCommandes, btnUtilisateurs, btnReclamations}) {
                if (b != null) {
                    b.getStyleClass().remove("nav-btn-active");
                    if (!b.getStyleClass().contains("nav-btn"))
                        b.getStyleClass().add("nav-btn");
                }
            }
            
            // Add active class to the clicked button (if it's a sidebar button)
            if (btnActif != null) {
                btnActif.getStyleClass().add("nav-btn-active");
            }
            
            mainPane.setCenter(FXMLLoader.load(url));
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur chargement : " + e.getMessage()).showAndWait();
        }
    }

    // ── Chemins CORRIGÉS : /fxml/backoffice/ (sans double fxml) ──────────────

    @FXML private void chargerDashboard() {
        chargerPage("/fxml/backoffice/dashboard_back.fxml", btnDashboard);
    }

    @FXML private void chargerProduits() {
        chargerPage("/fxml/backoffice/produit_back.fxml", btnProduits);
    }

    @FXML private void chargerKits() {
        chargerPage("/fxml/backoffice/kit_back.fxml", btnKits);
    }

    @FXML private void chargerCommandes() {
        chargerPage("/fxml/backoffice/commandes_back.fxml", btnCommandes);
    }

    @FXML private void chargerUtilisateurs() {
        chargerPage("/fxml/backoffice/user_back.fxml", btnUtilisateurs);
    }

    @FXML private void chargerReclamations() {
        chargerPage("/fxml/backoffice/reclamation_back.fxml", btnReclamations);
    }

    @FXML private void chargerProfil() {
        chargerPage("/fxml/frontoffice/Profil.fxml", null);
    }

    @FXML private void handleDeconnexion() {
        Session.getInstance().setUtilisateurConnecte(null);
        MainApp.changeScene("/fxml/frontoffice/login.fxml", "Lammetna - Connexion");
    }
}