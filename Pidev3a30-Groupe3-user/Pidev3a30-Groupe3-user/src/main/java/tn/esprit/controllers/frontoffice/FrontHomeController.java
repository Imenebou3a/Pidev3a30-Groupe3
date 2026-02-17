package tn.esprit.controllers.frontoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.KitHobbies;
import tn.esprit.entities.ProduitLocal;
import tn.esprit.services.KitHobbiesService;
import tn.esprit.services.ProduitLocalService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class FrontHomeController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboCategorie, comboRegion, comboType, comboNiveau, comboTri;
    @FXML private Slider sliderPrix;
    @FXML private Label lblPrixMax;
    @FXML private FlowPane gridProduits;
    @FXML private VBox panelDetail;
    @FXML private Label lblDetailNom, lblDetailCategorie, lblDetailPrix, lblDetailStock, lblDetailDescription;
    @FXML private Button btnProduits, btnKits, btnRetourAdmin;
    @FXML private VBox filtresProduits, filtresKits;

    private ProduitLocalService produitService;
    private KitHobbiesService kitService;
    private ObservableList<ProduitLocal> produits;
    private ObservableList<KitHobbies> kits;
    private boolean modeProduits = true;

    @FXML
    public void initialize() {
        try {
            produitService = new ProduitLocalService();
            kitService = new KitHobbiesService();

            configurerFiltres();
            chargerProduits();
            afficherProduits();

            // Bouton retour admin
            if (btnRetourAdmin != null) {
                btnRetourAdmin.setOnAction(e -> retourAdmin());
            }

        } catch (Exception e) {
            System.err.println("Erreur init front: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void retourAdmin() {
        try {
            Stage stage = (Stage) btnRetourAdmin.getScene().getWindow();

            // Recharger le MainApp pour avoir la structure complète
            stage.close();
            Stage newStage = new Stage();
            tn.esprit.MainApp app = new tn.esprit.MainApp();
            app.start(newStage);

        } catch (Exception e) {
            System.err.println("Erreur retour admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configurerFiltres() {
        comboCategorie.setItems(FXCollections.observableArrayList(
                "Toutes", "Artisanat", "Textile", "Poterie", "Bijoux", "Alimentation"
        ));
        comboCategorie.setValue("Toutes");

        comboRegion.setItems(FXCollections.observableArrayList(
                "Toutes", "Tunis", "Sfax", "Sousse", "Kairouan", "Nabeul", "Djerba"
        ));
        comboRegion.setValue("Toutes");

        comboType.setItems(FXCollections.observableArrayList(
                "Tous", "Poterie", "Tissage", "Broderie", "Sculpture", "Peinture"
        ));
        comboType.setValue("Tous");

        comboNiveau.setItems(FXCollections.observableArrayList(
                "Tous", "Facile", "Intermédiaire", "Difficile"
        ));
        comboNiveau.setValue("Tous");

        comboTri.setItems(FXCollections.observableArrayList(
                "Nom A-Z", "Prix croissant", "Prix décroissant", "Stock"
        ));
        comboTri.setValue("Nom A-Z");

        sliderPrix.setMin(0);
        sliderPrix.setMax(1000);
        sliderPrix.setValue(1000);
        lblPrixMax.setText("1000 TND");

        sliderPrix.valueProperty().addListener((obs, old, val) -> {
            lblPrixMax.setText(String.format("%.0f TND", val.doubleValue()));
            appliquerFiltres();
        });

        txtRecherche.textProperty().addListener((obs, old, val) -> appliquerFiltres());
        comboCategorie.setOnAction(e -> appliquerFiltres());
        comboRegion.setOnAction(e -> appliquerFiltres());
        comboType.setOnAction(e -> appliquerFiltres());
        comboNiveau.setOnAction(e -> appliquerFiltres());
        comboTri.setOnAction(e -> appliquerFiltres());

        btnProduits.setOnAction(e -> {
            modeProduits = true;
            filtresProduits.setVisible(true);
            filtresKits.setVisible(false);
            btnProduits.getStyleClass().add("onglet-actif");
            btnKits.getStyleClass().remove("onglet-actif");
            afficherProduits();
        });

        btnKits.setOnAction(e -> {
            modeProduits = false;
            filtresProduits.setVisible(false);
            filtresKits.setVisible(true);
            btnKits.getStyleClass().add("onglet-actif");
            btnProduits.getStyleClass().remove("onglet-actif");
            afficherKits();
        });
    }

    private void chargerProduits() {
        List<ProduitLocal> liste = produitService.afficher();
        produits = FXCollections.observableArrayList(liste);
    }

    private void chargerKits() {
        List<KitHobbies> liste = kitService.afficher();
        kits = FXCollections.observableArrayList(liste);
    }

    private void afficherProduits() {
        if (produits == null) chargerProduits();
        gridProduits.getChildren().clear();

        List<ProduitLocal> filtres = produits.stream()
                .filter(p -> txtRecherche.getText().isEmpty() ||
                        p.getNom().toLowerCase().contains(txtRecherche.getText().toLowerCase()))
                .filter(p -> "Toutes".equals(comboCategorie.getValue()) ||
                        p.getCategorie().equals(comboCategorie.getValue()))
                .filter(p -> "Toutes".equals(comboRegion.getValue()) ||
                        p.getRegion().equals(comboRegion.getValue()))
                .filter(p -> p.getPrix().doubleValue() <= sliderPrix.getValue())
                .collect(Collectors.toList());

        trierListe(filtres);

        for (ProduitLocal p : filtres) {
            gridProduits.getChildren().add(creerCarteProduit(p));
        }
    }

    private void afficherKits() {
        if (kits == null) chargerKits();
        gridProduits.getChildren().clear();

        List<KitHobbies> filtres = kits.stream()
                .filter(k -> txtRecherche.getText().isEmpty() ||
                        k.getNomKit().toLowerCase().contains(txtRecherche.getText().toLowerCase()))
                .filter(k -> "Tous".equals(comboType.getValue()) ||
                        k.getTypeArtisanat().equals(comboType.getValue()))
                .filter(k -> "Tous".equals(comboNiveau.getValue()) ||
                        k.getNiveauDifficulte().equals(comboNiveau.getValue()))
                .filter(k -> k.getPrix().doubleValue() <= sliderPrix.getValue())
                .collect(Collectors.toList());

        for (KitHobbies k : filtres) {
            gridProduits.getChildren().add(creerCarteKit(k));
        }
    }

    private VBox creerCarteProduit(ProduitLocal p) {
        VBox carte = new VBox(8);
        carte.getStyleClass().add("carte-produit");
        carte.setPrefSize(240, 300);

        Label initiale = new Label(p.getNom().substring(0, 1).toUpperCase());
        initiale.getStyleClass().add("carte-initiale");

        Label nom = new Label(p.getNom());
        nom.getStyleClass().add("carte-nom");
        nom.setWrapText(true);

        HBox badges = new HBox(6);
        Label badgeCat = new Label(p.getCategorie());
        badgeCat.getStyleClass().addAll("badge", "badge-categorie");
        Label badgeReg = new Label(p.getRegion());
        badgeReg.getStyleClass().addAll("badge", "badge-region");
        badges.getChildren().addAll(badgeCat, badgeReg);

        Label prix = new Label(String.format("%.2f TND", p.getPrix()));
        prix.getStyleClass().add("carte-prix");

        HBox stockBox = new HBox(4);
        Label stockLabel = new Label("Stock: ");
        stockLabel.getStyleClass().add("carte-stock-label");
        Label stockVal = new Label(String.valueOf(p.getStock()));
        stockVal.getStyleClass().add(p.getStock() > 10 ? "stock-ok" : "stock-faible");
        stockBox.getChildren().addAll(stockLabel, stockVal);

        Button btnDetail = new Button("Voir détail");
        btnDetail.getStyleClass().add("btn-detail");
        btnDetail.setOnAction(e -> afficherDetailProduit(p));

        carte.getChildren().addAll(initiale, nom, badges, prix, stockBox, btnDetail);
        return carte;
    }

    private VBox creerCarteKit(KitHobbies k) {
        VBox carte = new VBox(8);
        carte.getStyleClass().add("carte-kit");
        carte.setPrefSize(240, 300);

        Label initiale = new Label(k.getNomKit().substring(0, 1).toUpperCase());
        initiale.getStyleClass().add("carte-initiale-kit");

        Label nom = new Label(k.getNomKit());
        nom.getStyleClass().add("carte-nom");
        nom.setWrapText(true);

        HBox badges = new HBox(6);
        Label badgeType = new Label(k.getTypeArtisanat());
        badgeType.getStyleClass().addAll("badge", "badge-type");
        Label badgeNiv = new Label(k.getNiveauDifficulte());
        badgeNiv.getStyleClass().addAll("badge", "badge-niveau-" + k.getNiveauDifficulte().toLowerCase());
        badges.getChildren().addAll(badgeType, badgeNiv);

        Label prix = new Label(String.format("%.2f TND", k.getPrix()));
        prix.getStyleClass().add("carte-prix");

        Button btnDetail = new Button("Voir détail");
        btnDetail.getStyleClass().add("btn-detail");
        btnDetail.setOnAction(e -> afficherDetailKit(k));

        carte.getChildren().addAll(initiale, nom, badges, prix, btnDetail);
        return carte;
    }

    private void afficherDetailProduit(ProduitLocal p) {
        panelDetail.setVisible(true);
        lblDetailNom.setText(p.getNom());
        lblDetailCategorie.setText(p.getCategorie() + " • " + p.getRegion());
        lblDetailPrix.setText(String.format("%.2f TND", p.getPrix()));
        lblDetailStock.setText("Stock: " + p.getStock());
        lblDetailDescription.setText(p.getDescription());
    }

    private void afficherDetailKit(KitHobbies k) {
        panelDetail.setVisible(true);
        lblDetailNom.setText(k.getNomKit());
        lblDetailCategorie.setText(k.getTypeArtisanat() + " • " + k.getNiveauDifficulte());
        lblDetailPrix.setText(String.format("%.2f TND", k.getPrix()));
        lblDetailStock.setText("Stock: " + k.getStock());
        lblDetailDescription.setText(k.getDescription());
    }

    @FXML
    private void fermerDetail() {
        panelDetail.setVisible(false);
    }

    private void appliquerFiltres() {
        if (modeProduits) {
            afficherProduits();
        } else {
            afficherKits();
        }
    }

    private void trierListe(List<ProduitLocal> liste) {
        String tri = comboTri.getValue();
        switch (tri) {
            case "Nom A-Z":
                liste.sort((a, b) -> a.getNom().compareTo(b.getNom()));
                break;
            case "Prix croissant":
                liste.sort((a, b) -> a.getPrix().compareTo(b.getPrix()));
                break;
            case "Prix décroissant":
                liste.sort((a, b) -> b.getPrix().compareTo(a.getPrix()));
                break;
            case "Stock":
                liste.sort((a, b) -> Integer.compare(b.getStock(), a.getStock()));
                break;
        }
    }
}