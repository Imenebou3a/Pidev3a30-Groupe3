package tn.esprit.services;

import tn.esprit.entities.Commande;
import tn.esprit.entities.LignePanier;

import javax.mail.*;
import javax.mail.internet.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Service pour l'envoi d'emails de confirmation de commande
 */
public class EmailService {
    
    // Configuration SMTP - À MODIFIER avec vos propres identifiants
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "votre-email@gmail.com"; // CHANGEZ ICI
    private static final String EMAIL_PASSWORD = "votre-mot-de-passe-app"; // CHANGEZ ICI
    private static final String COMPANY_NAME = "Artisanat Tunisien";
    
    /**
     * Envoie un email de confirmation de commande
     */
    public boolean envoyerConfirmationCommande(Commande commande) {
        try {
            // Configuration des propriétés SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            
            // Création de la session avec authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });
            
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, COMPANY_NAME));
            message.setRecipients(Message.RecipientType.TO, 
                InternetAddress.parse(commande.getEmailClient()));
            message.setSubject("✓ Confirmation de commande #" + commande.getIdCommande());
            
            // Contenu HTML de l'email
            String htmlContent = genererContenuEmail(commande);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            // Envoi du message
            Transport.send(message);
            
            System.out.println("✅ Email envoyé avec succès à: " + commande.getEmailClient());
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Génère le contenu HTML de l'email de confirmation
     */
    private String genererContenuEmail(Commande commande) {
        StringBuilder html = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        
        // En-tête
        html.append("<div style='background-color: #1A7A7A; padding: 30px; text-align: center;'>");
        html.append("<h1 style='color: white; margin: 0;'>").append(COMPANY_NAME).append("</h1>");
        html.append("</div>");
        
        // Corps du message
        html.append("<div style='padding: 30px; background-color: #f9f9f9;'>");
        html.append("<div style='background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
        
        // Message de bienvenue
        html.append("<h2 style='color: #1A7A7A;'>Bonjour ").append(commande.getNomClient()).append(",</h2>");
        html.append("<p style='font-size: 16px;'>Merci pour votre commande ! Nous avons bien reçu votre paiement.</p>");
        
        // Informations de commande
        html.append("<div style='background-color: #f5f5f5; padding: 20px; border-radius: 4px; margin: 20px 0;'>");
        html.append("<h3 style='color: #1A7A7A; margin-top: 0;'>Détails de la commande</h3>");
        html.append("<p><strong>Numéro de commande:</strong> #").append(commande.getIdCommande()).append("</p>");
        html.append("<p><strong>Date:</strong> ").append(commande.getDateCommande().format(formatter)).append("</p>");
        html.append("<p><strong>Statut:</strong> <span style='color: #4CAF50;'>").append(commande.getStatut()).append("</span></p>");
        html.append("</div>");
        
        // Articles commandés
        html.append("<h3 style='color: #1A7A7A;'>Articles commandés</h3>");
        html.append("<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>");
        html.append("<thead><tr style='background-color: #1A7A7A; color: white;'>");
        html.append("<th style='padding: 12px; text-align: left;'>Article</th>");
        html.append("<th style='padding: 12px; text-align: center;'>Quantité</th>");
        html.append("<th style='padding: 12px; text-align: right;'>Prix unitaire</th>");
        html.append("<th style='padding: 12px; text-align: right;'>Sous-total</th>");
        html.append("</tr></thead><tbody>");
        
        for (LignePanier ligne : commande.getLignes()) {
            html.append("<tr style='border-bottom: 1px solid #ddd;'>");
            html.append("<td style='padding: 12px;'>").append(ligne.getNom());
            if (ligne.getDetails() != null && !ligne.getDetails().isEmpty()) {
                html.append("<br><small style='color: #888;'>").append(ligne.getDetails()).append("</small>");
            }
            html.append("</td>");
            html.append("<td style='padding: 12px; text-align: center;'>").append(ligne.getQuantite()).append("</td>");
            html.append("<td style='padding: 12px; text-align: right;'>").append(formatPrice(ligne.getPrixUnitaire())).append("</td>");
            html.append("<td style='padding: 12px; text-align: right;'>").append(formatPrice(ligne.getSousTotal())).append("</td>");
            html.append("</tr>");
        }
        
        html.append("</tbody></table>");
        
        // Totaux
        html.append("<div style='text-align: right; margin: 20px 0;'>");
        html.append("<p style='font-size: 16px;'><strong>Sous-total:</strong> ").append(formatPrice(commande.getSousTotal())).append("</p>");
        html.append("<p style='font-size: 16px;'><strong>Frais de livraison:</strong> ").append(formatPrice(commande.getFraisLivraison())).append("</p>");
        html.append("<p style='font-size: 20px; color: #D94F1E;'><strong>TOTAL:</strong> ").append(formatPrice(commande.getTotal())).append("</p>");
        html.append("</div>");
        
        // Adresse de livraison
        html.append("<div style='background-color: #f5f5f5; padding: 20px; border-radius: 4px; margin: 20px 0;'>");
        html.append("<h3 style='color: #1A7A7A; margin-top: 0;'>Adresse de livraison</h3>");
        html.append("<p>").append(commande.getNomClient()).append("<br>");
        html.append(commande.getTelephoneClient()).append("<br>");
        html.append(commande.getAdresseClient().replace("\n", "<br>")).append("</p>");
        html.append("</div>");
        
        // Message de remerciement
        html.append("<p style='font-size: 16px; margin-top: 30px;'>Votre commande sera préparée et expédiée dans les plus brefs délais.</p>");
        html.append("<p style='font-size: 16px;'>Pour toute question, n'hésitez pas à nous contacter.</p>");
        
        html.append("</div></div>");
        
        // Pied de page
        html.append("<div style='background-color: #333; color: white; padding: 20px; text-align: center;'>");
        html.append("<p style='margin: 0;'>© 2026 ").append(COMPANY_NAME).append(" - Tous droits réservés</p>");
        html.append("<p style='margin: 10px 0 0 0; font-size: 14px;'>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>");
        html.append("</div>");
        
        html.append("</body></html>");
        
        return html.toString();
    }
    
    /**
     * Formate un prix avec 2 décimales et la devise TND
     */
    private String formatPrice(BigDecimal price) {
        return String.format("%.2f TND", price);
    }
    
    /**
     * Teste la configuration email
     */
    public boolean testerConfiguration() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });
            
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            
            System.out.println("✅ Configuration email valide");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Configuration email invalide: " + e.getMessage());
            return false;
        }
    }
}
