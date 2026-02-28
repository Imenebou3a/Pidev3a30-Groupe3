package tn.esprit.tests;

import tn.esprit.entities.Commande;
import tn.esprit.entities.LignePanier;
import tn.esprit.services.SmsService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de test pour vérifier la configuration SMS Twilio
 */
public class TestSmsConfiguration {
    
    public static void main(String[] args) {
        System.out.println("=== Test de Configuration SMS Twilio ===\n");
        
        SmsService smsService = new SmsService();
        
        // Test 1: Vérifier la configuration Twilio
        System.out.println("Test 1: Vérification de la configuration Twilio...");
        if (smsService.testerConfiguration()) {
            System.out.println("✅ Configuration Twilio valide\n");
        } else {
            System.out.println("❌ Configuration Twilio invalide");
            System.out.println("Vérifiez ACCOUNT_SID, AUTH_TOKEN et TWILIO_PHONE_NUMBER dans SmsService.java\n");
            return;
        }
        
        // Test 2: Créer une commande de test
        System.out.println("Test 2: Création d'une commande de test...");
        Commande commandeTest = creerCommandeTest();
        System.out.println("✅ Commande créée: " + commandeTest + "\n");
        
        // Test 3: Envoyer un SMS de confirmation
        System.out.println("Test 3: Envoi du SMS de confirmation...");
        System.out.println("Destinataire: " + commandeTest.getTelephoneClient());
        
        try {
            System.out.println("Envoi en cours...");
            boolean success = smsService.envoyerConfirmationCommande(commandeTest);
            
            if (success) {
                System.out.println("✅ SMS envoyé avec succès!");
                System.out.println("Vérifiez votre téléphone: " + commandeTest.getTelephoneClient());
            } else {
                System.out.println("❌ Échec de l'envoi du SMS");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Fin des tests ===");
    }
    
    /**
     * Crée une commande de test avec des données fictives
     */
    private static Commande creerCommandeTest() {
        // Créer des lignes de panier
        List<LignePanier> lignes = new ArrayList<>();
        
        LignePanier ligne1 = new LignePanier();
        ligne1.setNom("Kit Peinture Artiste");
        ligne1.setQuantite(2);
        ligne1.setPrixUnitaire(new BigDecimal("45.00"));
        lignes.add(ligne1);
        
        LignePanier ligne2 = new LignePanier();
        ligne2.setNom("Huile d'Olive Bio 1L");
        ligne2.setQuantite(1);
        ligne2.setPrixUnitaire(new BigDecimal("25.00"));
        lignes.add(ligne2);
        
        // Créer la commande
        Commande commande = new Commande(
            "Ahmed Ben Ali",
            "ahmed@example.com",
            "56107714", // Numéro de test - sera normalisé en +21656107714
            "123 Avenue Habib Bourguiba, Tunis",
            lignes,
            new BigDecimal("115.00"),
            new BigDecimal("7.00")
        );
        
        commande.setIdCommande(12345);
        
        return commande;
    }
}
