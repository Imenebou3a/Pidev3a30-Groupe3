package tn.esprit.controllers.backoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import tn.esprit.entities.Commande;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Contrôleur pour la gestion des commandes dans le backoffice
 */
public class CommandeBackController {

    @FXML private FlowPane gridCommandes;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblTotalCommandes;
    @FXML private Label lblCommandesEnAttente;
    @FXML private Label lblCommandesConfirmees;
    @FXML private Label lblCommandesExpediees;
    
    private ObservableList<Commande> listeCommandes;
    private Connection connection;

    @FXML
    public void initialize() {
        connection = MyDataBase.getConnection();
        listeCommandes = FXCollections.observableArrayList();
        
        // Configurer le ComboBox des statuts
        cbStatut.setItems(FXCollections.observableArrayList(
            "Tous", "EN_ATTENTE", "CONFIRMEE", "EXPEDIEE", "LIVREE", "ANNULEE"
        ));
        cbStatut.setValue("Tous");
        
        // Charger les données
        chargerCommandes();
        mettreAJourStatistiques();
    }
    
    private void chargerCommandes() {
        listeCommandes.clear();
        
        String sql = "SELECT * FROM commandes ORDER BY date_commande DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Commande commande = new Commande();
                commande.setIdCommande(rs.getInt("id_commande"));
                commande.setNomClient(rs.getString("nom_client"));
                commande.setEmailClient(rs.getString("email_client"));
                commande.setTelephoneClient(rs.getString("telephone_client"));
                commande.setAdresseClient(rs.getString("adresse_client"));
                commande.setSousTotal(rs.getBigDecimal("sous_total"));
                commande.setFraisLivraison(rs.getBigDecimal("frais_livraison"));
                commande.setTotal(rs.getBigDecimal("total"));
                commande.setDateCommande(rs.getTimestamp("date_commande").toLocalDateTime());
                commande.setStatut(rs.getString("statut"));
                
                listeCommandes.add(commande);
            }
            
            chargerCommandesEnCartes(listeCommandes);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement commandes: " + e.getMessage());
            afficherErreur("Erreur de chargement", "Impossible de charger les commandes");
        }
    }
    
    private void chargerCommandesEnCartes(ObservableList<Commande> commandes) {
        gridCommandes.getChildren().clear();
        for (Commande cmd : commandes) {
            gridCommandes.getChildren().add(creerCarteCommande(cmd));
        }
    }
    
    private VBox creerCarteCommande(Commande commande) {
        VBox card = new VBox(12);
        card.getStyleClass().add("commande-card");
        card.setAlignment(Pos.TOP_LEFT);
        
        // En-tête avec numéro et statut
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label lblNumero = new Label("Commande #" + commande.getIdCommande());
        lblNumero.getStyleClass().add("commande-numero");
        
        Label lblStatut = new Label(getStatutTexte(commande.getStatut()));
        lblStatut.getStyleClass().addAll("commande-statut", "statut-" + commande.getStatut().toLowerCase());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(lblNumero, spacer, lblStatut);
        
        // Informations client
        VBox infoClient = new VBox(5);
        infoClient.getStyleClass().add("info-section");
        
        Label lblClient = new Label("👤 " + commande.getNomClient());
        lblClient.getStyleClass().add("info-principale");
        
        Label lblEmail = new Label("📧 " + commande.getEmailClient());
        lblEmail.getStyleClass().add("info-secondaire");
        
        Label lblTel = new Label("📞 " + commande.getTelephoneClient());
        lblTel.getStyleClass().add("info-secondaire");
        
        infoClient.getChildren().addAll(lblClient, lblEmail, lblTel);
        
        // Montant et date
        HBox infoCommande = new HBox(20);
        infoCommande.setAlignment(Pos.CENTER_LEFT);
        
        VBox montantBox = new VBox(3);
        Label lblMontantLabel = new Label("Montant total");
        lblMontantLabel.getStyleClass().add("label-petit");
        Label lblMontant = new Label(commande.getTotalFormate());
        lblMontant.getStyleClass().add("montant-total");
        montantBox.getChildren().addAll(lblMontantLabel, lblMontant);
        
        VBox dateBox = new VBox(3);
        Label lblDateLabel = new Label("Date");
        lblDateLabel.getStyleClass().add("label-petit");
        Label lblDate = new Label(commande.getDateFormatee());
        lblDate.getStyleClass().add("date-commande");
        dateBox.getChildren().addAll(lblDateLabel, lblDate);
        
        infoCommande.getChildren().addAll(montantBox, dateBox);
        
        // Séparateur
        Separator sep = new Separator();
        
        // Boutons d'action
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        
        Button btnDetails = new Button("📋 Détails");
        btnDetails.getStyleClass().addAll("btn-action", "btn-details");
        btnDetails.setOnAction(e -> afficherDetails(commande));
        
        // Boutons selon le statut
        if ("EN_ATTENTE".equals(commande.getStatut())) {
            Button btnConfirmer = new Button("✓ Confirmer");
            btnConfirmer.getStyleClass().addAll("btn-action", "btn-confirmer");
            btnConfirmer.setOnAction(e -> changerStatutRapide(commande, "CONFIRMEE"));
            
            Button btnAnnuler = new Button("✗ Annuler");
            btnAnnuler.getStyleClass().addAll("btn-action", "btn-annuler");
            btnAnnuler.setOnAction(e -> changerStatutRapide(commande, "ANNULEE"));
            
            actions.getChildren().addAll(btnDetails, btnConfirmer, btnAnnuler);
            
        } else if ("CONFIRMEE".equals(commande.getStatut())) {
            Button btnExpedier = new Button("📦 Expédier");
            btnExpedier.getStyleClass().addAll("btn-action", "btn-expedier");
            btnExpedier.setOnAction(e -> changerStatutRapide(commande, "EXPEDIEE"));
            
            Button btnAnnuler = new Button("✗ Annuler");
            btnAnnuler.getStyleClass().addAll("btn-action", "btn-annuler");
            btnAnnuler.setOnAction(e -> changerStatutRapide(commande, "ANNULEE"));
            
            actions.getChildren().addAll(btnDetails, btnExpedier, btnAnnuler);
            
        } else if ("EXPEDIEE".equals(commande.getStatut())) {
            Button btnLivrer = new Button("✓ Livrer");
            btnLivrer.getStyleClass().addAll("btn-action", "btn-livrer");
            btnLivrer.setOnAction(e -> changerStatutRapide(commande, "LIVREE"));
            
            actions.getChildren().addAll(btnDetails, btnLivrer);
            
        } else {
            // Pour LIVREE et ANNULEE, seulement le bouton détails
            actions.getChildren().add(btnDetails);
        }
        
        card.getChildren().addAll(header, infoClient, infoCommande, sep, actions);
        return card;
    }
    
    private String getStatutTexte(String statut) {
        return switch (statut) {
            case "EN_ATTENTE" -> "⏳ En Attente";
            case "CONFIRMEE" -> "✓ Confirmée";
            case "EXPEDIEE" -> "📦 Expédiée";
            case "LIVREE" -> "✅ Livrée";
            case "ANNULEE" -> "❌ Annulée";
            default -> statut;
        };
    }
    
    private void changerStatutRapide(Commande commande, String nouveauStatut) {
        try {
            String sql = "UPDATE commandes SET statut = ? WHERE id_commande = ?";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, nouveauStatut);
            pst.setInt(2, commande.getIdCommande());
            
            int rows = pst.executeUpdate();
            
            if (rows > 0) {
                commande.setStatut(nouveauStatut);
                chargerCommandes();
                mettreAJourStatistiques();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setHeaderText(null);
                success.setContentText("Statut mis à jour: " + getStatutTexte(nouveauStatut));
                success.showAndWait();
            }
            
        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible de mettre à jour le statut");
        }
    }
    
    @FXML
    private void rechercherCommandes() {
        String recherche = txtRecherche.getText().toLowerCase();
        String statutFiltre = cbStatut.getValue();
        
        ObservableList<Commande> filtrees = FXCollections.observableArrayList();
        
        for (Commande cmd : listeCommandes) {
            boolean matchRecherche = recherche.isEmpty() || 
                cmd.getNomClient().toLowerCase().contains(recherche) ||
                cmd.getEmailClient().toLowerCase().contains(recherche) ||
                String.valueOf(cmd.getIdCommande()).contains(recherche);
            
            boolean matchStatut = statutFiltre.equals("Tous") || 
                cmd.getStatut().equals(statutFiltre);
            
            if (matchRecherche && matchStatut) {
                filtrees.add(cmd);
            }
        }
        
        chargerCommandesEnCartes(filtrees);
    }
    
    @FXML
    private void reinitialiserFiltres() {
        txtRecherche.clear();
        cbStatut.setValue("Tous");
        chargerCommandesEnCartes(listeCommandes);
    }
    
    private void afficherDetails(Commande commande) {
        try {
            // Charger les lignes de commande
            String sql = "SELECT * FROM lignes_commande WHERE id_commande = ?";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, commande.getIdCommande());
            ResultSet rs = pst.executeQuery();
            
            StringBuilder details = new StringBuilder();
            details.append("═══════════════════════════════════════\n");
            details.append("       DÉTAILS DE LA COMMANDE #").append(commande.getIdCommande()).append("\n");
            details.append("═══════════════════════════════════════\n\n");
            
            details.append("👤 CLIENT\n");
            details.append("   Nom: ").append(commande.getNomClient()).append("\n");
            details.append("   Email: ").append(commande.getEmailClient()).append("\n");
            details.append("   Téléphone: ").append(commande.getTelephoneClient()).append("\n");
            details.append("   Adresse: ").append(commande.getAdresseClient()).append("\n\n");
            
            details.append("📦 ARTICLES COMMANDÉS\n");
            details.append("───────────────────────────────────────\n");
            
            while (rs.next()) {
                details.append("• ").append(rs.getString("nom_item")).append("\n");
                details.append("  Type: ").append(rs.getString("type_produit")).append("\n");
                details.append("  Prix unitaire: ").append(rs.getBigDecimal("prix_unitaire")).append(" TND\n");
                details.append("  Quantité: ").append(rs.getInt("quantite")).append("\n");
                details.append("  Sous-total: ").append(rs.getBigDecimal("sous_total")).append(" TND\n\n");
            }
            
            details.append("───────────────────────────────────────\n");
            details.append("💰 TOTAUX\n");
            details.append("   Sous-total: ").append(commande.getSousTotal()).append(" TND\n");
            details.append("   Frais de livraison: ").append(commande.getFraisLivraison()).append(" TND\n");
            details.append("   TOTAL: ").append(commande.getTotal()).append(" TND\n\n");
            
            details.append("📅 Date: ").append(commande.getDateFormatee()).append("\n");
            details.append("📊 Statut: ").append(commande.getStatut()).append("\n");
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Détails de la commande");
            alert.setHeaderText(null);
            alert.setContentText(details.toString());
            alert.getDialogPane().setMinWidth(500);
            alert.showAndWait();
            
        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible de charger les détails");
        }
    }
    
    private void changerStatut(Commande commande) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
            commande.getStatut(),
            "EN_ATTENTE", "CONFIRMEE", "EXPEDIEE", "LIVREE", "ANNULEE"
        );
        
        dialog.setTitle("Changer le statut");
        dialog.setHeaderText("Commande #" + commande.getIdCommande());
        dialog.setContentText("Nouveau statut:");
        
        dialog.showAndWait().ifPresent(nouveauStatut -> {
            try {
                String sql = "UPDATE commandes SET statut = ? WHERE id_commande = ?";
                PreparedStatement pst = connection.prepareStatement(sql);
                pst.setString(1, nouveauStatut);
                pst.setInt(2, commande.getIdCommande());
                
                int rows = pst.executeUpdate();
                
                if (rows > 0) {
                    commande.setStatut(nouveauStatut);
                    chargerCommandes();
                    mettreAJourStatistiques();
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Succès");
                    success.setHeaderText(null);
                    success.setContentText("Statut mis à jour avec succès!");
                    success.showAndWait();
                }
                
            } catch (SQLException e) {
                afficherErreur("Erreur", "Impossible de mettre à jour le statut");
            }
        });
    }
    
    private void mettreAJourStatistiques() {
        try {
            String sql = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN statut = 'EN_ATTENTE' THEN 1 ELSE 0 END) as en_attente, " +
                "SUM(CASE WHEN statut = 'CONFIRMEE' THEN 1 ELSE 0 END) as confirmees, " +
                "SUM(CASE WHEN statut = 'EXPEDIEE' THEN 1 ELSE 0 END) as expediees " +
                "FROM commandes";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                lblTotalCommandes.setText(String.valueOf(rs.getInt("total")));
                lblCommandesEnAttente.setText(String.valueOf(rs.getInt("en_attente")));
                lblCommandesConfirmees.setText(String.valueOf(rs.getInt("confirmees")));
                lblCommandesExpediees.setText(String.valueOf(rs.getInt("expediees")));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur statistiques: " + e.getMessage());
        }
    }
    
    @FXML
    private void actualiser() {
        chargerCommandes();
        mettreAJourStatistiques();
        reinitialiserFiltres();
    }
    
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
