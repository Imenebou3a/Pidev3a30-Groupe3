package tn.esprit.controllers.frontoffice;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.entities.KitHobbies;
import tn.esprit.services.KitHobbiesService;

import java.io.File;
import java.util.List;

/**
 * Controller Frontoffice pour l'affichage des kits hobbies
 * Interface utilisateur (consultation et recherche)
 */
public class KitFrontController {

    // ===== COMPOSANTS FXML =====
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterNiveau;
    @FXML private ComboBox<String> filterTri;
    @FXML private GridPane gridKits;
    @FXML private ScrollPane scrollPane;
    @FXML private Label lblNombreResultats;

    // ===== SERVICES =====
    private KitHobbiesService kitService;
    private List<KitHobbies> kitsCourants;

    // ===== CONSTANTES =====
    private static final int COLONNES = 3;
    private static final double CARD_WIDTH = 280;
    private static final double CARD_HEIGHT = 450;

    /**
     * Initialisation du controller
     */
    @FXML
    public void initialize() {
        kitService = new KitHobbiesService();

        initialiserFiltres();
        configurerListeners();
        chargerKits();
    }

    /**
     * Initialiser les filtres
     */
    private void initialiserFiltres() {
        // Types d'artisanat
        filterType.setItems(FXCollections.observableArrayList(
                "Tous", "Poterie", "Tissage", "Sculpture", "Peinture",
                "Calligraphie", "Bijouterie", "Mosaïque", "Ferronnerie", "Vannerie", "Broderie"
        ));
        filterType.setValue("Tous");

        // Niveaux de difficulté
        filterNiveau.setItems(FXCollections.observableArrayList(
                "Tous", "Facile", "Moyen", "Difficile"
        ));
        filterNiveau.setValue("Tous");

        // Options de tri
        filterTri.setItems(FXCollections.observableArrayList(
                "Plus récents", "Prix croissant", "Prix décroissant", "Nom A-Z", "Difficulté"
        ));
        filterTri.setValue("Plus récents");
    }

    /**
     * Configurer les listeners
     */
    private void configurerListeners() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
        filterType.setOnAction(e -> appliquerFiltres());
        filterNiveau.setOnAction(e -> appliquerFiltres());
        filterTri.setOnAction(e -> trierKits());
    }

    /**
     * Charger tous les kits
     */
    private void chargerKits() {
        kitsCourants = kitService.afficher().stream()
                .filter(k -> k.getStock() > 0)
                .toList();
        afficherKits(kitsCourants);
    }

    /**
     * Appliquer les filtres
     */
    private void appliquerFiltres() {
        List<KitHobbies> kitsFiltres = kitService.afficher();

        // Filtre par recherche
        String recherche = searchField.getText().trim();
        if (!recherche.isEmpty()) {
            kitsFiltres = kitsFiltres.stream()
                    .filter(k -> k.getNomKit().toLowerCase().contains(recherche.toLowerCase()) ||
                            k.getDescription().toLowerCase().contains(recherche.toLowerCase()))
                    .toList();
        }

        // Filtre par type
        String type = filterType.getValue();
        if (type != null && !type.equals("Tous")) {
            kitsFiltres = kitsFiltres.stream()
                    .filter(k -> k.getTypeArtisanat().equals(type))
                    .toList();
        }

        // Filtre par niveau
        String niveau = filterNiveau.getValue();
        if (niveau != null && !niveau.equals("Tous")) {
            kitsFiltres = kitsFiltres.stream()
                    .filter(k -> k.getNiveauDifficulte().equals(niveau))
                    .toList();
        }

        // Filtrer uniquement les kits en stock
        kitsFiltres = kitsFiltres.stream()
                .filter(k -> k.getStock() > 0)
                .toList();

        kitsCourants = kitsFiltres;
        trierKits();
    }

    /**
     * Trier les kits
     */
    private void trierKits() {
        String tri = filterTri.getValue();
        List<KitHobbies> kitsTries = kitsCourants;

        switch (tri) {
            case "Prix croissant":
                kitsTries = kitsCourants.stream()
                        .sorted((k1, k2) -> k1.getPrix().compareTo(k2.getPrix()))
                        .toList();
                break;
            case "Prix décroissant":
                kitsTries = kitsCourants.stream()
                        .sorted((k1, k2) -> k2.getPrix().compareTo(k1.getPrix()))
                        .toList();
                break;
            case "Nom A-Z":
                kitsTries = kitsCourants.stream()
                        .sorted((k1, k2) -> k1.getNomKit().compareTo(k2.getNomKit()))
                        .toList();
                break;
            case "Difficulté":
                kitsTries = kitsCourants.stream()
                        .sorted((k1, k2) -> {
                            int ordre1 = getOrdreDifficulte(k1.getNiveauDifficulte());
                            int ordre2 = getOrdreDifficulte(k2.getNiveauDifficulte());
                            return Integer.compare(ordre1, ordre2);
                        })
                        .toList();
                break;
        }

        afficherKits(kitsTries);
    }

    /**
     * Obtenir l'ordre de difficulté
     */
    private int getOrdreDifficulte(String niveau) {
        switch (niveau) {
            case "Facile": return 1;
            case "Moyen": return 2;
            case "Difficile": return 3;
            default: return 0;
        }
    }

    /**
     * Afficher les kits dans la grille
     */
    private void afficherKits(List<KitHobbies> kits) {
        gridKits.getChildren().clear();

        int row = 0;
        int col = 0;

        for (KitHobbies kit : kits) {
            VBox card = creerCardKit(kit);
            gridKits.add(card, col, row);

            col++;
            if (col >= COLONNES) {
                col = 0;
                row++;
            }
        }

        // Mettre à jour le nombre de résultats
        if (lblNombreResultats != null) {
            lblNombreResultats.setText(kits.size() + " kit(s) trouvé(s)");
        }
    }

    /**
     * Créer une card pour un kit
     */
    private VBox creerCardKit(KitHobbies kit) {
        VBox card = new VBox(12);
        card.getStyleClass().add("kit-card");
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15px; " +
                     "-fx-effect: dropshadow(gaussian, rgba(26,122,122,0.15), 20, 0, 0, 5); " +
                     "-fx-cursor: hand;");

        // Image du kit
        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("product-image");

        try {
            File imageFile = new File("src/main/resources/images/" + kit.getImageUrl());
            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
            } else {
                imageView.setImage(new Image("file:src/main/resources/images/default-kit.png"));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
        }

        // Nom du kit
        Label lblNom = new Label(kit.getNomKit());
        lblNom.getStyleClass().add("kit-name");
        lblNom.setWrapText(true);
        lblNom.setMaxWidth(240);

        // HBox pour badges
        HBox badgesBox = new HBox(10);
        badgesBox.setAlignment(Pos.CENTER);

        // Badge niveau de difficulté
        Label lblNiveau = new Label(kit.getNiveauDifficulte());
        switch (kit.getNiveauDifficulte().toLowerCase()) {
            case "facile":
                lblNiveau.getStyleClass().add("badge-facile");
                break;
            case "moyen":
                lblNiveau.getStyleClass().add("badge-moyen");
                break;
            case "difficile":
                lblNiveau.getStyleClass().add("badge-difficile");
                break;
        }

        // Tag type d'artisanat
        Label lblType = new Label(kit.getTypeArtisanat());
        lblType.getStyleClass().add("tag-artisanat");

        badgesBox.getChildren().addAll(lblNiveau, lblType);

        // Description courte
        String descCourte = kit.getDescription();
        if (descCourte != null && descCourte.length() > 80) {
            descCourte = descCourte.substring(0, 80) + "...";
        }
        Label lblDesc = new Label(descCourte);
        lblDesc.getStyleClass().add("kit-description");
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(240);

        // Prix
        Label lblPrix = new Label(kit.getPrix() + " TND");
        lblPrix.getStyleClass().add("kit-price");

        // Stock
        Label lblStock = new Label("Disponible: " + kit.getStock() + " kit(s)");
        if (kit.getStock() < 5) {
            lblStock.getStyleClass().add("stock-low");
        } else {
            lblStock.getStyleClass().add("stock-available");
        }

        // Boutons
        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        
        Button btnDetails = new Button("👁 Détails");
        btnDetails.setStyle("-fx-background-color: transparent; -fx-border-color: #1a7a7a; " +
                           "-fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; " +
                           "-fx-text-fill: #1a7a7a; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16;");
        btnDetails.setOnAction(e -> afficherDetails(kit));
        
        Button btnPanier = new Button("🛒 Ajouter");
        btnPanier.setStyle("-fx-background-color: #1a7a7a; -fx-text-fill: white; " +
                          "-fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand; -fx-padding: 8 16;");
        btnPanier.setDisable(kit.getStock() <= 0);
        btnPanier.setOnAction(e -> ajouterAuPanier(kit));
        
        buttonsBox.getChildren().addAll(btnDetails, btnPanier);

        card.getChildren().addAll(imageView, lblNom, badgesBox, lblDesc, lblPrix, lblStock, buttonsBox);
        
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
     * Ajouter un kit au panier
     */
    private void ajouterAuPanier(KitHobbies kit) {
        tn.esprit.utils.Panier.getInstance().ajouterKit(kit, 1);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Panier");
        alert.setHeaderText("✓ Kit ajouté !");
        alert.setContentText(kit.getNomKit() + " a été ajouté à votre panier.");
        alert.showAndWait();
    }

    /**
     * Afficher les détails d'un kit
     */
    private void afficherDetails(KitHobbies kit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du kit");
        alert.setHeaderText(kit.getNomKit());

        String details = String.format(
                "Description: %s\n\n" +
                        "Type d'artisanat: %s\n" +
                        "Niveau de difficulté: %s\n" +
                        "Prix: %.2f TND\n" +
                        "Stock disponible: %d kit(s)\n" +
                        "Produit associé: %s",
                kit.getDescription(),
                kit.getTypeArtisanat(),
                kit.getNiveauDifficulte(),
                kit.getPrix(),
                kit.getStock(),
                kit.getNomProduit() != null ? kit.getNomProduit() : "Non spécifié"
        );

        alert.setContentText(details);

        // Ajouter un bouton personnalisé
        ButtonType btnCommander = new ButtonType("Commander", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnFermer = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnCommander, btnFermer);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnCommander) {
                afficherMessageCommande(kit);
            }
        });
    }

    /**
     * Afficher un message de confirmation de commande
     */
    private void afficherMessageCommande(KitHobbies kit) {
        // Ajouter au panier
        tn.esprit.utils.Panier.getInstance().ajouterKit(kit, 1);
        
        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
        confirmation.setTitle("Panier");
        confirmation.setHeaderText("✓ Kit ajouté !");
        confirmation.setContentText("Le kit \"" + kit.getNomKit() +
                "\" a été ajouté à votre panier.");
        confirmation.showAndWait();
    }

    /**
     * Action : Réinitialiser les filtres
     */
    @FXML
    private void handleResetFiltres() {
        searchField.clear();
        filterType.setValue("Tous");
        filterNiveau.setValue("Tous");
        filterTri.setValue("Plus récents");
        chargerKits();
    }
}