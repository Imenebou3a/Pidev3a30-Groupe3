package tn.esprit.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité représentant une commande validée
 */
public class Commande {
    
    private int idCommande;
    private String nomClient;
    private String emailClient;
    private String telephoneClient;
    private String adresseClient;
    private List<LignePanier> lignes;
    private BigDecimal sousTotal;
    private BigDecimal fraisLivraison;
    private BigDecimal total;
    private LocalDateTime dateCommande;
    private String statut; // "EN_ATTENTE", "CONFIRMEE", "EXPEDIEE", "LIVREE"
    
    public Commande() {
        this.dateCommande = LocalDateTime.now();
        this.statut = "EN_ATTENTE";
    }
    
    public Commande(String nomClient, String emailClient, String telephoneClient,
                    String adresseClient, List<LignePanier> lignes,
                    BigDecimal sousTotal, BigDecimal fraisLivraison) {
        this();
        this.nomClient = nomClient;
        this.emailClient = emailClient;
        this.telephoneClient = telephoneClient;
        this.adresseClient = adresseClient;
        this.lignes = lignes;
        this.sousTotal = sousTotal;
        this.fraisLivraison = fraisLivraison;
        this.total = sousTotal.add(fraisLivraison);
    }
    
    // Getters et Setters
    public int getIdCommande() { return idCommande; }
    public void setIdCommande(int idCommande) { this.idCommande = idCommande; }
    
    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    
    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }
    
    public String getTelephoneClient() { return telephoneClient; }
    public void setTelephoneClient(String telephoneClient) { this.telephoneClient = telephoneClient; }
    
    public String getAdresseClient() { return adresseClient; }
    public void setAdresseClient(String adresseClient) { this.adresseClient = adresseClient; }
    
    public List<LignePanier> getLignes() { return lignes; }
    public void setLignes(List<LignePanier> lignes) { this.lignes = lignes; }
    
    public BigDecimal getSousTotal() { return sousTotal; }
    public void setSousTotal(BigDecimal sousTotal) { this.sousTotal = sousTotal; }
    
    public BigDecimal getFraisLivraison() { return fraisLivraison; }
    public void setFraisLivraison(BigDecimal fraisLivraison) { this.fraisLivraison = fraisLivraison; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    
    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    // Méthodes de formatage pour l'affichage
    public String getTotalFormate() {
        return String.format("%.2f TND", total);
    }
    
    public String getDateFormatee() {
        if (dateCommande == null) return "";
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateCommande.format(formatter);
    }
    
    @Override
    public String toString() {
        return "Commande{" +
                "idCommande=" + idCommande +
                ", nomClient='" + nomClient + '\'' +
                ", total=" + total +
                ", dateCommande=" + dateCommande +
                ", statut='" + statut + '\'' +
                '}';
    }
}
