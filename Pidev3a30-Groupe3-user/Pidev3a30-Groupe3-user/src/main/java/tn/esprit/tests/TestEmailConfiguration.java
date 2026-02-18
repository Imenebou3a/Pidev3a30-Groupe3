package tn.esprit.tests;

import tn.esprit.entities.Commande;
import tn.esprit.entities.LignePanier;
import tn.esprit.services.EmailService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de test pour vérifier la configuration email
 */
public class TestEmailConfiguration {
    
    public static void main(String[] args) {
        System.out.println("=== Test de Configuration Email ===\n");
        
        EmailService emailService = new EmailService();
        
        // Test 1: Vérifier la configuration SMTP
        System.out.println("Test 1: Vérification de la configuration SMTP...");
        if (emailService.testerConfiguration()) {
            System.out.println("✅ Configuration SMTP valide\n");
        } else {
            System.out.println("❌ Configuration SMTP invalide");
            System.out.println("Vérifiez EMAIL_FROM et EMAIL_PASSWORD dans EmailService.java\n");
            return;
        }
        
        // Test 2: Envoyer un email de test
        System.out.println("Test 2: Envoi d'un email de test...");
        System.out.print("Entrez votre email pour recevoir un test: ");
        
        try {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            String emailTest = scanner.nextLine().trim();
            
            if (emailTest.isEmpty() || !emailTest.contains("@")) {
                System.out.println("❌ Email invalide");
                return;
            }
            
            // Créer une commande de test
            Commande commandeTest = creerCommandeTest(emailTest);
            
            System.out.println("Envoi en cours...");
            boolean success = emailService.envoyerConfirmationCommande(commandeTest);
            
            if (success) {
                System.out.println("✅ Email envoyé avec succès!");
                System.out.println("Vérifiez votre boîte de réception (et les spams)");
            } else {
                System.out.println("❌ Échec de l'envoi");
                System.out.println("Vérifiez les logs pour plus de détails");
            }
            
            scanner.close();
            
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Fin du test ===");
    }
    
    /**
     * Crée une commande de test pour l'envoi d'email
     */
    private static Commande creerCommandeTest(String email) {
        // Créer des lignes de panier de test
        List<LignePanier> lignes = new ArrayList<>();
        
        LignePanier ligne1 = new LignePanier(
            "PRODUIT",
            1,
            "Poterie Artisanale de Nabeul",
            new BigDecimal("45.50"),
            2,
            "",
            "Artisanat • Nabeul"
        );
        ligne1.setIdLigne(1);
        
        LignePanier ligne2 = new LignePanier(
            "KIT",
            1,
            "Kit Découverte Poterie",
            new BigDecimal("89.00"),
            1,
            "",
            "Poterie • Débutant"
        );
        ligne2.setIdLigne(2);
        
        lignes.add(ligne1);
        lignes.add(ligne2);
        
        // Créer la commande
        Commande commande = new Commande(
            "Test Utilisateur",
            email,
            "+216 12 345 678",
            "123 Avenue Habib Bourguiba\nTunis 1000\nTunisie",
            lignes,
            new BigDecimal("180.00"),
            new BigDecimal("7.00")
        );
        
        commande.setIdCommande(12345);
        commande.setStatut("CONFIRMEE");
        
        return commande;
    }
}
