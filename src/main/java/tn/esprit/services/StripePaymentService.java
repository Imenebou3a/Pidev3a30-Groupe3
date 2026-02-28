package tn.esprit.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour gérer les paiements via Stripe API
 */
public class StripePaymentService {
    
    // Clé API Stripe (TEST MODE)
    // IMPORTANT: En production, utiliser des variables d'environnement
    private static final String STRIPE_SECRET_KEY = "sk_test_51QsLhYP5example_your_test_key_here";
    
    // Mode de test (true = simulation, false = vraie API)
    private static final boolean TEST_MODE = true;
    
    public StripePaymentService() {
        // Initialiser Stripe avec la clé secrète
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }
    
    /**
     * Créer un paiement via Stripe
     * 
     * @param montant Montant en TND
     * @param email Email du client
     * @param description Description du paiement
     * @return ID du PaymentIntent si succès, null sinon
     */
    public String creerPaiement(BigDecimal montant, String email, String description) {
        if (TEST_MODE) {
            // Mode simulation pour les tests
            return simulerPaiement(montant, email, description);
        }
        
        try {
            // Convertir TND en centimes (Stripe utilise les plus petites unités)
            long montantCentimes = montant.multiply(new BigDecimal("100")).longValue();
            
            // Créer les paramètres du paiement
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(montantCentimes)
                .setCurrency("tnd") // Dinar Tunisien
                .setDescription(description)
                .putMetadata("email", email)
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();
            
            // Créer le PaymentIntent
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            System.out.println("✅ Paiement Stripe créé: " + paymentIntent.getId());
            System.out.println("   Montant: " + montant + " TND");
            System.out.println("   Statut: " + paymentIntent.getStatus());
            
            return paymentIntent.getId();
            
        } catch (StripeException e) {
            System.err.println("❌ Erreur Stripe: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Confirmer un paiement avec les détails de la carte
     * 
     * @param paymentIntentId ID du PaymentIntent
     * @param numeroCarte Numéro de carte (format: 4242424242424242)
     * @param expMois Mois d'expiration (1-12)
     * @param expAnnee Année d'expiration (ex: 2025)
     * @param cvv Code CVV
     * @return true si succès, false sinon
     */
    public boolean confirmerPaiement(String paymentIntentId, String numeroCarte, 
                                     int expMois, int expAnnee, String cvv) {
        if (TEST_MODE) {
            // En mode test, valider le format de la carte
            return validerCarteTest(numeroCarte, expMois, expAnnee, cvv);
        }
        
        try {
            // Récupérer le PaymentIntent
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            // Confirmer le paiement
            Map<String, Object> params = new HashMap<>();
            params.put("payment_method", creerMethodePaiement(numeroCarte, expMois, expAnnee, cvv));
            
            PaymentIntent confirmedIntent = paymentIntent.confirm(params);
            
            boolean success = "succeeded".equals(confirmedIntent.getStatus());
            
            if (success) {
                System.out.println("✅ Paiement confirmé avec succès");
            } else {
                System.err.println("⚠️ Paiement en attente: " + confirmedIntent.getStatus());
            }
            
            return success;
            
        } catch (StripeException e) {
            System.err.println("❌ Erreur confirmation: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Créer une méthode de paiement avec les détails de la carte
     */
    private String creerMethodePaiement(String numeroCarte, int expMois, int expAnnee, String cvv) 
            throws StripeException {
        // Cette méthode nécessiterait l'utilisation de Stripe Elements côté client
        // Pour une application JavaFX, on utilise une approche simplifiée
        return "pm_card_visa"; // ID de test Stripe
    }
    
    /**
     * Mode simulation pour les tests (sans vraie API)
     */
    private String simulerPaiement(BigDecimal montant, String email, String description) {
        System.out.println("🧪 MODE SIMULATION STRIPE");
        System.out.println("   Montant: " + montant + " TND");
        System.out.println("   Email: " + email);
        System.out.println("   Description: " + description);
        
        // Générer un ID fictif
        String fakeId = "pi_test_" + System.currentTimeMillis();
        System.out.println("   PaymentIntent ID: " + fakeId);
        
        return fakeId;
    }
    
    /**
     * Valider une carte en mode test
     */
    private boolean validerCarteTest(String numeroCarte, int expMois, int expAnnee, String cvv) {
        // Cartes de test Stripe valides
        String carteClean = numeroCarte.replaceAll("\\s", "");
        
        boolean carteValide = carteClean.equals("4242424242424242") || // Visa
                              carteClean.equals("5555555555554444") || // Mastercard
                              carteClean.equals("378282246310005") ||  // Amex
                              carteClean.length() == 16;
        
        boolean expValide = expMois >= 1 && expMois <= 12 && expAnnee >= 2024;
        boolean cvvValide = cvv.length() == 3 || cvv.length() == 4;
        
        if (carteValide && expValide && cvvValide) {
            System.out.println("✅ Carte validée (mode test)");
            return true;
        } else {
            System.err.println("❌ Carte invalide");
            if (!carteValide) System.err.println("   - Numéro de carte invalide");
            if (!expValide) System.err.println("   - Date d'expiration invalide");
            if (!cvvValide) System.err.println("   - CVV invalide");
            return false;
        }
    }
    
    /**
     * Récupérer les détails d'un paiement
     */
    public Map<String, String> getDetailsPaiement(String paymentIntentId) {
        Map<String, String> details = new HashMap<>();
        
        if (TEST_MODE) {
            details.put("id", paymentIntentId);
            details.put("status", "succeeded");
            details.put("mode", "test");
            return details;
        }
        
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            details.put("id", paymentIntent.getId());
            details.put("status", paymentIntent.getStatus());
            details.put("amount", String.valueOf(paymentIntent.getAmount() / 100.0));
            details.put("currency", paymentIntent.getCurrency());
            details.put("mode", "live");
            
        } catch (StripeException e) {
            System.err.println("❌ Erreur récupération: " + e.getMessage());
            details.put("error", e.getMessage());
        }
        
        return details;
    }
    
    /**
     * Annuler un paiement
     */
    public boolean annulerPaiement(String paymentIntentId) {
        if (TEST_MODE) {
            System.out.println("🧪 Paiement annulé (mode test): " + paymentIntentId);
            return true;
        }
        
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntent canceledIntent = paymentIntent.cancel();
            
            boolean success = "canceled".equals(canceledIntent.getStatus());
            if (success) {
                System.out.println("✅ Paiement annulé");
            }
            return success;
            
        } catch (StripeException e) {
            System.err.println("❌ Erreur annulation: " + e.getMessage());
            return false;
        }
    }
}
