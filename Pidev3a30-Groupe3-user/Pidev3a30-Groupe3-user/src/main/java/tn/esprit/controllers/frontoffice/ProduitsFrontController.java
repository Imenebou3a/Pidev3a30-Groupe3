package tn.esprit.controllers.frontoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.entities.ProduitLocal;
import tn.esprit.services.ProduitLocalService;

import java.io.File;
import java.util.List;

/**
 * Controller Frontoffice pour l'affichage des produits locaux
 * Interface utilisateur (consultation et recherche)
 */
public class ProduitsFrontController {

    // ===== COMPOSANTS FXML =====
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategorie;
    @FXML private ComboBox<String> filterRegion;
    @FXML private ComboBox<String> filterTri;
    @FXML private GridPane gridProduits;
    @FXML private ScrollPane scrollPane;
    @FXML private Label lblNombreResultats;

    // ===== SERVICES =====
    private ProduitLocalService produitService;
    private List<ProduitLocal> produitsCourants;

    // ===== CONSTANTES =====
    private static final int COLONNES = 3;
    private static final double CARD_WIDTH = 280;
    private static final double CARD_HEIGHT = 400;

    /**
     * Initialisation du controller
     */
    @FXML
    public void initialize() {
        produitService = new ProduitLocalService();

        initialiserFiltres();
        configurerListeners();
        chargerProduits();
    }

    /**
     * Initialiser les filtres
     */
    private void initialiserFiltres() {
        // Catégories
        filterCategorie.setItems(FXCollections.observableArrayList(
                "Toutes", "Artisanat", "Gastronomie", "Textile", "Décoration", "Bijoux", "Cosmétiques"
        ));
        filterCategorie.setValue("Toutes");

        // Régions
        filterRegion.setItems(FXCollections.observableArrayList(
                "Toutes", "Tunis", "Nabeul", "Kairouan", "Sousse", "Sfax", "Bizerte"
        ));
        filterRegion.setValue("Toutes");

        // Options de tri
        filterTri.setItems(FXCollections.observableArrayList(
                "Plus récents", "Prix croissant", "Prix décroissant", "Nom A-Z", "Nom Z-A"
        ));
        filterTri.setValue("Plus récents");
    }

    /**
     * Configurer les listeners
     */
    private void configurerListeners() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
        filterCategorie.setOnAction(e -> appliquerFiltres());
        filterRegion.setOnAction(e -> appliquerFiltres());
        filterTri.setOnAction(e -> trierProduits());
    }

    /**
     * Charger tous les produits
     */
    private void chargerProduits() {
        produitsCourants = produitService.afficher().stream()
                .filter(p -> p.getStock() > 0)
                .toList();
        afficherProduits(produitsCourants);
    }

    /**
     * Appliquer les filtres
     */
    private void appliquerFiltres() {
        List<ProduitLocal> produitsFiltres = produitService.afficher();

        // Filtre par recherche
        String recherche = searchField.getText().trim();
        if (!recherche.isEmpty()) {
            produitsFiltres = produitsFiltres.stream()
                    .filter(p -> p.getNom().toLowerCase().contains(recherche.toLowerCase()) ||
                            p.getDescription().toLowerCase().contains(recherche.toLowerCase()))
                    .toList();
        }

        // Filtre par catégorie
        String categorie = filterCategorie.getValue();
        if (categorie != null && !categorie.equals("Toutes")) {
            produitsFiltres = produitsFiltres.stream()
                    .filter(p -> p.getCategorie().equals(categorie))
                    .toList();
        }

        // Filtre par région
        String region = filterRegion.getValue();
        if (region != null && !region.equals("Toutes")) {
            produitsFiltres = produitsFiltres.stream()
                    .filter(p -> p.getRegion().equals(region))
                    .toList();
        }

        // Filtrer uniquement les produits en stock
        produitsFiltres = produitsFiltres.stream()
                .filter(p -> p.getStock() > 0)
                .toList();

        produitsCourants = produitsFiltres;
        trierProduits();
    }

    /**
     * Trier les produits
     */
    private void trierProduits() {
        String tri = filterTri.getValue();
        List<ProduitLocal> produitsTries = produitsCourants;

        switch (tri) {
            case "Prix croissant":
                produitsTries = produitsCourants.stream()
                        .sorted((p1, p2) -> p1.getPrix().compareTo(p2.getPrix()))
                        .toList();
                break;
            case "Prix décroissant":
                produitsTries = produitsCourants.stream()
                        .sorted((p1, p2) -> p2.getPrix().compareTo(p1.getPrix()))
                        .toList();
                break;
            case "Nom A-Z":
                produitsTries = produitsCourants.stream()
                        .sorted((p1, p2) -> p1.getNom().compareTo(p2.getNom()))
                        .toList();
                break;
            case "Nom Z-A":
                produitsTries = produitsCourants.stream()
                        .sorted((p1, p2) -> p2.getNom().compareTo(p1.getNom()))
                        .toList();
                break;
        }

        afficherProduits(produitsTries);
    }

    /**
     * Afficher les produits dans la grille
     */
    private void afficherProduits(List<ProduitLocal> produits) {
        gridProduits.getChildren().clear();

        int row = 0;
        int col = 0;

        for (ProduitLocal produit : produits) {
            VBox card = creerCardProduit(produit);
            gridProduits.add(card, col, row);

            col++;
            if (col >= COLONNES) {
                col = 0;
                row++;
            }
        }

        // Mettre à jour le nombre de résultats
        if (lblNombreResultats != null) {
            lblNombreResultats.setText(produits.size() + " produit(s) trouvé(s)");
        }
    }

    /**
     * Créer une card pour un produit
     */
    private VBox creerCardProduit(ProduitLocal produit) {
        VBox card = new VBox(15);
        card.getStyleClass().add("product-card");
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15px; " +
                     "-fx-effect: dropshadow(gaussian, rgba(26,122,122,0.15), 20, 0, 0, 5); " +
                     "-fx-cursor: hand;");

        // Image du produit
        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 10px;");

        try {
            File imageFile = new File("src/main/resources/images/" + produit.getImageUrl());
            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
            } else {
                // Placeholder avec emoji
                Label placeholder = new Label("🖼️");
                placeholder.setStyle("-fx-font-size: 60px;");
                VBox imgBox = new VBox(placeholder);
                imgBox.setAlignment(Pos.CENTER);
                imgBox.setPrefSize(240, 180);
                imgBox.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 10px;");
                card.getChildren().add(imgBox);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
        }

        if (imageView.getImage() != null) {
            card.getChildren().add(imageView);
        }

        // Nom du produit
        Label lblNom = new Label(produit.getNom());
        lblNom.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a7a7a; " +
                       "-fx-wrap-text: true; -fx-text-alignment: center;");
        lblNom.setMaxWidth(240);
        lblNom.setWrapText(true);

        // Catégorie et région
        HBox infoBox = new HBox(8);
        infoBox.setAlignment(Pos.CENTER);
        Label lblCategorie = new Label(produit.getCategorie());
        lblCategorie.setStyle("-fx-background-color: #e0f2f1; -fx-text-fill: #1a7a7a; " +
                             "-fx-padding: 4 10; -fx-background-radius: 12px; -fx-font-size: 11px;");
        Label lblRegion = new Label("📍 " + produit.getRegion());
        lblRegion.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        infoBox.getChildren().addAll(lblCategorie, lblRegion);

        // Prix
        Label lblPrix = new Label(String.format("%.2f TND", produit.getPrix()));
        lblPrix.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a7a7a;");

        // Stock
        Label lblStock = new Label("En stock: " + produit.getStock());
        lblStock.setStyle("-fx-font-size: 12px; -fx-text-fill: " + 
                         (produit.getStock() < 5 ? "#ef4444" : "#10b981") + "; -fx-font-weight: 600;");

        // Boutons
        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        
        Button btnDetails = new Button("👁 Détails");
        btnDetails.setStyle("-fx-background-color: transparent; -fx-border-color: #1a7a7a; " +
                           "-fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; " +
                           "-fx-text-fill: #1a7a7a; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16;");
        btnDetails.setOnAction(e -> afficherDetails(produit));
        
        Button btnPanier = new Button("🛒 Ajouter");
        btnPanier.setStyle("-fx-background-color: #1a7a7a; -fx-text-fill: white; " +
                          "-fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand; -fx-padding: 8 16;");
        btnPanier.setDisable(produit.getStock() <= 0);
        btnPanier.setOnAction(e -> ajouterAuPanier(produit));
        
        buttonsBox.getChildren().addAll(btnDetails, btnPanier);

        card.getChildren().addAll(lblNom, infoBox, lblPrix, lblStock, buttonsBox);
        
        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 15px; " +
            "-fx-effect: dropshadow(gaussian, rgba(26,122,122,0.3), 25, 0, 0, 8); " +
            "-fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 15px; " +
            "-fx-effect: dropshadow(gaussian, rgba(26,122,122,0.15), 20, 0, 0, 5); " +
            "-fx-cursor: hand;"
        ));
        
        return card;
    }

    /**
     * Ajouter un produit au panier
     */
    private void ajouterAuPanier(ProduitLocal produit) {
        tn.esprit.services.PanierService panier = tn.esprit.services.PanierService.getInstance();
        
        panier.ajouterProduit(
            produit.getIdProduit(),
            produit.getNom(),
            produit.getPrix(),
            produit.getCategorie() + " • " + produit.getRegion(),
            produit.getImageUrl()
        );
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Panier");
        alert.setHeaderText("✓ Produit ajouté !");
        alert.setContentText(produit.getNom() + " a été ajouté à votre panier.\n\n" +
                           "Articles dans le panier: " + panier.getNombreArticles());
        alert.showAndWait();
    }

    /**
     * Afficher les détails d'un produit
     */
    private void afficherDetails(ProduitLocal produit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du produit");
        alert.setHeaderText(produit.getNom());

        String details = String.format(
                "Description: %s\n\n" +
                        "Catégorie: %s\n" +
                        "Région: %s\n" +
                        "Prix: %.2f TND\n" +
                        "Stock disponible: %d unités",
                produit.getDescription(),
                produit.getCategorie(),
                produit.getRegion(),
                produit.getPrix(),
                produit.getStock()
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    /**
     * Action : Réinitialiser les filtres
     */
    @FXML
    private void handleResetFiltres() {
        searchField.clear();
        filterCategorie.setValue("Toutes");
        filterRegion.setValue("Toutes");
        filterTri.setValue("Plus récents");
        chargerProduits();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
