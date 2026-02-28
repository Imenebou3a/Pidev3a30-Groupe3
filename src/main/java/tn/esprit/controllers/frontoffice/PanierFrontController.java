package tn.esprit.controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.LignePanier;
import tn.esprit.services.PanierService;

import java.math.BigDecimal;

/**
 * Contrôleur créatif pour le panier avec animations et design moderne
 */
public class PanierFrontController {
    
    @FXML private VBox containerLignes;
    @FXML private Label lblTotal;
    @FXML private Label lblNbArticles;
    @FXML private Button btnValider;
    @FXML private Button btnVider;
    @FXML private Button btnContinuer;
    @FXML private VBox panierVide;
    @FXML private BorderPane panierPlein;
    
    private PanierService panierService;
    
    @FXML
    public void initialize() {
        panierService = PanierService.getInstance();
        rafraichirPanier();
        
        if (btnVider != null) {
            btnVider.setOnAction(e -> viderPanier());
        }
        
        if (btnValider != null) {
            btnValider.setOnAction(e -> validerCommande());
        }
        
        if (btnContinuer != null) {
            btnContinuer.setOnAction(e -> fermerPanier());
        }
    }
    
    public void rafraichirPanier() {
        containerLignes.getChildren().clear();
        
        if (panierService.getLignes().isEmpty()) {
            panierVide.setVisible(true);
            panierPlein.setVisible(false);
        } else {
            panierVide.setVisible(false);
            panierPlein.setVisible(true);
            
            for (LignePanier ligne : panierService.getLignes()) {
                containerLignes.getChildren().add(creerLignePanier(ligne));
            }
            
            mettreAJourTotaux();
        }
    }
    
    private HBox creerLignePanier(LignePanier ligne) {
        HBox box = new HBox(15);
        box.getStyleClass().add("ligne-panier");
        box.setAlignment(Pos.CENTER_LEFT);
        
        // Initiale colorée
        Label initiale = new Label(ligne.getNom().substring(0, 1).toUpperCase());
        initiale.getStyleClass().add("ligne-initiale");
        if ("KIT".equals(ligne.getTypeProduit())) {
            initiale.getStyleClass().add("ligne-initiale-kit");
        }
        
        // Infos produit
        VBox infos = new VBox(5);
        infos.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infos, Priority.ALWAYS);
        
        Label nom = new Label(ligne.getNom());
        nom.getStyleClass().add("ligne-nom");
        
        Label details = new Label(ligne.getDetails());
        details.getStyleClass().add("ligne-details");
        
        Label prix = new Label(String.format("%.2f TND", ligne.getPrixUnitaire()));
        prix.getStyleClass().add("ligne-prix-unitaire");
        
        infos.getChildren().addAll(nom, details, prix);
        
        // Contrôles quantité
        HBox controles = new HBox(8);
        controles.setAlignment(Pos.CENTER);
        
        Button btnMoins = new Button("-");
        btnMoins.getStyleClass().add("btn-quantite");
        btnMoins.setOnAction(e -> {
            panierService.modifierQuantite(ligne.getIdLigne(), ligne.getQuantite() - 1);
            rafraichirPanier();
        });
        
        Label lblQte = new Label(String.valueOf(ligne.getQuantite()));
        lblQte.getStyleClass().add("ligne-quantite");
        lblQte.setMinWidth(30);
        lblQte.setAlignment(Pos.CENTER);
        
        Button btnPlus = new Button("+");
        btnPlus.getStyleClass().add("btn-quantite");
        btnPlus.setOnAction(e -> {
            panierService.modifierQuantite(ligne.getIdLigne(), ligne.getQuantite() + 1);
            rafraichirPanier();
        });
        
        controles.getChildren().addAll(btnMoins, lblQte, btnPlus);
        
        // Sous-total
        Label sousTotal = new Label(String.format("%.2f TND", ligne.getSousTotal()));
        sousTotal.getStyleClass().add("ligne-sous-total");
        sousTotal.setMinWidth(100);
        sousTotal.setAlignment(Pos.CENTER_RIGHT);
        
        // Bouton supprimer
        Button btnSuppr = new Button("🗑");
        btnSuppr.getStyleClass().add("btn-supprimer");
        btnSuppr.setOnAction(e -> {
            panierService.supprimerLigne(ligne.getIdLigne());
            rafraichirPanier();
        });
        
        box.getChildren().addAll(initiale, infos, controles, sousTotal, btnSuppr);
        return box;
    }
    
    private void mettreAJourTotaux() {
        lblTotal.setText(String.format("%.2f TND", panierService.getTotal()));
        lblNbArticles.setText(panierService.getNombreArticles() + " article(s)");
    }
    
    @FXML
    private void viderPanier() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Vider le panier");
        confirm.setHeaderText("Êtes-vous sûr ?");
        confirm.setContentText("Tous les articles seront supprimés du panier.");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            panierService.viderPanier();
            rafraichirPanier();
        }
    }
    
    @FXML
    private void validerCommande() {
        if (panierService.getLignes().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Panier vide");
            alert.setHeaderText("Impossible de valider");
            alert.setContentText("Votre panier est vide. Ajoutez des articles avant de valider.");
            alert.showAndWait();
            return;
        }
        
        // Ouvrir la page de paiement
        ouvrirPaiement();
    }
    
    @FXML
    private void fermerPanier() {
        Stage stage = (Stage) btnContinuer.getScene().getWindow();
        stage.close();
    }
    
    private void ouvrirPaiement() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/frontoffice/paiement.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Paiement Sécurisé");
            stage.setScene(scene);
            
            // Fermer le panier actuel
            Stage currentStage = (Stage) btnValider.getScene().getWindow();
            currentStage.close();
            
            // Ouvrir la page de paiement
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Erreur ouverture paiement: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la page de paiement");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
