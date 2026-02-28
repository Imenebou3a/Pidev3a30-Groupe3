package tn.esprit.entities;

import java.math.BigDecimal;

/**
 * Entité représentant une recommandation IA
 */
public class Recommendation {
    
    private int idRecommandation;
    private String typeItem; // "PRODUIT" ou "KIT"
    private int idItem;
    private String nomItem;
    private String description;
    private BigDecimal prix;
    private String imageUrl;
    private double scorePertinence; // 0-100
    private String raisonRecommandation; // Pourquoi ce produit est recommandé
    
    public Recommendation() {}
    
    public Recommendation(String typeItem, int idItem, String nomItem, double scorePertinence) {
        this.typeItem = typeItem;
        this.idItem = idItem;
        this.nomItem = nomItem;
        this.scorePertinence = scorePertinence;
    }
    
    // Getters et Setters
    public int getIdRecommandation() { return idRecommandation; }
    public void setIdRecommandation(int idRecommandation) { this.idRecommandation = idRecommandation; }
    
    public String getTypeItem() { return typeItem; }
    public void setTypeItem(String typeItem) { this.typeItem = typeItem; }
    
    // Alias pour compatibilité
    public String getTypeRecommandation() { return typeItem != null ? typeItem.toLowerCase() : null; }
    public void setTypeRecommandation(String typeRecommandation) { 
        this.typeItem = typeRecommandation != null ? typeRecommandation.toUpperCase() : null; 
    }
    
    public int getIdItem() { return idItem; }
    public void setIdItem(int idItem) { this.idItem = idItem; }
    
    public String getNomItem() { return nomItem; }
    public void setNomItem(String nomItem) { this.nomItem = nomItem; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public double getScorePertinence() { return scorePertinence; }
    public void setScorePertinence(double scorePertinence) { this.scorePertinence = scorePertinence; }
    
    public String getRaisonRecommandation() { return raisonRecommandation; }
    public void setRaisonRecommandation(String raisonRecommandation) { 
        this.raisonRecommandation = raisonRecommandation; 
    }
    
    public String getScoreFormate() {
        return String.format("%.0f%%", scorePertinence);
    }
    
    @Override
    public String toString() {
        return "Recommendation{" +
                "typeItem='" + typeItem + '\'' +
                ", nomItem='" + nomItem + '\'' +
                ", scorePertinence=" + scorePertinence +
                '}';
    }
}
