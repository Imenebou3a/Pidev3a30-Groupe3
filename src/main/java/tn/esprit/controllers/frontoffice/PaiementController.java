package tn.esprit.controllers.frontoffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.Commande;
import tn.esprit.entities.LignePanier;
import tn.esprit.services.CommandeService;
import tn.esprit.services.SmsService;
import tn.esprit.services.PanierService;
import tn.esprit.services.StripePaymentService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Contrôleur pour la page de paiement
 */
public class PaiementController {
    
    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextArea txtAdresse;
    @FXML private TextField txtNumeroCarte;
    @FXML private TextField txtExpiration;
    @FXML private TextField txtCVV;
    @FXML private VBox containerResume;
    @FXML private Label lblSousTotal;
    @FXML private Label lblLivraison;
    @FXML private Label lblTotalFinal;
    @FXML private Button btnPayer;
    
    private PanierService panierService;
    private CommandeService commandeService;
    private SmsService smsService;
    private StripePaymentService stripeService;
    private static final BigDecimal FRAIS_LIVRAISON = new BigDecimal("7.00");
    
    private String paymentIntentId; // ID du paiement Stripe
    
    @FXML
    public void initialize() {
        panierService = PanierService.getInstance();
        commandeService = new CommandeService();
        smsService = new SmsService();
        stripeService = new StripePaymentService();
        
        // Formater automatiquement les champs
        configurerFormatageChamps();
        
        // Charger le résumé
        chargerResume();
        
        // Configurer le bouton payer
        if (btnPayer != null) {
            btnPayer.setOnAction(e -> traiterPaiement());
        }
    }
    
    private void configurerFormatageChamps() {
        // Formater le numéro de carte (XXXX XXXX XXXX XXXX)
        txtNumeroCarte.textProperty().addListener((obs, old, val) -> {
            if (val == null) return;
            if (!val.matches("[0-9 ]*")) {
                Platform.runLater(() -> txtNumeroCarte.setText(old != null ? old : ""));
            } else if (val.length() > 19) {
                Platform.runLater(() -> txtNumeroCarte.setText(old != null ? old : ""));
            } else {
                String formatted = val.replaceAll(" ", "");
                if (formatted.length() > 0) {
                    formatted = formatted.replaceAll("(.{4})", "$1 ").trim();
                    if (!val.equals(formatted)) {
                        final String finalFormatted = formatted;
                        Platform.runLater(() -> {
                            txtNumeroCarte.setText(finalFormatted);
                            txtNumeroCarte.positionCaret(finalFormatted.length());
                        });
                    }
                }
            }
        });
        
        // Formater la date d'expiration (MM/AA)
        txtExpiration.textProperty().addListener((obs, old, val) -> {
            if (val == null) return;
            if (!val.matches("[0-9/]*")) {
                Platform.runLater(() -> txtExpiration.setText(old != null ? old : ""));
            } else if (val.length() > 5) {
                Platform.runLater(() -> txtExpiration.setText(old != null ? old : ""));
            } else {
                String formatted = val.replaceAll("/", "");
                if (formatted.length() >= 2) {
                    formatted = formatted.substring(0, 2) + "/" + formatted.substring(2);
                }
                if (!val.equals(formatted) && formatted.length() <= 5) {
                    final String finalFormatted = formatted;
                    Platform.runLater(() -> {
                        txtExpiration.setText(finalFormatted);
                        txtExpiration.positionCaret(finalFormatted.length());
                    });
                }
            }
        });
        
        // CVV - seulement des chiffres (max 4)
        txtCVV.textProperty().addListener((obs, old, val) -> {
            if (val == null) return;
            if (!val.matches("\\d*")) {
                Platform.runLater(() -> txtCVV.setText(old != null ? old : ""));
            } else if (val.length() > 4) {
                Platform.runLater(() -> txtCVV.setText(old != null ? old : ""));
            }
        });
        
        // Téléphone - format tunisien
        txtTelephone.textProperty().addListener((obs, old, val) -> {
            if (val == null) return;
            if (!val.matches("[+0-9 ]*")) {
                Platform.runLater(() -> txtTelephone.setText(old != null ? old : ""));
            }
        });
    }
    
    private void chargerResume() {
        containerResume.getChildren().clear();
        
        for (LignePanier ligne : panierService.getLignes()) {
            containerResume.getChildren().add(creerItemResume(ligne));
        }
        
        // Calculer les totaux
        BigDecimal sousTotal = panierService.getTotal();
        BigDecimal total = sousTotal.add(FRAIS_LIVRAISON);
        
        lblSousTotal.setText(String.format("%.2f TND", sousTotal));
        lblLivraison.setText(String.format("%.2f TND", FRAIS_LIVRAISON));
        lblTotalFinal.setText(String.format("%.2f TND", total));
    }
    
    private VBox creerItemResume(LignePanier ligne) {
        VBox item = new VBox(5);
        item.getStyleClass().add("item-resume");
        
        Label nom = new Label(ligne.getNom());
        nom.getStyleClass().add("item-nom");
        
        Label details = new Label(ligne.getDetails());
        details.getStyleClass().add("item-details");
        
        HBox bottom = new HBox();
        bottom.setSpacing(10);
        
        Label quantite = new Label("Qté: " + ligne.getQuantite());
        quantite.getStyleClass().add("item-quantite");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label prix = new Label(String.format("%.2f TND", ligne.getSousTotal()));
        prix.getStyleClass().add("item-prix");
        
        bottom.getChildren().addAll(quantite, spacer, prix);
        item.getChildren().addAll(nom, details, bottom);
        
        return item;
    }
    
    private void traiterPaiement() {
        // Valider les champs
        if (!validerFormulaire()) {
            return;
        }
        
        // Désactiver le bouton
        btnPayer.setDisable(true);
        btnPayer.setText("Traitement en cours...");
        
        // Traiter le paiement avec Stripe en arrière-plan
        new Thread(() -> {
            try {
                // Étape 1: Créer le PaymentIntent avec Stripe
                BigDecimal total = panierService.getTotal().add(FRAIS_LIVRAISON);
                String description = String.format("Commande de %d article(s)", panierService.getNombreArticles());
                
                paymentIntentId = stripeService.creerPaiement(total, txtEmail.getText(), description);
                
                if (paymentIntentId == null) {
                    javafx.application.Platform.runLater(() -> {
                        afficherErreurPaiement("Impossible de créer le paiement. Veuillez réessayer.");
                    });
                    return;
                }
                
                // Étape 2: Confirmer le paiement avec les détails de la carte
                String numeroCarte = txtNumeroCarte.getText().replaceAll("\\s", "");
                String[] expParts = txtExpiration.getText().split("/");
                int expMois = Integer.parseInt(expParts[0]);
                int expAnnee = 2000 + Integer.parseInt(expParts[1]);
                String cvv = txtCVV.getText();
                
                boolean paiementReussi = stripeService.confirmerPaiement(
                    paymentIntentId, numeroCarte, expMois, expAnnee, cvv
                );
                
                // Simulation d'un délai de traitement
                Thread.sleep(1500);
                
                javafx.application.Platform.runLater(() -> {
                    if (paiementReussi) {
                        afficherConfirmation();
                    } else {
                        afficherErreurPaiement("Le paiement a été refusé. Vérifiez vos informations.");
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    afficherErreurPaiement("Erreur lors du traitement: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void afficherErreurPaiement(String message) {
        btnPayer.setDisable(false);
        btnPayer.setText("Payer maintenant");
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de paiement");
        alert.setHeaderText("Le paiement a échoué");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();
        
        // Valider nom
        if (txtNom.getText().trim().isEmpty()) {
            erreurs.append("- Le nom est requis\n");
        }
        
        // Valider email
        if (txtEmail.getText().trim().isEmpty()) {
            erreurs.append("- L'email est requis\n");
        } else if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", txtEmail.getText())) {
            erreurs.append("- L'email n'est pas valide\n");
        }
        
        // Valider téléphone
        if (txtTelephone.getText().trim().isEmpty()) {
            erreurs.append("- Le téléphone est requis\n");
        }
        
        // Valider adresse
        if (txtAdresse.getText().trim().isEmpty()) {
            erreurs.append("- L'adresse est requise\n");
        }
        
        // Valider numéro de carte
        String carte = txtNumeroCarte.getText().replaceAll(" ", "");
        if (carte.isEmpty()) {
            erreurs.append("- Le numéro de carte est requis\n");
        } else if (carte.length() != 16) {
            erreurs.append("- Le numéro de carte doit contenir 16 chiffres\n");
        }
        
        // Valider date d'expiration
        if (txtExpiration.getText().trim().isEmpty()) {
            erreurs.append("- La date d'expiration est requise\n");
        } else if (!Pattern.matches("\\d{2}/\\d{2}", txtExpiration.getText())) {
            erreurs.append("- La date d'expiration doit être au format MM/AA\n");
        }
        
        // Valider CVV
        if (txtCVV.getText().trim().isEmpty()) {
            erreurs.append("- Le CVV est requis\n");
        } else if (txtCVV.getText().length() != 3) {
            erreurs.append("- Le CVV doit contenir 3 chiffres\n");
        }
        
        if (erreurs.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Formulaire incomplet");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes:");
            alert.setContentText(erreurs.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    private void afficherConfirmation() {
        BigDecimal total = panierService.getTotal().add(FRAIS_LIVRAISON);
        
        // Créer et enregistrer la commande en base de données
        Commande commande = new Commande(
            txtNom.getText().trim(),
            txtEmail.getText().trim(),
            txtTelephone.getText().trim(),
            txtAdresse.getText().trim(),
            new ArrayList<>(panierService.getLignes()),
            panierService.getTotal(),
            FRAIS_LIVRAISON
        );
        
        commande.setStatut("CONFIRMEE"); // Statut CONFIRMEE car paiement Stripe confirmé
        
        // Enregistrer en base
        int idCommande = commandeService.enregistrerCommande(commande);
        
        String messageConfirmation;
        
        if (idCommande > 0) {
            commande.setIdCommande(idCommande);
            
            // Envoyer le SMS de confirmation en arrière-plan
            new Thread(() -> {
                boolean success = smsService.envoyerConfirmationCommande(commande);
                if (success) {
                    System.out.println("✅ SMS de confirmation envoyé");
                } else {
                    System.err.println("⚠️ Le SMS n'a pas pu être envoyé");
                }
            }).start();
            
            messageConfirmation = String.format(
                "Merci %s !\n\n" +
                "✅ Paiement Stripe confirmé\n" +
                "Transaction ID: %s\n\n" +
                "Numéro de commande: #%d\n" +
                "Montant payé: %.2f TND\n" +
                "Nombre d'articles: %d\n\n" +
                "Un SMS de confirmation sera envoyé au:\n%s\n\n" +
                "Votre commande sera livrée à:\n%s",
                txtNom.getText(),
                paymentIntentId != null ? paymentIntentId : "N/A",
                idCommande,
                total,
                panierService.getNombreArticles(),
                txtTelephone.getText(),
                txtAdresse.getText()
            );
        } else {
            messageConfirmation = String.format(
                "Merci %s !\n\n" +
                "✅ Paiement Stripe confirmé\n" +
                "Transaction ID: %s\n\n" +
                "Montant payé: %.2f TND\n" +
                "Nombre d'articles: %d\n\n" +
                "Votre commande sera livrée à:\n%s\n\n" +
                "Note: La commande n'a pas pu être enregistrée en base de données.",
                txtNom.getText(),
                paymentIntentId != null ? paymentIntentId : "N/A",
                total,
                panierService.getNombreArticles(),
                txtAdresse.getText()
            );
        }
        
        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
        confirmation.setTitle("Paiement réussi");
        confirmation.setHeaderText("✓ Votre paiement a été confirmé !");
        confirmation.setContentText(messageConfirmation);
        
        confirmation.showAndWait();
        
        // Vider le panier et fermer
        panierService.viderPanier();
        fermerFenetre();
    }
    
    @FXML
    private void retourPanier() {
        fermerFenetre();
    }
    
    private void fermerFenetre() {
        Stage stage = (Stage) btnPayer.getScene().getWindow();
        stage.close();
    }
}
