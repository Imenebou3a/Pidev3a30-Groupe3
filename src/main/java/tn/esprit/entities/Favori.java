package tn.esprit.entities;

import java.time.LocalDateTime;

/**
 * Entité représentant un favori (produit ou kit)
 */
public class Favori {
    
    private int idFavori;
    private int idUtilisateur;
    private String typeItem; // "PRODUIT" ou "KIT"
    private int idItem;
    private LocalDateTime dateAjout;
    
    // Informations supplémentaires (non stockées en BD)
    private String nomItem;
    private String imageUrl;
    private String prix;
    
    public Favori() {
        this.dateAjout = LocalDateTime.now();
    }
    
    public Favori(int idUtilisateur, String typeItem, int idItem) {
        this();
        this.idUtilisateur = idUtilisateur;
        this.typeItem = typeItem;
        this.idItem = idItem;
    }
    
    // Getters et Setters
    public int getIdFavori() { return idFavori; }
    public void setIdFavori(int idFavori) { this.idFavori = idFavori; }
    
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    
    public String getTypeItem() { return typeItem; }
    public void setTypeItem(String typeItem) { this.typeItem = typeItem; }
    
    // Alias pour compatibilité
    public String getTypeFavori() { return typeItem != null ? typeItem.toLowerCase() : null; }
    public void setTypeFavori(String typeFavori) { this.typeItem = typeFavori != null ? typeFavori.toUpperCase() : null; }
    
    public int getIdItem() { return idItem; }
    public void setIdItem(int idItem) { this.idItem = idItem; }
    
    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }
    
    public String getNomItem() { return nomItem; }
    public void setNomItem(String nomItem) { this.nomItem = nomItem; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getPrix() { return prix; }
    public void setPrix(String prix) { this.prix = prix; }
    
    public boolean isProduit() {
        return "PRODUIT".equals(typeItem);
    }
    
    public boolean isKit() {
        return "KIT".equals(typeItem);
    }
    
    @Override
    public String toString() {
        return "Favori{" +
                "idFavori=" + idFavori +
                ", typeItem='" + typeItem + '\'' +
                ", idItem=" + idItem +
                ", nomItem='" + nomItem + '\'' +
                '}';
    }
}
