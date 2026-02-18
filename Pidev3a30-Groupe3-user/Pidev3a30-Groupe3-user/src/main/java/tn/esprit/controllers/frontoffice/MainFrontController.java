package tn.esprit.controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import tn.esprit.MainApp;
import tn.esprit.entities.Utilisateur;
import tn.esprit.utils.Session;

/**
 * Contrôleur principal du front office
 * Version: 2.0 - Avec panier persistant
 */
public class MainFrontController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUserNom;
    @FXML private Label lblBadgePanier;
    @FXML private Button btnAccueil;
    @FXML private Button btnProduits;
    @FXML private Button btnKits;
    @FXML private Button btnEvenements;
    @FXML private Button btnHebergement;
    @FXML private Button btnForum;
    @FXML private Button btnReclamations;
    @FXML private Button btnPanier;

    private Button btnActif;

    @FXML
    public void initialize() {
        Utilisateur user = Session.getInstance().getUtilisateurConnecte();
        if (user != null) {
            lblUserNom.setText("Bienvenue, " + user.getPrenom() + " " + user.getNom());
        }
        // Charger l'accueil par défaut
        afficherAccueil();
        
        // Mettre à jour le compteur panier
        mettreAJourPanier();
    }
    
    private void mettreAJourPanier() {
        int nbArticles = tn.esprit.services.PanierService.getInstance().getNombreArticles();
        
        if (lblBadgePanier != null) {
            if (nbArticles > 0) {
                lblBadgePanier.setText(String.valueOf(nbArticles));
                lblBadgePanier.setVisible(true);
            } else {
                lblBadgePanier.setVisible(false);
            }
        }
    }

    @FXML
    public void afficherAccueil() {
        activerBouton(btnAccueil);
        chargerFXML("/fxml/frontoffice/home_front.fxml");
    }

    @FXML
    public void afficherProduits() {
        activerBouton(btnProduits);
        chargerFXML("/fxml/frontoffice/produits_front.fxml");
        mettreAJourPanier();
    }

    @FXML
    public void afficherKits() {
        activerBouton(btnKits);
        chargerFXML("/fxml/frontoffice/kits_front.fxml");
        mettreAJourPanier();
    }

    @FXML
    public void afficherEvenements() {
        activerBouton(btnEvenements);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Événements & Participation");
        alert.setHeaderText("Module en cours de développement");
        alert.setContentText("La section Événements & Participation sera bientôt disponible.");
        alert.showAndWait();
        mettreAJourPanier();
    }

    @FXML
    public void afficherHebergement() {
        activerBouton(btnHebergement);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hébergement & Réservation");
        alert.setHeaderText("Module en cours de développement");
        alert.setContentText("La section Hébergement & Réservation sera bientôt disponible.");
        alert.showAndWait();
        mettreAJourPanier();
    }

    @FXML
    public void afficherForum() {
        activerBouton(btnForum);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forum & Discussion");
        alert.setHeaderText("Module en cours de développement");
        alert.setContentText("La section Forum & Discussion sera bientôt disponible.");
        alert.showAndWait();
        mettreAJourPanier();
    }

    @FXML
    public void afficherReclamations() {
        activerBouton(btnReclamations);
        chargerFXML("/fxml/frontoffice/reclamations_content.fxml");
        mettreAJourPanier();
    }

    @FXML
    public void afficherProfil() {
        // Désactiver tous les boutons de navigation
        if (btnActif != null) {
            btnActif.getStyleClass().remove("front-nav-btn-active");
            btnActif = null;
        }
        // Changer de scène pour le profil (car il a sa propre sidebar)
        MainApp.changeScene("/fxml/frontoffice/Profil.fxml", "Lammetna - Mon Profil");
    }
    
    @FXML
    public void afficherPanier() {
        System.out.println("DEBUG: Méthode afficherPanier() appelée - Version 2.0");
        try {
            // Ouvrir le panier dans une nouvelle fenêtre
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/frontoffice/panier.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Mon Panier");
            stage.setScene(scene);
            stage.show();
            
            // Mettre à jour le compteur après fermeture
            stage.setOnHidden(e -> mettreAJourPanier());
            
        } catch (Exception e) {
            System.err.println("Erreur ouverture panier: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir le panier");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleDeconnexion() {
        Session.getInstance().deconnecter();
        MainApp.changeScene("/fxml/frontoffice/login.fxml", "Lammetna - Connexion");
    }

    private void chargerFXML(String chemin) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(chemin));
            
            // Ajouter une transition fade
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(150), contentArea);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().setAll(node);
                
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), contentArea);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            
            fadeOut.play();
        } catch (Exception e) {
            System.err.println("Erreur chargement FXML " + chemin + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void activerBouton(Button btn) {
        if (btnActif != null) btnActif.getStyleClass().remove("front-nav-btn-active");
        btnActif = btn;
        if (btn != null) btn.getStyleClass().add("front-nav-btn-active");
    }
}