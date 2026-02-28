package tn.esprit.controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;

import tn.esprit.entities.Favori;
import tn.esprit.entities.ProduitLocal;
import tn.esprit.entities.KitHobbies;
import tn.esprit.services.FavoriService;
import tn.esprit.services.ProduitLocalService;
import tn.esprit.services.KitHobbiesService;
import tn.esprit.utils.SessionManager;

import java.util.List;
import java.util.Optional;

public class FavorisFrontController {

    @FXML private VBox gridFavoris;
    @FXML private ScrollPane scrollFavoris;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private Label lblNombreFavoris;
    @FXML private Label lblMessage;
    @FXML private VBox boxVide;

    private FavoriService favoriService;
    private ProduitLocalService produitService;
    private KitHobbiesService kitService;
    private int idUtilisateur;

    @FXML
    public void initialize() {
        favoriService = new FavoriService();
        produitService = new ProduitLocalService();
        kitService = new KitHobbiesService();
        
        // Récupérer l'utilisateur connecté
        idUtilisateur = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        
        // Configurer le filtre
        comboFiltre.setValue("Tous");
        comboFiltre.setOnAction(e -> appliquerFiltre());
        
        // Charger les favoris
        chargerFavoris();
    }

    @FXML
    private void actualiserFavoris() {
        chargerFavoris();
        afficherMessage("✓ Favoris actualisés", "success");
    }

    private void chargerFavoris() {
        try {
            List<Favori> favoris = favoriService.getFavorisByUtilisateur(idUtilisateur);
            
            if (favoris.isEmpty()) {
                afficherVide(true);
                lblNombreFavoris.setText("0 favoris");
                return;
            }
            
            afficherVide(false);
            lblNombreFavoris.setText(favoris.size() + " favori" + (favoris.size() > 1 ? "s" : ""));
            
            afficherFavorisEnCartes(favoris);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement favoris: " + e.getMessage());
            afficherMessage("Erreur lors du chargement des favoris", "error");
        }
    }

    private void appliquerFiltre() {
        String filtre = comboFiltre.getValue();
        List<Favori> favoris = favoriService.getFavorisByUtilisateur(idUtilisateur);
        
        if ("Produits".equals(filtre)) {
            favoris = favoris.stream()
                .filter(f -> "produit".equals(f.getTypeFavori()))
                .toList();
        } else if ("Kits".equals(filtre)) {
            favoris = favoris.stream()
                .filter(f -> "kit".equals(f.getTypeFavori()))
                .toList();
        }
        
        afficherFavorisEnCartes(favoris);
        lblNombreFavoris.setText(favoris.size() + " favori" + (favoris.size() > 1 ? "s" : ""));
    }

    private void afficherFavorisEnCartes(List<Favori> favoris) {
        gridFavoris.getChildren().clear();
        
        for (Favori fav : favoris) {
            VBox carte = creerCarteFavori(fav);
            if (carte != null) {
                gridFavoris.getChildren().add(carte);
            }
        }
    }

    private VBox creerCarteFavori(Favori favori) {
        VBox card = new VBox(10);
        card.getStyleClass().add("favori-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(240);

        try {
            if ("produit".equals(favori.getTypeFavori())) {
                ProduitLocal produit = produitService.getById(favori.getIdItem());
                if (produit != null) {
                    return creerCarteProduit(produit, favori);
                }
            } else if ("kit".equals(favori.getTypeFavori())) {
                KitHobbies kit = kitService.getById(favori.getIdItem());
                if (kit != null) {
                    return creerCarteKit(kit, favori);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur création carte: " + e.getMessage());
        }
        
        return null;
    }

    private VBox creerCarteProduit(ProduitLocal produit, Favori favori) {
        HBox card = new HBox(15);
        card.getStyleClass().add("favori-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefHeight(100);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("favori-image");
        
        try {
            if (produit.getImageUrl() != null && !produit.getImageUrl().isEmpty()) {
                java.io.File imageFile = new java.io.File("src/main/resources" + produit.getImageUrl());
                if (imageFile.exists()) {
                    imageView.setImage(new Image(imageFile.toURI().toString()));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur image: " + e.getMessage());
        }

        // Infos
        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        
        Label lblType = new Label("📦 PRODUIT");
        lblType.getStyleClass().add("favori-type-badge");
        
        Label lblNom = new Label(produit.getNom());
        lblNom.getStyleClass().add("favori-nom");
        lblNom.setMaxWidth(350);
        
        Label lblInfo = new Label(produit.getCategorie() + " • " + produit.getRegion());
        lblInfo.getStyleClass().add("favori-info");
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        
        Label lblPrix = new Label(String.format("%.2f TND", produit.getPrix()));
        lblPrix.getStyleClass().add("favori-prix");

        infoBox.getChildren().addAll(lblType, lblNom, lblInfo, lblPrix);

        // Boutons
        VBox btnBox = new VBox(8);
        btnBox.setAlignment(Pos.CENTER);
        
        Button btnVoir = new Button("👁️ Voir");
        btnVoir.getStyleClass().add("btn-voir");
        btnVoir.setPrefWidth(100);
        btnVoir.setOnAction(e -> voirProduit(produit));

        Button btnRetirer = new Button("🗑️ Retirer");
        btnRetirer.getStyleClass().add("btn-retirer");
        btnRetirer.setPrefWidth(100);
        btnRetirer.setOnAction(e -> retirerFavori(favori));

        btnBox.getChildren().addAll(btnVoir, btnRetirer);

        card.getChildren().addAll(imageView, infoBox, btnBox);
        
        VBox wrapper = new VBox(card);
        return wrapper;
    }

    private VBox creerCarteKit(KitHobbies kit, Favori favori) {
        HBox card = new HBox(15);
        card.getStyleClass().add("favori-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefHeight(100);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("favori-image");
        
        try {
            if (kit.getImageUrl() != null && !kit.getImageUrl().isEmpty()) {
                java.io.File imageFile = new java.io.File("src/main/resources" + kit.getImageUrl());
                if (imageFile.exists()) {
                    imageView.setImage(new Image(imageFile.toURI().toString()));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur image: " + e.getMessage());
        }

        // Infos
        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        
        Label lblType = new Label("🎨 KIT");
        lblType.getStyleClass().add("favori-type-badge");
        
        Label lblNom = new Label(kit.getNomKit());
        lblNom.getStyleClass().add("favori-nom");
        lblNom.setMaxWidth(350);
        
        Label lblInfo = new Label(kit.getTypeArtisanat() + " • " + kit.getNiveauDifficulte());
        lblInfo.getStyleClass().add("favori-info");
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        
        Label lblPrix = new Label(String.format("%.2f TND", kit.getPrix()));
        lblPrix.getStyleClass().add("favori-prix");

        infoBox.getChildren().addAll(lblType, lblNom, lblInfo, lblPrix);

        // Boutons
        VBox btnBox = new VBox(8);
        btnBox.setAlignment(Pos.CENTER);
        
        Button btnVoir = new Button("👁️ Voir");
        btnVoir.getStyleClass().add("btn-voir");
        btnVoir.setPrefWidth(100);
        btnVoir.setOnAction(e -> voirKit(kit));

        Button btnRetirer = new Button("🗑️ Retirer");
        btnRetirer.getStyleClass().add("btn-retirer");
        btnRetirer.setPrefWidth(100);
        btnRetirer.setOnAction(e -> retirerFavori(favori));

        btnBox.getChildren().addAll(btnVoir, btnRetirer);

        card.getChildren().addAll(imageView, infoBox, btnBox);
        
        VBox wrapper = new VBox(card);
        return wrapper;
    }

    private void retirerFavori(Favori favori) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Retirer des favoris");
        confirm.setContentText("Voulez-vous vraiment retirer cet élément de vos favoris ?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                favoriService.retirerFavori(favori.getIdUtilisateur(), favori.getTypeFavori(), favori.getIdItem());
                afficherMessage("✓ Retiré des favoris", "success");
                chargerFavoris();
            } catch (Exception e) {
                afficherMessage("❌ Erreur lors de la suppression", "error");
            }
        }
    }

    @FXML
    private void supprimerTousFavoris() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("⚠️ Confirmation");
        confirm.setHeaderText("Supprimer tous les favoris");
        confirm.setContentText("Cette action est irréversible. Continuer ?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                List<Favori> favoris = favoriService.getFavorisByUtilisateur(idUtilisateur);
                for (Favori fav : favoris) {
                    favoriService.retirerFavori(fav.getIdUtilisateur(), fav.getTypeFavori(), fav.getIdItem());
                }
                afficherMessage("✓ Tous les favoris ont été supprimés", "success");
                chargerFavoris();
            } catch (Exception e) {
                afficherMessage("❌ Erreur lors de la suppression", "error");
            }
        }
    }

    @FXML
    private void allerProduits() {
        // Navigation vers la page produits
        System.out.println("Navigation vers produits...");
    }

    private void voirProduit(ProduitLocal produit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Produit");
        alert.setHeaderText(produit.getNom());
        alert.setContentText(String.format(
            "Catégorie: %s\nRégion: %s\nPrix: %.2f TND\nStock: %d\n\n%s",
            produit.getCategorie(), produit.getRegion(), produit.getPrix(),
            produit.getStock(), produit.getDescription()
        ));
        alert.showAndWait();
    }

    private void voirKit(KitHobbies kit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Kit");
        alert.setHeaderText(kit.getNomKit());
        alert.setContentText(String.format(
            "Type: %s\nNiveau: %s\nPrix: %.2f TND\nStock: %d\n\n%s",
            kit.getTypeArtisanat(), kit.getNiveauDifficulte(), kit.getPrix(),
            kit.getStock(), kit.getDescription()
        ));
        alert.showAndWait();
    }

    private void afficherVide(boolean visible) {
        boxVide.setVisible(visible);
        boxVide.setManaged(visible);
        scrollFavoris.setVisible(!visible);
        scrollFavoris.setManaged(!visible);
    }
    
    @FXML
    private void fermerFavoris() {
        gridFavoris.getScene().getWindow().hide();
    }

    private void afficherMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.setVisible(true);
        lblMessage.getStyleClass().removeAll("message-success", "message-error");
        lblMessage.getStyleClass().add("message-" + type);
        
        // Masquer après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> lblMessage.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
