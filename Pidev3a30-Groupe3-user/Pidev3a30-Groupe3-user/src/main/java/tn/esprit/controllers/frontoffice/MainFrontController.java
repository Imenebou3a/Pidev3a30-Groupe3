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

public class MainFrontController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUserNom;
    @FXML private Button btnAccueil;
    @FXML private Button btnProduits;
    @FXML private Button btnKits;
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
        int nbArticles = tn.esprit.utils.Panier.getInstance().getNombreArticles();
        if (btnPanier != null) {
            btnPanier.setText("🛒 Panier (" + nbArticles + ")");
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Panier");
        alert.setHeaderText("🛒 Votre Panier");
        
        int nbArticles = tn.esprit.utils.Panier.getInstance().getNombreArticles();
        java.math.BigDecimal total = tn.esprit.utils.Panier.getInstance().getTotal();
        
        String contenu = String.format(
            "Nombre d'articles: %d\n" +
            "Total: %.2f TND\n\n" +
            "Fonctionnalité de commande à venir...",
            nbArticles, total
        );
        
        alert.setContentText(contenu);
        alert.showAndWait();
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