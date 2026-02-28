package tn.esprit.controllers.backoffice;
import tn.esprit.entities.KitHobbies;
import tn.esprit.entities.ProduitLocal;
import tn.esprit.services.KitHobbiesService;
import tn.esprit.services.ProduitLocalService;
import tn.esprit.utils.ImageUploadUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class KitBackController {

    // ===== TABPANE =====
    @FXML private TabPane tabPane;

    // ===== TAB 1 - LISTE =====
    @FXML private FlowPane gridKits;
    @FXML private TextField        txtRecherche;
    @FXML private ComboBox<String> comboType;
    @FXML private ComboBox<String> comboNiveau;
    @FXML private Label            lblStats;

    // ===== TAB 2 - AJOUTER =====
    @FXML private TextField        txtAjoutNom;
    @FXML private TextArea         txtAjoutDescription;
    @FXML private TextField        txtAjoutPrix;
    @FXML private ComboBox<String> comboAjoutType;
    @FXML private ComboBox<String> comboAjoutNiveau;
    @FXML private TextField        txtAjoutStock;
    @FXML private TextField        txtAjoutDuree;
    @FXML private TextArea         txtAjoutMateriaux;
    @FXML private ComboBox<ProduitLocal> comboAjoutProduit; // FK
    @FXML private Label            lblMessageGlobal;
    @FXML private Label            lblErreurNom;
    @FXML private Label            lblErreurType;
    @FXML private Label            lblErreurNiveau;
    @FXML private Label            lblErreurPrix;
    @FXML private Label            lblErreurDuree;
    @FXML private Label            lblErreurStock;
    @FXML private Label            lblErreurUrl;
    @FXML private Label            lblErreurProduit; // FK
    @FXML private Label            lblErreurDescription;
    @FXML private Label            lblErreurMateriaux;
    @FXML private Label            lblCompteurDescription;
    @FXML private Label            lblCompteurMateriaux;
    @FXML private ImageView        imgAjoutPreview;
    @FXML private Button           btnAjoutUploadImage;
    @FXML private Label            lblAjoutImageNom;

    // ===== TAB 3 - MODIFIER =====
    @FXML private ComboBox<KitHobbies>   comboModifierKit;
    @FXML private TextField        txtModifId;
    @FXML private TextField        txtModifNom;
    @FXML private ComboBox<String> comboModifType;
    @FXML private ComboBox<String> comboModifNiveau;
    @FXML private TextField        txtModifPrix;
    @FXML private TextField        txtModifDuree;
    @FXML private TextField        txtModifStock;
    @FXML private TextArea         txtModifDescription;
    @FXML private TextArea         txtModifMateriaux;
    @FXML private ComboBox<ProduitLocal> comboModifProduit; // FK
    @FXML private Label            lblMessageModif;
    @FXML private ImageView        imgModifPreview;
    @FXML private Button           btnModifUploadImage;
    @FXML private Label            lblModifImageNom;

    // ===== TAB 4 - SUPPRIMER =====
    @FXML private ComboBox<KitHobbies> comboSupprimerKit;
    @FXML private VBox  detailsSuppressionBox;
    @FXML private Label lblSupprNom;
    @FXML private Label lblSupprType;
    @FXML private Label lblSupprPrix;
    @FXML private Label lblSupprStock;
    @FXML private Label lblSupprProduit; // nouveau
    @FXML private Label lblMessageSuppr;

    // ===== STATS =====
    @FXML private Label lblTotalKits;
    @FXML private Label lblKitsFaciles;
    @FXML private Label lblKitsDifficiles;

    // ===== SERVICES =====
    private KitHobbiesService   kitService;
    private ProduitLocalService produitService;
    private ObservableList<ProduitLocal> produitsObs;
    
    // Chemins des images uploadées
    private String ajoutImagePath = null;
    private String modifImagePath = null;

    private static final ObservableList<String> TYPES = FXCollections.observableArrayList(
            "Poterie","Tissage","Sculpture","Peinture","Calligraphie",
            "Bijouterie","Mosaique","Ferronnerie","Vannerie","Broderie");
    private static final ObservableList<String> NIVEAUX = FXCollections.observableArrayList(
            "Facile","Moyen","Difficile");

    // ─────────────────────────────────────────────
    //  INIT
    // ─────────────────────────────────────────────
    @FXML
    public void initialize() {
        try {
            kitService     = new KitHobbiesService();
            produitService = new ProduitLocalService();

            chargerProduits();
            initialiserComboBox();
            chargerKits();
            mettreAJourStatistiques();
            configurerValidationTempsReel();
        } catch (Exception e) {
            System.err.println("Erreur init KitBackController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ─────────────────────────────────────────────
    //  UPLOAD D'IMAGES
    // ─────────────────────────────────────────────
    @FXML
    private void choisirImageAjout() {
        Stage stage = (Stage) btnAjoutUploadImage.getScene().getWindow();
        File selectedFile = ImageUploadUtil.choisirImage(stage);
        
        if (selectedFile != null) {
            String erreur = ImageUploadUtil.validateImage(selectedFile);
            if (erreur != null) {
                lblErreurUrl.setText(erreur);
                lblErreurUrl.setVisible(true);
                lblErreurUrl.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }
            
            String uploadedPath = ImageUploadUtil.uploadImage(selectedFile, "kit");
            if (uploadedPath != null) {
                ajoutImagePath = uploadedPath;
                lblAjoutImageNom.setText(selectedFile.getName());
                lblAjoutImageNom.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                
                try {
                    File imageFile = new File("src/main/resources" + uploadedPath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        if (imgAjoutPreview != null) {
                            imgAjoutPreview.setImage(image);
                            imgAjoutPreview.setVisible(true);
                        }
                    }
                    lblErreurUrl.setText("✓ Image uploadée avec succès");
                    lblErreurUrl.setVisible(true);
                    lblErreurUrl.setStyle("-fx-text-fill: #27ae60;");
                } catch (Exception e) {
                    System.err.println("Erreur aperçu: " + e.getMessage());
                }
            }
        }
    }
    
    @FXML
    private void choisirImageModif() {
        Stage stage = (Stage) btnModifUploadImage.getScene().getWindow();
        File selectedFile = ImageUploadUtil.choisirImage(stage);
        
        if (selectedFile != null) {
            String erreur = ImageUploadUtil.validateImage(selectedFile);
            if (erreur != null) {
                afficherMessage(lblMessageModif, erreur, false);
                return;
            }
            
            String uploadedPath = ImageUploadUtil.uploadImage(selectedFile, "kit");
            if (uploadedPath != null) {
                modifImagePath = uploadedPath;
                lblModifImageNom.setText(selectedFile.getName());
                lblModifImageNom.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                
                try {
                    File imageFile = new File("src/main/resources" + uploadedPath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        if (imgModifPreview != null) {
                            imgModifPreview.setImage(image);
                            imgModifPreview.setVisible(true);
                        }
                    }
                    afficherMessage(lblMessageModif, "✓ Image uploadée", true);
                } catch (Exception e) {
                    System.err.println("Erreur aperçu: " + e.getMessage());
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    //  CHARGEMENT PRODUITS (réutilisé dans les 3 combos FK)
    // ─────────────────────────────────────────────
    private void chargerProduits() {
        List<ProduitLocal> produits = produitService.afficher();
        produitsObs = FXCollections.observableArrayList(produits);
    }

    /** Applique le style d'affichage "Nom (Region)" sur une ComboBox<ProduitLocal> */
    private void styliserComboProduit(ComboBox<ProduitLocal> combo) {
        combo.setItems(produitsObs);
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(ProduitLocal p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null
                        : "#" + p.getIdProduit() + " — " + p.getNom() + " (" + p.getRegion() + ")");
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(ProduitLocal p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "Selectionner le produit associe"
                        : "#" + p.getIdProduit() + " — " + p.getNom() + " (" + p.getRegion() + ")");
            }
        });
    }

    // ─────────────────────────────────────────────
    //  COMBO BOX
    // ─────────────────────────────────────────────
    private void initialiserComboBox() {
        // Filtres Tab 1
        comboType.setItems(FXCollections.observableArrayList("Tous"));
        comboType.getItems().addAll(TYPES);
        comboType.setValue("Tous");
        comboNiveau.setItems(FXCollections.observableArrayList("Tous"));
        comboNiveau.getItems().addAll(NIVEAUX);
        comboNiveau.setValue("Tous");

        // Formulaire Tab 2
        comboAjoutType.setItems(TYPES);
        comboAjoutNiveau.setItems(NIVEAUX);
        styliserComboProduit(comboAjoutProduit);

        // Formulaire Tab 3
        comboModifType.setItems(TYPES);
        comboModifNiveau.setItems(NIVEAUX);
        styliserComboProduit(comboModifProduit);

        // Kits pour modifier/supprimer
        chargerCombosKits();

        comboType.setOnAction(e -> appliquerFiltres());
        comboNiveau.setOnAction(e -> appliquerFiltres());
    }

    private void chargerCombosKits() {
        ObservableList<KitHobbies> obs = FXCollections.observableArrayList(kitService.afficher());
        for (ComboBox<KitHobbies> cb : new ComboBox[]{comboModifierKit, comboSupprimerKit}) {
            if (cb == null) continue;
            cb.setItems(obs);
            cb.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(KitHobbies k, boolean empty) {
                    super.updateItem(k, empty);
                    setText(empty || k == null ? null : "#" + k.getIdKit() + " — " + k.getNomKit());
                }
            });
            cb.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(KitHobbies k, boolean empty) {
                    super.updateItem(k, empty);
                    setText(empty || k == null ? "Choisir un kit..." : "#" + k.getIdKit() + " — " + k.getNomKit());
                }
            });
        }
    }

    // ─────────────────────────────────────────────
    //  VALIDATION TEMPS RÉEL
    // ─────────────────────────────────────────────
    private void configurerValidationTempsReel() {
        txtAjoutNom.textProperty().addListener((o, ov, nv) -> {
            if (nv.trim().isEmpty()) setEtat(txtAjoutNom, lblErreurNom, false, "Nom obligatoire *");
            else if (nv.trim().length() < 3) setEtat(txtAjoutNom, lblErreurNom, false, "Minimum 3 caracteres");
            else setEtat(txtAjoutNom, lblErreurNom, true, null);
        });
        txtAjoutPrix.textProperty().addListener((o, ov, nv) -> {
            if (nv.trim().isEmpty()) { setEtat(txtAjoutPrix, lblErreurPrix, false, "Prix obligatoire *"); return; }
            try {
                setEtat(txtAjoutPrix, lblErreurPrix,
                        new BigDecimal(nv.trim()).compareTo(BigDecimal.ZERO) > 0, "Prix doit etre > 0");
            } catch (NumberFormatException e) { setEtat(txtAjoutPrix, lblErreurPrix, false, "Nombre invalide"); }
        });
        txtAjoutStock.textProperty().addListener((o, ov, nv) -> {
            if (nv.trim().isEmpty()) { setEtat(txtAjoutStock, lblErreurStock, false, "Stock obligatoire *"); return; }
            try {
                setEtat(txtAjoutStock, lblErreurStock,
                        Integer.parseInt(nv.trim()) >= 0, "Stock ne peut pas etre negatif");
            } catch (NumberFormatException e) { setEtat(txtAjoutStock, lblErreurStock, false, "Entier requis"); }
        });
        comboAjoutType.valueProperty().addListener((o, ov, nv) ->
                setEtat(null, lblErreurType, nv != null, "Type obligatoire *"));
        comboAjoutNiveau.valueProperty().addListener((o, ov, nv) ->
                setEtat(null, lblErreurNiveau, nv != null, "Niveau obligatoire *"));
        comboAjoutProduit.valueProperty().addListener((o, ov, nv) ->
                setEtat(null, lblErreurProduit, nv != null, "Produit associe obligatoire *"));

        // Compteurs
        if (lblCompteurDescription != null)
            txtAjoutDescription.textProperty().addListener((o, ov, nv) -> {
                lblCompteurDescription.setText(nv.length() + "/1000");
                lblCompteurDescription.setStyle(nv.length() > 900
                        ? "-fx-text-fill:#D94F1E;-fx-font-weight:bold;" : "-fx-text-fill:#8a8a8a;");
            });
        if (lblCompteurMateriaux != null)
            txtAjoutMateriaux.textProperty().addListener((o, ov, nv) -> {
                lblCompteurMateriaux.setText(nv.length() + "/500");
                lblCompteurMateriaux.setStyle(nv.length() > 450
                        ? "-fx-text-fill:#D94F1E;-fx-font-weight:bold;" : "-fx-text-fill:#8a8a8a;");
            });

        // Tab 3 feedback
        txtModifNom.textProperty().addListener((o, ov, nv) -> surlignerChamp(txtModifNom, !nv.trim().isEmpty()));
        txtModifPrix.textProperty().addListener((o, ov, nv) -> {
            try { surlignerChamp(txtModifPrix, !nv.trim().isEmpty() && new BigDecimal(nv.trim()).compareTo(BigDecimal.ZERO) > 0); }
            catch (NumberFormatException e) { surlignerChamp(txtModifPrix, false); }
        });
        txtModifStock.textProperty().addListener((o, ov, nv) -> {
            try { surlignerChamp(txtModifStock, !nv.trim().isEmpty() && Integer.parseInt(nv.trim()) >= 0); }
            catch (NumberFormatException e) { surlignerChamp(txtModifStock, false); }
        });
    }

    // ─────────────────────────────────────────────
    //  TAB 1 — LISTE
    // ─────────────────────────────────────────────
    private void chargerKits() {
        chargerKitsEnCartes(FXCollections.observableArrayList(kitService.afficher()));
    }

    private void chargerKitsEnCartes(ObservableList<KitHobbies> liste) {
        gridKits.getChildren().clear();
        for (KitHobbies k : liste) {
            gridKits.getChildren().add(creerCarteKit(k));
        }
    }

    private VBox creerCarteKit(KitHobbies kit) {
        VBox card = new VBox(10);
        card.getStyleClass().add("kit-card");
        card.setAlignment(Pos.TOP_CENTER);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("kit-image");
        
        try {
            if (kit.getImageUrl() != null && !kit.getImageUrl().isEmpty()) {
                imageView.setImage(new Image(kit.getImageUrl(), true));
            } else {
                imageView.setImage(new Image("https://via.placeholder.com/220x150?text=Pas+d'image", true));
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/220x150?text=Erreur", true));
        }

        // Nom
        Label lblNom = new Label(kit.getNomKit());
        lblNom.getStyleClass().add("kit-nom");
        lblNom.setWrapText(true);
        lblNom.setMaxWidth(220);

        // Type
        Label lblType = new Label(kit.getTypeArtisanat());
        lblType.getStyleClass().add("kit-type");

        // Difficulté avec badge coloré
        Label lblDifficulte = new Label(kit.getNiveauDifficulte());
        lblDifficulte.getStyleClass().add("kit-difficulte");
        switch (kit.getNiveauDifficulte()) {
            case "Facile" -> lblDifficulte.getStyleClass().add("difficulte-facile");
            case "Moyen" -> lblDifficulte.getStyleClass().add("difficulte-moyen");
            case "Difficile" -> lblDifficulte.getStyleClass().add("difficulte-difficile");
        }

        // Prix
        Label lblPrix = new Label(String.format("%.2f TND", kit.getPrix()));
        lblPrix.getStyleClass().add("kit-prix");

        // Stock avec indicateur coloré
        String stockText;
        String stockStyle;
        if (kit.getStock() == 0) {
            stockText = "🔴 Rupture";
            stockStyle = "stock-rupture";
        } else if (kit.getStock() < 5) {
            stockText = "🟠 Stock: " + kit.getStock();
            stockStyle = "stock-faible";
        } else {
            stockText = "🟢 Stock: " + kit.getStock();
            stockStyle = "stock-ok";
        }
        Label lblStock = new Label(stockText);
        lblStock.getStyleClass().addAll("kit-stock", stockStyle);

        // Produit associé
        String nomProduit = produitsObs.stream()
                .filter(p -> p.getIdProduit() == kit.getIdProduit())
                .map(p -> "📦 " + p.getNom())
                .findFirst().orElse("📦 Produit inconnu");
        Label lblProduit = new Label(nomProduit);
        lblProduit.getStyleClass().add("kit-type");
        lblProduit.setWrapText(true);
        lblProduit.setMaxWidth(220);

        // Boutons d'action
        Button btnModif = new Button("✏️ Modifier");
        btnModif.getStyleClass().addAll("btn-card", "btn-modifier");
        btnModif.setOnAction(e -> {
            // Changer vers l'onglet "Modifier" (index 2)
            if (tabPane != null) {
                tabPane.getSelectionModel().select(2);
            }
            comboModifierKit.setValue(kit);
            chargerKitPourModification();
        });
        btnModif.setPrefWidth(100);

        Button btnSuppr = new Button("🗑️ Supprimer");
        btnSuppr.getStyleClass().addAll("btn-card", "btn-supprimer");
        btnSuppr.setOnAction(e -> {
            comboSupprimerKit.setValue(kit);
            afficherDetailsKitASupprimer();
            supprimerKit();
        });
        btnSuppr.setPrefWidth(100);

        HBox actions = new HBox(5, btnModif, btnSuppr);
        actions.setAlignment(Pos.CENTER);
        actions.getStyleClass().add("card-actions");

        card.getChildren().addAll(imageView, lblNom, lblType, lblDifficulte, lblPrix, lblStock, lblProduit, actions);
        return card;
    }

    private void afficherDetailsKit(KitHobbies kit) {
        String nomProduit = produitsObs.stream()
                .filter(p -> p.getIdProduit() == kit.getIdProduit())
                .map(p -> p.getNom() + " (" + p.getRegion() + ")")
                .findFirst().orElse("Produit inconnu");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Kit");
        alert.setHeaderText(kit.getNomKit());
        
        String details = String.format(
            "ID: %d\n" +
            "Type: %s\n" +
            "Niveau: %s\n" +
            "Prix: %.2f TND\n" +
            "Stock: %d unités\n" +
            "Produit associé: %s\n\n" +
            "Description:\n%s",
            kit.getIdKit(),
            kit.getTypeArtisanat(),
            kit.getNiveauDifficulte(),
            kit.getPrix(),
            kit.getStock(),
            nomProduit,
            kit.getDescription() != null ? kit.getDescription() : "Aucune description"
        );
        
        alert.setContentText(details);
        alert.showAndWait();
    }

    @FXML private void reinitialiserFiltres() {
        txtRecherche.clear();
        comboType.setValue("Tous");
        comboNiveau.setValue("Tous");
        chargerKits();
    }

    private void appliquerFiltres() {
        String q  = txtRecherche.getText().trim().toLowerCase();
        String tp = comboType.getValue();
        String nv = comboNiveau.getValue();
        chargerKitsEnCartes(FXCollections.observableArrayList(
                kitService.afficher().stream()
                        .filter(k -> q.isEmpty() || k.getNomKit().toLowerCase().contains(q))
                        .filter(k -> tp == null || tp.equals("Tous") || tp.equals(k.getTypeArtisanat()))
                        .filter(k -> nv == null || nv.equals("Tous") || nv.equals(k.getNiveauDifficulte()))
                        .toList()));
    }

    @FXML private void exporterCSV() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Exporter en CSV");
            fc.setInitialFileName("kits_hobbies.csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            File file = fc.showSaveDialog(gridKits.getScene().getWindow());
            if (file == null) return;
            try (PrintWriter w = new PrintWriter(file, "UTF-8")) {
                w.println("ID,Nom,Type,Niveau,Prix,Stock,Produit Associe,Description");
                for (KitHobbies k : kitService.afficher()) {
                    String nomProduit = produitsObs.stream()
                            .filter(p -> p.getIdProduit() == k.getIdProduit())
                            .map(ProduitLocal::getNom).findFirst().orElse("");
                    w.printf("%d,\"%s\",\"%s\",\"%s\",%.2f,%d,\"%s\",\"%s\"%n",
                            k.getIdKit(), k.getNomKit(), k.getTypeArtisanat(),
                            k.getNiveauDifficulte(), k.getPrix(), k.getStock(),
                            nomProduit,
                            k.getDescription() != null ? k.getDescription().replace("\"","\"\"") : "");
                }
            }
            alerteInfo("Export reussi", "Fichier exporte avec succes !");
        } catch (Exception e) { alerteErreur("Erreur export", e.getMessage()); }
    }

    // ─────────────────────────────────────────────
    //  TAB 2 — AJOUTER
    // ─────────────────────────────────────────────
    @FXML private void ajouterKit() {
        // Déclencher les listeners
        txtAjoutNom.setText(txtAjoutNom.getText());
        txtAjoutPrix.setText(txtAjoutPrix.getText());
        txtAjoutStock.setText(txtAjoutStock.getText());
        comboAjoutType.setValue(comboAjoutType.getValue());
        comboAjoutNiveau.setValue(comboAjoutNiveau.getValue());
        comboAjoutProduit.setValue(comboAjoutProduit.getValue());

        if (!validerFormAjout()) return;

        try {
            KitHobbies kit = new KitHobbies(
                    txtAjoutNom.getText().trim(),
                    txtAjoutDescription.getText().trim(),
                    comboAjoutType.getValue(),
                    comboAjoutNiveau.getValue(),
                    new BigDecimal(txtAjoutPrix.getText().trim()),
                    Integer.parseInt(txtAjoutStock.getText().trim()),
                    ajoutImagePath != null ? ajoutImagePath : "",
                    comboAjoutProduit.getValue().getIdProduit()  // FK réelle
            );
            kitService.ajouter(kit);
            afficherMessage(lblMessageGlobal, "✓ Kit ajoute avec succes !", true);
            reinitialiserFormulaireAjout();
            chargerKits();
            chargerCombosKits();
            mettreAJourStatistiques();
        } catch (Exception e) {
            afficherMessage(lblMessageGlobal, "✗ Erreur : " + e.getMessage(), false);
        }
    }

    @FXML private void reinitialiserFormulaireAjout() {
        txtAjoutNom.clear(); txtAjoutDescription.clear();
        txtAjoutPrix.clear(); txtAjoutStock.clear();
        txtAjoutDuree.clear();
        txtAjoutMateriaux.clear();
        comboAjoutType.setValue(null);
        comboAjoutNiveau.setValue(null);
        comboAjoutProduit.setValue(null);
        
        // Réinitialiser l'image
        ajoutImagePath = null;
        if (lblAjoutImageNom != null) {
            lblAjoutImageNom.setText("Aucune image sélectionnée");
            lblAjoutImageNom.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        }
        if (imgAjoutPreview != null) {
            imgAjoutPreview.setImage(null);
            imgAjoutPreview.setVisible(false);
        }
        
        for (TextField tf : new TextField[]{txtAjoutNom, txtAjoutPrix, txtAjoutStock, txtAjoutDuree})
            tf.setStyle("");
        for (Label l : new Label[]{lblErreurNom, lblErreurType, lblErreurNiveau,
                lblErreurPrix, lblErreurStock, lblErreurDuree, lblErreurUrl,
                lblErreurProduit, lblErreurDescription, lblErreurMateriaux})
            if (l != null) { l.setText(""); l.setVisible(false); }
        if (lblMessageGlobal != null) lblMessageGlobal.setVisible(false);
    }

    private boolean validerFormAjout() {
        boolean ok = true;
        if (txtAjoutNom.getText().trim().isEmpty() || txtAjoutNom.getText().trim().length() < 3) {
            setEtat(txtAjoutNom, lblErreurNom, false, "Nom obligatoire, min 3 caracteres *"); ok = false;
        }
        if (comboAjoutType.getValue() == null) {
            setEtat(null, lblErreurType, false, "Type obligatoire *"); ok = false;
        }
        if (comboAjoutNiveau.getValue() == null) {
            setEtat(null, lblErreurNiveau, false, "Niveau obligatoire *"); ok = false;
        }
        if (txtAjoutPrix.getText().trim().isEmpty()) {
            setEtat(txtAjoutPrix, lblErreurPrix, false, "Prix obligatoire *"); ok = false;
        } else {
            try {
                if (new BigDecimal(txtAjoutPrix.getText().trim()).compareTo(BigDecimal.ZERO) <= 0) {
                    setEtat(txtAjoutPrix, lblErreurPrix, false, "Prix doit etre > 0"); ok = false;
                }
            } catch (NumberFormatException e) {
                setEtat(txtAjoutPrix, lblErreurPrix, false, "Nombre invalide"); ok = false;
            }
        }
        if (txtAjoutStock.getText().trim().isEmpty()) {
            setEtat(txtAjoutStock, lblErreurStock, false, "Stock obligatoire *"); ok = false;
        } else {
            try {
                if (Integer.parseInt(txtAjoutStock.getText().trim()) < 0) {
                    setEtat(txtAjoutStock, lblErreurStock, false, "Stock ne peut pas etre negatif"); ok = false;
                }
            } catch (NumberFormatException e) {
                setEtat(txtAjoutStock, lblErreurStock, false, "Entier requis"); ok = false;
            }
        }
        if (comboAjoutProduit.getValue() == null) {
            setEtat(null, lblErreurProduit, false, "Produit associe obligatoire *"); ok = false;
        }
        return ok;
    }

    // ─────────────────────────────────────────────
    //  TAB 3 — MODIFIER
    // ─────────────────────────────────────────────
    @FXML private void chargerKitPourModification() {
        KitHobbies k = comboModifierKit.getValue();
        if (k == null) return;
        txtModifId.setText(String.valueOf(k.getIdKit()));
        txtModifNom.setText(k.getNomKit());
        comboModifType.setValue(k.getTypeArtisanat());
        comboModifNiveau.setValue(k.getNiveauDifficulte());
        txtModifPrix.setText(k.getPrix() != null ? k.getPrix().toString() : "");
        txtModifStock.setText(String.valueOf(k.getStock()));
        txtModifDescription.setText(k.getDescription() != null ? k.getDescription() : "");
        txtModifDuree.clear();
        txtModifMateriaux.clear();
        
        // Charger l'image existante
        modifImagePath = k.getImageUrl();
        if (modifImagePath != null && !modifImagePath.isEmpty()) {
            lblModifImageNom.setText("Image actuelle");
            lblModifImageNom.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            try {
                File imageFile = new File("src/main/resources" + modifImagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    if (imgModifPreview != null) {
                        imgModifPreview.setImage(image);
                        imgModifPreview.setVisible(true);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement image: " + e.getMessage());
            }
        } else {
            lblModifImageNom.setText("Aucune image sélectionnée");
            lblModifImageNom.setStyle("-fx-text-fill: #7f8c8d;");
            if (imgModifPreview != null) {
                imgModifPreview.setImage(null);
                imgModifPreview.setVisible(false);
            }
        }
        
        // Pré-sélectionner le produit associé
        produitsObs.stream()
                .filter(p -> p.getIdProduit() == k.getIdProduit())
                .findFirst()
                .ifPresent(comboModifProduit::setValue);
        for (TextField tf : new TextField[]{txtModifNom, txtModifPrix, txtModifStock})
            tf.setStyle("");
        if (lblMessageModif != null) lblMessageModif.setVisible(false);
    }

    @FXML private void modifierKit() {
        KitHobbies k = comboModifierKit.getValue();
        if (k == null) { afficherMessage(lblMessageModif, "Selectionnez un kit.", false); return; }

        boolean ok = true;
        if (txtModifNom.getText().trim().isEmpty()) {
            surlignerChamp(txtModifNom, false);
            afficherMessage(lblMessageModif, "Nom obligatoire.", false); ok = false;
        }
        if (comboModifProduit.getValue() == null) {
            afficherMessage(lblMessageModif, "Produit associe obligatoire.", false); ok = false;
        }
        try {
            if (new BigDecimal(txtModifPrix.getText().trim()).compareTo(BigDecimal.ZERO) <= 0) {
                surlignerChamp(txtModifPrix, false);
                afficherMessage(lblMessageModif, "Prix doit etre > 0.", false); ok = false;
            }
        } catch (Exception e) { surlignerChamp(txtModifPrix, false); afficherMessage(lblMessageModif, "Prix invalide.", false); ok = false; }
        try {
            if (Integer.parseInt(txtModifStock.getText().trim()) < 0) {
                surlignerChamp(txtModifStock, false);
                afficherMessage(lblMessageModif, "Stock ne peut pas etre negatif.", false); ok = false;
            }
        } catch (Exception e) { surlignerChamp(txtModifStock, false); afficherMessage(lblMessageModif, "Stock invalide.", false); ok = false; }
        if (!ok) return;

        try {
            k.setNomKit(txtModifNom.getText().trim());
            k.setTypeArtisanat(comboModifType.getValue());
            k.setNiveauDifficulte(comboModifNiveau.getValue());
            k.setPrix(new BigDecimal(txtModifPrix.getText().trim()));
            k.setStock(Integer.parseInt(txtModifStock.getText().trim()));
            
            // Gérer l'image
            if (modifImagePath != null) {
                // Supprimer l'ancienne image si elle existe
                if (k.getImageUrl() != null && !k.getImageUrl().isEmpty()) {
                    ImageUploadUtil.deleteImage(k.getImageUrl());
                }
                k.setImageUrl(modifImagePath);
            }
            
            k.setDescription(txtModifDescription.getText().trim());
            k.setIdProduit(comboModifProduit.getValue().getIdProduit()); // FK mise à jour
            kitService.modifier(k);
            afficherMessage(lblMessageModif, "✓ Kit modifie avec succes !", true);
            chargerKits(); chargerCombosKits(); mettreAJourStatistiques();
        } catch (Exception e) {
            afficherMessage(lblMessageModif, "✗ Erreur : " + e.getMessage(), false);
        }
    }

    @FXML private void annulerModification() {
        comboModifierKit.setValue(null);
        for (TextField tf : new TextField[]{txtModifId, txtModifNom, txtModifPrix, txtModifStock, txtModifDuree})
            if (tf != null) { tf.clear(); tf.setStyle(""); }
        if (txtModifDescription != null) txtModifDescription.clear();
        if (txtModifMateriaux   != null) txtModifMateriaux.clear();
        comboModifType.setValue(null); comboModifNiveau.setValue(null);
        comboModifProduit.setValue(null);
        
        // Réinitialiser l'image
        modifImagePath = null;
        if (lblModifImageNom != null) {
            lblModifImageNom.setText("Aucune image sélectionnée");
            lblModifImageNom.setStyle("-fx-text-fill: #7f8c8d;");
        }
        if (imgModifPreview != null) {
            imgModifPreview.setImage(null);
            imgModifPreview.setVisible(false);
        }
        
        if (lblMessageModif != null) lblMessageModif.setVisible(false);
    }

    // ─────────────────────────────────────────────
    //  TAB 4 — SUPPRIMER
    // ─────────────────────────────────────────────
    @FXML private void afficherDetailsKitASupprimer() {
        KitHobbies k = comboSupprimerKit.getValue();
        if (k == null) { detailsSuppressionBox.setVisible(false); return; }
        lblSupprNom.setText("Nom : "    + k.getNomKit());
        lblSupprType.setText("Type : "  + k.getTypeArtisanat());
        lblSupprPrix.setText("Prix : "  + k.getPrix() + " TND");
        lblSupprStock.setText("Stock : " + k.getStock());
        // Afficher le nom du produit associé
        String nomProduit = produitsObs.stream()
                .filter(p -> p.getIdProduit() == k.getIdProduit())
                .map(p -> p.getNom() + " (" + p.getRegion() + ")")
                .findFirst().orElse("—");
        lblSupprProduit.setText("Produit : " + nomProduit);
        detailsSuppressionBox.setVisible(true);
        if (lblMessageSuppr != null) lblMessageSuppr.setVisible(false);
    }

    @FXML private void supprimerKit() {
        KitHobbies k = comboSupprimerKit.getValue();
        if (k == null) { afficherMessage(lblMessageSuppr, "Selectionnez un kit.", false); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le kit");
        confirm.setContentText("Supprimer definitivement \"" + k.getNomKit() + "\" ?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            kitService.supprimer(k.getIdKit());
            afficherMessage(lblMessageSuppr, "✓ Kit supprime avec succes !", true);
            detailsSuppressionBox.setVisible(false);
            chargerKits(); chargerCombosKits(); mettreAJourStatistiques();
        }
    }

    @FXML private void annulerSuppression() {
        comboSupprimerKit.setValue(null);
        detailsSuppressionBox.setVisible(false);
        if (lblMessageSuppr != null) lblMessageSuppr.setVisible(false);
    }

    // ─────────────────────────────────────────────
    //  STATISTIQUES
    // ─────────────────────────────────────────────
    private void mettreAJourStatistiques() {
        List<KitHobbies> kits = kitService.afficher();
        long faciles    = kits.stream().filter(k -> "Facile".equals(k.getNiveauDifficulte())).count();
        long difficiles = kits.stream().filter(k -> "Difficile".equals(k.getNiveauDifficulte())).count();
        if (lblStats        != null) lblStats.setText("Total: " + kits.size() + " kits");
        if (lblTotalKits    != null) lblTotalKits.setText(String.valueOf(kits.size()));
        if (lblKitsFaciles  != null) lblKitsFaciles.setText(String.valueOf(faciles));
        if (lblKitsDifficiles != null) lblKitsDifficiles.setText(String.valueOf(difficiles));
    }

    // ─────────────────────────────────────────────
    //  HELPERS VISUELS
    // ─────────────────────────────────────────────
    private void setEtat(TextField champ, Label lbl, boolean ok, String message) {
        if (champ != null) surlignerChamp(champ, ok);
        if (lbl == null) return;
        if (!ok && message != null) {
            lbl.setText("⚠ " + message);
            lbl.setStyle("-fx-text-fill:#D94F1E;-fx-font-size:11px;-fx-font-weight:bold;");
            lbl.setVisible(true);
        } else {
            lbl.setText(ok ? "✓" : "");
            lbl.setStyle(ok ? "-fx-text-fill:#1A7A7A;-fx-font-size:11px;-fx-font-weight:bold;" : "");
            lbl.setVisible(ok);
        }
    }

    private void surlignerChamp(TextField champ, boolean ok) {
        champ.setStyle(ok
                ? "-fx-border-color:#1A7A7A;-fx-border-width:2;-fx-border-radius:8;-fx-background-radius:8;"
                : "-fx-border-color:#D94F1E;-fx-border-width:2;-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-effect:dropshadow(gaussian,rgba(217,79,30,0.25),6,0,0,0);");
    }

    private void afficherMessage(Label lbl, String msg, boolean succes) {
        if (lbl == null) return;
        lbl.setText(msg);
        lbl.setStyle(succes
                ? "-fx-text-fill:#1A7A7A;-fx-font-weight:bold;-fx-font-size:13px;"
                + "-fx-background-color:rgba(26,122,122,0.08);-fx-padding:8 12;-fx-background-radius:6;"
                : "-fx-text-fill:#D94F1E;-fx-font-weight:bold;-fx-font-size:13px;"
                + "-fx-background-color:rgba(217,79,30,0.08);-fx-padding:8 12;-fx-background-radius:6;");
        lbl.setVisible(true);
    }

    private void alerteInfo(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void alerteErreur(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}