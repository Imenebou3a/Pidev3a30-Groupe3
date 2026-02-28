package tn.esprit.entities;

import java.math.BigDecimal;

/**
 * Représente une ligne dans le panier (un produit ou kit avec sa quantité)
 */
public class LignePanier {
    
    private int idLigne;
    private String typeProduit; // "PRODUIT" ou "KIT"
    private int idItem; // ID du produit ou kit
    private String nom;
    private BigDecimal prixUnitaire;
    private int quantite;
    private String imageUrl;
    private String details; // Catégorie/Région pour produit, Type/Niveau pour kit
    
    public LignePanier() {}
    
    public LignePanier(String typeProduit, int idItem, String nom, BigDecimal prixUnitaire,
                       int quantite, String imageUrl, String details) {
        this.typeProduit = typeProduit;
        this.idItem = idItem;
        this.nom = nom;
        this.prixUnitaire = prixUnitaire;
        this.quantite = quantite;
        this.imageUrl = imageUrl;
        this.details = details;
    }
    
    // Calcul du sous-total pour cette ligne
    public BigDecimal getSousTotal() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
    
    // Getters et Setters
    public int getIdLigne() { return idLigne; }
    public void setIdLigne(int idLigne) { this.idLigne = idLigne; }
    
    public String getTypeProduit() { return typeProduit; }
    public void setTypeProduit(String typeProduit) { this.typeProduit = typeProduit; }
    
    public int getIdItem() { return idItem; }
    public void setIdItem(int idItem) { this.idItem = idItem; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    @Override
    public String toString() {
        return "LignePanier{" +
                "typeProduit='" + typeProduit + '\'' +
                ", nom='" + nom + '\'' +
                ", quantite=" + quantite +
                ", sousTotal=" + getSousTotal() +
                '}';
    }
}
