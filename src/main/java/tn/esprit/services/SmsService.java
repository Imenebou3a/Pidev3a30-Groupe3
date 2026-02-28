package tn.esprit.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import tn.esprit.entities.Commande;
import tn.esprit.entities.LignePanier;

import java.math.BigDecimal;

/**
 * Service pour l'envoi de SMS de confirmation de commande via Twilio
 */
public class SmsService {
    
    // Configuration Twilio
    // ⚠️ IMPORTANT: Remplacer par vos vraies clés Twilio
    private static final String ACCOUNT_SID = "YOUR_TWILIO_ACCOUNT_SID";
    private static final String AUTH_TOKEN = "YOUR_TWILIO_AUTH_TOKEN";
    private static final String TWILIO_PHONE_NUMBER = "YOUR_TWILIO_PHONE_NUMBER";
    
    // MODE SIMULATION pour la démo (mettre false pour utiliser vraie API)
    // ⚠️ IMPORTANT: La Tunisie est un pays restreint sur Twilio Trial
    // Pour recevoir de vrais SMS, il faut upgrader le compte Twilio
    private static final boolean MODE_SIMULATION = false; // ✅ Mode réel activé
    
    private static boolean twilioInitialized = false;
    
    /**
     * Initialise la connexion Twilio
     */
    private void initializeTwilio() {
        if (!twilioInitialized && !MODE_SIMULATION) {
            try {
                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                twilioInitialized = true;
                System.out.println("✅ Twilio initialisé avec succès");
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l'initialisation de Twilio: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Envoie un SMS de confirmation de commande
     * @param commande La commande à confirmer
     * @return true si l'envoi a réussi, false sinon
     */
    public boolean envoyerConfirmationCommande(Commande commande) {
        try {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║           📱 ENVOI SMS DE CONFIRMATION                ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            
            String messageText = construireMessageConfirmation(commande);
            String phoneNumberOriginal = commande.getTelephoneClient();
            String phoneNumber = normaliserNumeroTelephone(phoneNumberOriginal);
            
            System.out.println("📞 Numéro original (du formulaire): " + phoneNumberOriginal);
            System.out.println("📞 Numéro normalisé (format international): " + phoneNumber);
            System.out.println("📧 Email: " + commande.getEmailClient());
            System.out.println("💰 Montant: " + String.format("%.2f TND", commande.getTotal()));
            System.out.println("\n📝 Message:");
            System.out.println("─────────────────────────────────────────────────────────");
            System.out.println(messageText);
            System.out.println("─────────────────────────────────────────────────────────");
            
            if (MODE_SIMULATION) {
                // Mode simulation pour la démo
                System.out.println("\n🧪 MODE SIMULATION ACTIVÉ");
                System.out.println("✅ SMS simulé envoyé avec succès!");
                System.out.println("📱 Le SMS serait envoyé à: " + phoneNumber);
                System.out.println("🔧 Pour activer l'envoi réel:");
                System.out.println("   1. Vérifier le numéro sur Twilio Console");
                System.out.println("   2. Mettre MODE_SIMULATION = false");
                System.out.println("   3. Vérifier que le AUTH_TOKEN est valide");
                System.out.println("\n╚════════════════════════════════════════════════════════╝\n");
                return true;
            }
            
            // Mode réel avec Twilio API
            initializeTwilio();
            
            System.out.println("\n📤 Envoi via Twilio API...");
            
            Message message = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(TWILIO_PHONE_NUMBER),
                messageText
            ).create();
            
            System.out.println("✅ SMS envoyé avec succès!");
            System.out.println("📋 Message SID: " + message.getSid());
            System.out.println("📊 Statut: " + message.getStatus());
            System.out.println("💵 Prix: " + message.getPrice() + " " + message.getPriceUnit());
            System.out.println("\n╚════════════════════════════════════════════════════════╝\n");
            return true;
            
        } catch (Exception e) {
            System.err.println("\n❌ ERREUR lors de l'envoi du SMS");
            System.err.println("Message d'erreur: " + e.getMessage());
            System.err.println("\n💡 Solutions possibles:");
            System.err.println("   1. Vérifier que le compte Twilio est actif");
            System.err.println("   2. Vérifier le AUTH_TOKEN dans Twilio Console");
            System.err.println("   3. Vérifier que le numéro destinataire est vérifié (mode Trial)");
            System.err.println("   4. Activer MODE_SIMULATION pour la démo");
            System.err.println("\n╚════════════════════════════════════════════════════════╝\n");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Construit le message de confirmation
     */
    private String construireMessageConfirmation(Commande commande) {
        StringBuilder message = new StringBuilder();
        
        message.append("╔═══════════════════════════╗\n");
        message.append("  ✅ COMMANDE CONFIRMÉE\n");
        message.append("╚═══════════════════════════╝\n\n");
        
        message.append("📋 Commande N° ").append(commande.getIdCommande()).append("\n");
        message.append("👤 Client: ").append(commande.getNomClient()).append("\n");
        message.append("📧 Email: ").append(commande.getEmailClient()).append("\n");
        message.append("📱 Tél: ").append(commande.getTelephoneClient()).append("\n");
        message.append("📍 Adresse: ").append(commande.getAdresseClient()).append("\n\n");
        
        message.append("───────────────────────────\n");
        message.append("🛒 ARTICLES COMMANDÉS\n");
        message.append("───────────────────────────\n");
        
        int count = 0;
        for (LignePanier ligne : commande.getLignes()) {
            if (count < 5) { // Limiter à 5 articles pour la longueur du SMS
                message.append("• ").append(ligne.getNom())
                       .append("\n  Qté: ").append(ligne.getQuantite())
                       .append(" × ").append(String.format("%.2f", ligne.getPrixUnitaire()))
                       .append(" = ").append(String.format("%.2f", ligne.getSousTotal()))
                       .append(" TND\n");
                count++;
            }
        }
        
        if (commande.getLignes().size() > 5) {
            message.append("• ... et ").append(commande.getLignes().size() - 5).append(" autre(s) article(s)\n");
        }
        
        message.append("\n───────────────────────────\n");
        message.append("💰 RÉCAPITULATIF\n");
        message.append("───────────────────────────\n");
        message.append("Sous-total: ").append(String.format("%.2f", commande.getSousTotal())).append(" TND\n");
        message.append("Livraison:  ").append(String.format("%.2f", commande.getFraisLivraison())).append(" TND\n");
        message.append("───────────────────────────\n");
        message.append("TOTAL:      ").append(String.format("%.2f", commande.getTotal())).append(" TND\n");
        message.append("═══════════════════════════\n\n");
        
        message.append("📦 Livraison sous 3-5 jours\n");
        message.append("🙏 Merci pour votre confiance!\n\n");
        message.append("Artisanat Tunisien 🇹🇳");
        
        return message.toString();
    }
    
    /**
     * Normalise le numéro de téléphone au format international
     * Ajoute +216 si le numéro commence par 0 ou n'a pas de préfixe
     */
    private String normaliserNumeroTelephone(String telephone) {
        if (telephone == null || telephone.trim().isEmpty()) {
            throw new IllegalArgumentException("Numéro de téléphone invalide");
        }
        
        // Supprimer les espaces et caractères spéciaux
        String cleaned = telephone.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Si commence par +, retourner tel quel
        if (cleaned.startsWith("+")) {
            return cleaned;
        }
        
        // Si commence par 00216, remplacer par +216
        if (cleaned.startsWith("00216")) {
            return "+" + cleaned.substring(2);
        }
        
        // Si commence par 216, ajouter +
        if (cleaned.startsWith("216")) {
            return "+" + cleaned;
        }
        
        // Si commence par 0, remplacer par +216
        if (cleaned.startsWith("0")) {
            return "+216" + cleaned.substring(1);
        }
        
        // Sinon, ajouter +216
        return "+216" + cleaned;
    }
    
    /**
     * Teste la configuration Twilio
     */
    public boolean testerConfiguration() {
        try {
            if (MODE_SIMULATION) {
                System.out.println("🧪 Mode simulation activé - Configuration OK");
                return true;
            }
            initializeTwilio();
            System.out.println("✅ Configuration Twilio valide");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Configuration Twilio invalide: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie un SMS de test
     */
    public boolean envoyerSmsTest(String numeroDestination, String message) {
        try {
            String phoneNumber = normaliserNumeroTelephone(numeroDestination);
            
            if (MODE_SIMULATION) {
                System.out.println("🧪 SMS de test simulé");
                System.out.println("📱 Destinataire: " + phoneNumber);
                System.out.println("📝 Message: " + message);
                return true;
            }
            
            initializeTwilio();
            
            Message msg = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(TWILIO_PHONE_NUMBER),
                message
            ).create();
            
            System.out.println("✅ SMS de test envoyé - SID: " + msg.getSid());
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du SMS de test: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
