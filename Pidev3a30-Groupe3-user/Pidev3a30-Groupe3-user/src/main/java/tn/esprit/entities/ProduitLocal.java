package tn.esprit.entities;

import java.math.BigDecimal;

public class ProduitLocal {

    private int idProduit;
    private String nom;
    private String description;
    private BigDecimal prix;
    private String categorie;
    private String region;
    private int stock;
    private String imageUrl;

    public ProduitLocal() {}

    public ProduitLocal(int idProduit, String nom, String description, BigDecimal prix,
                        String categorie, String region, int stock, String imageUrl) {
        this.idProduit = idProduit;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.categorie = categorie;
        this.region = region;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public ProduitLocal(String nom, String description, BigDecimal prix,
                        String categorie, String region, int stock, String imageUrl) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.categorie = categorie;
        this.region = region;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "ProduitLocal{idProduit=" + idProduit + ", nom='" + nom + "', prix=" + prix + '}';
    }
}