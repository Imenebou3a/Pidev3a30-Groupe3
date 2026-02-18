package tn.esprit.controllers.backoffice;
import tn.esprit.entities.ProduitLocal;
import tn.esprit.services.ProduitLocalService;
import tn.esprit.utils.MyDataBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProduitBackController {

    // ===== TAB 1: LISTE =====
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboCategorie, comboRegion;
    @FXML private FlowPane gridProduits;
    @FXML private Label lblStats;

    // ===== TAB 2: AJOUTER =====
    @FXML private TextField txtAjoutNom, txtAjoutPrix, txtAjoutStock, txtAjoutImageUrl;
    @FXML private ComboBox<String> comboAjoutCategorie, comboAjoutRegion;
    @FXML private TextArea txtAjoutDescription;

    // Labels d'erreur pour validation
    @FXML private Label lblErreurNom, lblErreurCategorie, lblErreurRegion;
    @FXML private Label lblErreurPrix, lblErreurStock, lblErreurUrl;
    @FXML private Label lblErreurDescription, lblCompteurDescription;
    @FXML private Label lblMessageGlobal;

    // ===== TAB 3: MODIFIER =====
    @FXML private ComboBox<String> comboModifierProduit;
    @FXML private TextField txtModifId, txtModifNom, txtModifPrix, txtModifStock, txtModifImageUrl;
    @FXML private ComboBox<String> comboModifCategorie, comboModifRegion;
    @FXML private TextArea txtModifDescription;
    @FXML private Label lblMessageModif;

    // ===== TAB 4: SUPPRIMER =====
    @FXML private ComboBox<String> comboSupprimerProduit;
    @FXML private VBox detailsSuppressionBox;
    @FXML private Label lblSupprNom, lblSupprCategorie, lblSupprPrix, lblSupprStock, lblMessageSuppr;

    private ProduitLocalService produitService;
    private ObservableList<ProduitLocal> produits;
    private ProduitLocal produitSelectionne;

    @FXML
    public void initialize() {
        produitService = new ProduitLocalService();
        produits = FXCollections.observableArrayList();

        configurerFiltres();
        configurerComboBoxes();
        configurerValidation();
        chargerProduits();

        // Listeners pour recherche en temps réel
        txtRecherche.textProperty().addListener((obs, old, val) -> appliquerFiltres());
        comboCategorie.setOnAction(e -> appliquerFiltres());
        comboRegion.setOnAction(e -> appliquerFiltres());
    }

    // ========== CONFIGURATION ==========

    private void configurerFiltres() {
        comboCategorie.setItems(FXCollections.observableArrayList(
                "Toutes", "Artisanat", "Textile", "Poterie", "Bijoux", "Alimentation"
        ));
        comboCategorie.setValue("Toutes");

        comboRegion.setItems(FXCollections.observableArrayList(
                "Toutes", "Tunis", "Sfax", "Sousse", "Kairouan", "Nabeul", "Djerba", "Bizerte"
        ));
        comboRegion.setValue("Toutes");
    }

    private void configurerComboBoxes() {
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Artisanat", "Textile", "Poterie", "Bijoux", "Alimentation"
        );
        ObservableList<String> regions = FXCollections.observableArrayList(
                "Tunis", "Sfax", "Sousse", "Kairouan", "Nabeul", "Djerba", "Bizerte"
        );

        comboAjoutCategorie.setItems(categories);
        comboAjoutRegion.setItems(regions);
        comboModifCategorie.setItems(categories);
        comboModifRegion.setItems(regions);
    }

    // ========== VALIDATION EN TEMPS RÉEL ==========

    private void configurerValidation() {
        // VALIDATION NOM
        txtAjoutNom.textProperty().addListener((obs, old, val) -> {
            if (val.isEmpty()) {
                afficherErreur(lblErreurNom, "");
                txtAjoutNom.setStyle("");
            } else if (val.length() > 100) {
                txtAjoutNom.setText(old);
            } else if (!val.matches("[a-zA-ZÀ-ÿ0-9\\s\\-']+")) {
                afficherErreur(lblErreurNom, "❌ Caractères invalides");
                txtAjoutNom.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (val.length() < 3) {
                afficherErreur(lblErreurNom, "⚠️ Minimum 3 caractères");
                txtAjoutNom.setStyle("-fx-border-color: #f39c12; -fx-border-width: 2;");
            } else {
                afficherErreur(lblErreurNom, "✓ Nom valide");
                lblErreurNom.setStyle("-fx-text-fill: #27ae60;");
                txtAjoutNom.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            }
        });

        // VALIDATION PRIX
        txtAjoutPrix.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*\\.?\\d*")) {
                txtAjoutPrix.setText(old);
                return;
            }
            if (val.contains(".")) {
                String[] parts = val.split("\\.");
                if (parts.length > 1 && parts[1].length() > 2) {
                    txtAjoutPrix.setText(old);
                    return;
                }
            }

            try {
                if (val.isEmpty() || val.equals(".")) {
                    afficherErreur(lblErreurPrix, "");
                    txtAjoutPrix.setStyle("");
                } else {
                    double prix = Double.parseDouble(val);
                    if (prix <= 0) {
                        afficherErreur(lblErreurPrix, "❌ Prix doit être > 0");
                        txtAjoutPrix.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                    } else if (prix > 10000) {
                        afficherErreur(lblErreurPrix, "⚠️ Prix très élevé");
                        txtAjoutPrix.setStyle("-fx-border-color: #f39c12; -fx-border-width: 2;");
                    } else {
                        afficherErreur(lblErreurPrix, "✓ Prix valide");
                        lblErreurPrix.setStyle("-fx-text-fill: #27ae60;");
                        txtAjoutPrix.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                    }
                }
            } catch (NumberFormatException e) {
                afficherErreur(lblErreurPrix, "");
            }
        });

        // VALIDATION STOCK
        txtAjoutStock.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) {
                txtAjoutStock.setText(old);
                return;
            }
            if (val.length() > 5) {
                txtAjoutStock.setText(old);
                return;
            }

            try {
                if (val.isEmpty()) {
                    afficherErreur(lblErreurStock, "");
                    txtAjoutStock.setStyle("");
                } else {
                    int stock = Integer.parseInt(val);
                    if (stock < 0) {
                        afficherErreur(lblErreurStock, "❌ Stock négatif");
                        txtAjoutStock.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                    } else if (stock > 1000) {
                        afficherErreur(lblErreurStock, "⚠️ Stock très élevé");
                        txtAjoutStock.setStyle("-fx-border-color: #f39c12; -fx-border-width: 2;");
                    } else {
                        afficherErreur(lblErreurStock, "✓ Stock valide");
                        lblErreurStock.setStyle("-fx-text-fill: #27ae60;");
                        txtAjoutStock.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                    }
                }
            } catch (NumberFormatException e) {
                afficherErreur(lblErreurStock, "");
            }
        });

        // VALIDATION URL
        txtAjoutImageUrl.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String url = txtAjoutImageUrl.getText().trim();
                if (url.isEmpty()) {
                    afficherErreur(lblErreurUrl, "");
                    txtAjoutImageUrl.setStyle("");
                } else if (!url.matches("^https?://.*")) {
                    afficherErreur(lblErreurUrl, "❌ URL doit commencer par http:// ou https://");
                    txtAjoutImageUrl.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                } else {
                    afficherErreur(lblErreurUrl, "✓ URL valide");
                    lblErreurUrl.setStyle("-fx-text-fill: #27ae60;");
                    txtAjoutImageUrl.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                }
            }
        });

        // VALIDATION DESCRIPTION + COMPTEUR
        txtAjoutDescription.textProperty().addListener((obs, old, val) -> {
            int length = val.length();
            if (lblCompteurDescription != null) {
                lblCompteurDescription.setText(length + "/1000");
            }

            if (length > 1000) {
                txtAjoutDescription.setText(old);
            } else if (length > 900 && lblCompteurDescription != null) {
                lblCompteurDescription.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                afficherErreur(lblErreurDescription, "⚠️ Limite bientôt atteinte");
            } else {
                if (lblCompteurDescription != null) {
                    lblCompteurDescription.setStyle("-fx-text-fill: #666;");
                }
                afficherErreur(lblErreurDescription, "");
            }
        });

        // VALIDATION COMBOS
        comboAjoutCategorie.setOnAction(e -> {
            if (comboAjoutCategorie.getValue() != null) {
                afficherErreur(lblErreurCategorie, "✓ Catégorie OK");
                lblErreurCategorie.setStyle("-fx-text-fill: #27ae60;");
            }
        });

        comboAjoutRegion.setOnAction(e -> {
            if (comboAjoutRegion.getValue() != null) {
                afficherErreur(lblErreurRegion, "✓ Région OK");
                lblErreurRegion.setStyle("-fx-text-fill: #27ae60;");
            }
        });

        // Valeurs par défaut
        txtAjoutPrix.setText("0.00");
        txtAjoutStock.setText("0");
        if (lblCompteurDescription != null) {
            lblCompteurDescription.setText("0/1000");
        }
    }

    private void afficherErreur(Label label, String message) {
        if (label == null) return;

        if (message.isEmpty()) {
            label.setVisible(false);
        } else {
            label.setText(message);
            label.setVisible(true);
            if (!message.startsWith("✓")) {
                label.setStyle("-fx-text-fill: #c0392b;");
            }
        }
    }

    // ========== CHARGEMENT DONNÉES ==========

    private void chargerProduits() {
        List<ProduitLocal> liste = produitService.afficher();
        produits.setAll(liste);
        chargerProduitsEnCartes(produits);
        mettreAJourStats();
        mettreAJourCombosProduits();
    }

    private void chargerProduitsEnCartes(ObservableList<ProduitLocal> liste) {
        gridProduits.getChildren().clear();
        for (ProduitLocal p : liste) {
            gridProduits.getChildren().add(creerCarteProduit(p));
        }
    }

    private VBox creerCarteProduit(ProduitLocal produit) {
        VBox card = new VBox(10);
        card.getStyleClass().add("produit-card");
        card.setAlignment(Pos.TOP_CENTER);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("produit-image");
        
        try {
            if (produit.getImageUrl() != null && !produit.getImageUrl().isEmpty()) {
                imageView.setImage(new Image(produit.getImageUrl(), true));
            } else {
                imageView.setImage(new Image("https://via.placeholder.com/220x150?text=Pas+d'image", true));
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/220x150?text=Erreur", true));
        }

        // Nom
        Label lblNom = new Label(produit.getNom());
        lblNom.getStyleClass().add("produit-nom");
        lblNom.setWrapText(true);
        lblNom.setMaxWidth(220);

        // Catégorie et Région
        Label lblInfo = new Label(produit.getCategorie() + " • " + produit.getRegion());
        lblInfo.getStyleClass().add("produit-categorie");

        // Prix
        Label lblPrix = new Label(String.format("%.2f TND", produit.getPrix()));
        lblPrix.getStyleClass().add("produit-prix");

        // Stock avec indicateur coloré
        String stockText;
        String stockStyle;
        if (produit.getStock() == 0) {
            stockText = "🔴 Rupture";
            stockStyle = "stock-rupture";
        } else if (produit.getStock() <= 10) {
            stockText = "🟠 Stock: " + produit.getStock();
            stockStyle = "stock-faible";
        } else {
            stockText = "🟢 Stock: " + produit.getStock();
            stockStyle = "stock-ok";
        }
        Label lblStock = new Label(stockText);
        lblStock.getStyleClass().addAll("produit-stock", stockStyle);

        // Boutons d'action
        Button btnModif = new Button("✏️");
        btnModif.getStyleClass().addAll("btn-card", "btn-modifier");
        btnModif.setOnAction(e -> preparerModification(produit));

        Button btnSuppr = new Button("🗑️");
        btnSuppr.getStyleClass().addAll("btn-card", "btn-supprimer");
        btnSuppr.setOnAction(e -> confirmerSuppression(produit));

        HBox actions = new HBox(5, btnModif, btnSuppr);
        actions.setAlignment(Pos.CENTER);
        actions.getStyleClass().add("card-actions");

        card.getChildren().addAll(imageView, lblNom, lblInfo, lblPrix, lblStock, actions);
        return card;
    }

    private void afficherDetailsProduit(ProduitLocal produit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Produit");
        alert.setHeaderText(produit.getNom());
        
        String details = String.format(
            "ID: %d\n" +
            "Catégorie: %s\n" +
            "Région: %s\n" +
            "Prix: %.2f TND\n" +
            "Stock: %d unités\n\n" +
            "Description:\n%s",
            produit.getIdProduit(),
            produit.getCategorie(),
            produit.getRegion(),
            produit.getPrix(),
            produit.getStock(),
            produit.getDescription() != null ? produit.getDescription() : "Aucune description"
        );
        
        alert.setContentText(details);
        alert.showAndWait();
    }

    private void appliquerFiltres() {
        List<ProduitLocal> liste = produitService.afficher();

        String recherche = txtRecherche.getText().toLowerCase();
        String cat = comboCategorie.getValue();
        String reg = comboRegion.getValue();

        ObservableList<ProduitLocal> filtres = FXCollections.observableArrayList();

        for (ProduitLocal p : liste) {
            boolean matchRecherche = recherche.isEmpty() || p.getNom().toLowerCase().contains(recherche);
            boolean matchCat = "Toutes".equals(cat) || p.getCategorie().equals(cat);
            boolean matchReg = "Toutes".equals(reg) || p.getRegion().equals(reg);

            if (matchRecherche && matchCat && matchReg) {
                filtres.add(p);
            }
        }

        chargerProduitsEnCartes(filtres);
        lblStats.setText("Affichés: " + filtres.size() + " / Total: " + liste.size() + " produits");
    }

    private void mettreAJourStats() {
        lblStats.setText("Total: " + produits.size() + " produits");
    }

    private void mettreAJourCombosProduits() {
        ObservableList<String> noms = FXCollections.observableArrayList();
        for (ProduitLocal p : produits) {
            noms.add(p.getIdProduit() + " - " + p.getNom());
        }
        comboModifierProduit.setItems(noms);
        comboSupprimerProduit.setItems(noms);
    }

    // ========== TAB 1: ACTIONS LISTE ==========

    @FXML
    private void reinitialiserFiltres() {
        txtRecherche.clear();
        comboCategorie.setValue("Toutes");
        comboRegion.setValue("Toutes");
        chargerProduits();
    }

    @FXML
    private void exporterCSV() {
        afficherMessageGlobal("Fonctionnalité d'export CSV à venir...", "info");
    }

    // ========== TAB 2: AJOUTER ==========

    @FXML
    private void ajouterProduit() {
        // Effacer message global
        if (lblMessageGlobal != null) {
            lblMessageGlobal.setVisible(false);
        }

        try {
            // Validation
            String erreurs = validerFormulaire();
            if (!erreurs.isEmpty()) {
                afficherMessageGlobal("❌ Erreurs:\n" + erreurs, "error");
                return;
            }

            String nom = txtAjoutNom.getText().trim();

            // Vérifier doublon
            for (ProduitLocal p : produits) {
                if (p.getNom().equalsIgnoreCase(nom)) {
                    afficherMessageGlobal("❌ Un produit avec ce nom existe déjà", "error");
                    return;
                }
            }

            // Créer produit
            ProduitLocal produit = new ProduitLocal();
            produit.setNom(nom);
            produit.setDescription(txtAjoutDescription.getText().trim());
            produit.setPrix(new BigDecimal(txtAjoutPrix.getText()));
            produit.setCategorie(comboAjoutCategorie.getValue());
            produit.setRegion(comboAjoutRegion.getValue());
            produit.setStock(Integer.parseInt(txtAjoutStock.getText()));
            produit.setImageUrl(txtAjoutImageUrl.getText().trim());

            produitService.ajouter(produit);

            afficherMessageGlobal("✅ Produit ajouté avec succès !", "success");
            reinitialiserFormulaireAjout();
            chargerProduits();

        } catch (NumberFormatException e) {
            afficherMessageGlobal("❌ Erreur: Prix ou Stock invalide", "error");
        } catch (Exception e) {
            afficherMessageGlobal("❌ Erreur: " + e.getMessage(), "error");
        }
    }

    private String validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (txtAjoutNom.getText().trim().isEmpty()) {
            erreurs.append("• Nom obligatoire\n");
        } else if (txtAjoutNom.getText().trim().length() < 3) {
            erreurs.append("• Nom minimum 3 caractères\n");
        }

        if (comboAjoutCategorie.getValue() == null) {
            erreurs.append("• Catégorie obligatoire\n");
        }

        if (comboAjoutRegion.getValue() == null) {
            erreurs.append("• Région obligatoire\n");
        }

        try {
            double prix = Double.parseDouble(txtAjoutPrix.getText());
            if (prix <= 0) {
                erreurs.append("• Prix doit être > 0\n");
            }
        } catch (NumberFormatException e) {
            erreurs.append("• Prix invalide\n");
        }

        try {
            int stock = Integer.parseInt(txtAjoutStock.getText());
            if (stock < 0) {
                erreurs.append("• Stock ne peut être négatif\n");
            }
        } catch (NumberFormatException e) {
            erreurs.append("• Stock invalide\n");
        }

        String url = txtAjoutImageUrl.getText().trim();
        if (!url.isEmpty() && !url.matches("^https?://.*")) {
            erreurs.append("• URL invalide\n");
        }

        return erreurs.toString();
    }

    @FXML
    private void reinitialiserFormulaireAjout() {
        txtAjoutNom.clear();
        txtAjoutDescription.clear();
        txtAjoutPrix.setText("0.00");
        txtAjoutStock.setText("0");
        txtAjoutImageUrl.clear();
        comboAjoutCategorie.setValue(null);
        comboAjoutRegion.setValue(null);

        // Réinitialiser erreurs
        afficherErreur(lblErreurNom, "");
        afficherErreur(lblErreurCategorie, "");
        afficherErreur(lblErreurRegion, "");
        afficherErreur(lblErreurPrix, "");
        afficherErreur(lblErreurStock, "");
        afficherErreur(lblErreurUrl, "");
        afficherErreur(lblErreurDescription, "");

        if (lblCompteurDescription != null) {
            lblCompteurDescription.setText("0/1000");
            lblCompteurDescription.setStyle("-fx-text-fill: #666;");
        }

        if (lblMessageGlobal != null) {
            lblMessageGlobal.setVisible(false);
        }

        // Styles
        txtAjoutNom.setStyle("");
        txtAjoutPrix.setStyle("");
        txtAjoutStock.setStyle("");
        txtAjoutImageUrl.setStyle("");
    }

    // ========== TAB 3: MODIFIER ==========

    @FXML
    private void chargerProduitPourModification() {
        String selection = comboModifierProduit.getValue();
        if (selection == null) return;

        int id = Integer.parseInt(selection.split(" - ")[0]);
        produitSelectionne = produitService.getById(id);

        if (produitSelectionne != null) {
            txtModifId.setText(String.valueOf(produitSelectionne.getIdProduit()));
            txtModifNom.setText(produitSelectionne.getNom());
            txtModifDescription.setText(produitSelectionne.getDescription());
            txtModifPrix.setText(produitSelectionne.getPrix().toString());
            txtModifStock.setText(String.valueOf(produitSelectionne.getStock()));
            txtModifImageUrl.setText(produitSelectionne.getImageUrl());
            comboModifCategorie.setValue(produitSelectionne.getCategorie());
            comboModifRegion.setValue(produitSelectionne.getRegion());
        }
    }

    private void preparerModification(ProduitLocal p) {
        comboModifierProduit.setValue(p.getIdProduit() + " - " + p.getNom());
        chargerProduitPourModification();
    }

    @FXML
    private void modifierProduit() {
        if (produitSelectionne == null) {
            afficherMessage(lblMessageModif, "❌ Aucun produit sélectionné", "error");
            return;
        }

        try {
            produitSelectionne.setNom(txtModifNom.getText().trim());
            produitSelectionne.setDescription(txtModifDescription.getText().trim());
            produitSelectionne.setPrix(new BigDecimal(txtModifPrix.getText()));
            produitSelectionne.setCategorie(comboModifCategorie.getValue());
            produitSelectionne.setRegion(comboModifRegion.getValue());
            produitSelectionne.setStock(Integer.parseInt(txtModifStock.getText()));
            produitSelectionne.setImageUrl(txtModifImageUrl.getText().trim());

            produitService.modifier(produitSelectionne);

            afficherMessage(lblMessageModif, "✅ Produit modifié !", "success");
            chargerProduits();
            annulerModification();

        } catch (Exception e) {
            afficherMessage(lblMessageModif, "❌ Erreur: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void annulerModification() {
        comboModifierProduit.setValue(null);
        txtModifId.clear();
        txtModifNom.clear();
        txtModifDescription.clear();
        txtModifPrix.clear();
        txtModifStock.clear();
        txtModifImageUrl.clear();
        comboModifCategorie.setValue(null);
        comboModifRegion.setValue(null);
        produitSelectionne = null;
        lblMessageModif.setText("");
    }

    // ========== TAB 4: SUPPRIMER ==========

    @FXML
    private void afficherDetailsProduitASupprimer() {
        String selection = comboSupprimerProduit.getValue();
        if (selection == null) {
            detailsSuppressionBox.setVisible(false);
            return;
        }

        int id = Integer.parseInt(selection.split(" - ")[0]);
        produitSelectionne = produitService.getById(id);

        if (produitSelectionne != null) {
            lblSupprNom.setText("Nom: " + produitSelectionne.getNom());
            lblSupprCategorie.setText("Catégorie: " + produitSelectionne.getCategorie() + " | Région: " + produitSelectionne.getRegion());
            lblSupprPrix.setText("Prix: " + produitSelectionne.getPrix() + " TND");
            lblSupprStock.setText("Stock: " + produitSelectionne.getStock() + " unités");
            detailsSuppressionBox.setVisible(true);
        }
    }

    private void confirmerSuppression(ProduitLocal p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le produit?");
        alert.setContentText("Voulez-vous vraiment supprimer: " + p.getNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            produitService.supprimer(p.getIdProduit());
            chargerProduits();
        }
    }

    @FXML
    private void supprimerProduit() {
        if (produitSelectionne == null) {
            afficherMessage(lblMessageSuppr, "❌ Aucun produit sélectionné", "error");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("⚠️ Confirmation");
        alert.setHeaderText("Supprimer " + produitSelectionne.getNom() + " ?");
        alert.setContentText("Action irréversible !");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            produitService.supprimer(produitSelectionne.getIdProduit());
            afficherMessage(lblMessageSuppr, "✅ Produit supprimé", "success");
            chargerProduits();
            annulerSuppression();
        }
    }

    @FXML
    private void annulerSuppression() {
        comboSupprimerProduit.setValue(null);
        detailsSuppressionBox.setVisible(false);
        produitSelectionne = null;
        lblMessageSuppr.setText("");
    }

    // ========== UTILITAIRES ==========

    private void afficherMessage(Label label, String message, String type) {
        if (label == null) return;
        label.setText(message);
        label.getStyleClass().removeAll("message-success", "message-error", "message-info");
        label.getStyleClass().add("message-" + type);
    }

    private void afficherMessageGlobal(String message, String type) {
        if (lblMessageGlobal == null) return;
        lblMessageGlobal.setText(message);
        lblMessageGlobal.setVisible(true);
        lblMessageGlobal.getStyleClass().removeAll("message-global", "success", "error", "info");
        lblMessageGlobal.getStyleClass().addAll("message-global", type);
    }
}