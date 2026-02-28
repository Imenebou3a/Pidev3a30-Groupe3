package tn.esprit.entities;

import java.math.BigDecimal;

public class KitHobbies {

    private int idKit;
    private String nomKit;
    private String description;
    private String typeArtisanat;
    private String niveauDifficulte;
    private BigDecimal prix;
    private int stock;
    private String imageUrl;
    private int idProduit;
    private String nomProduit;

    public KitHobbies() {}

    public KitHobbies(int idKit, String nomKit, String description, String typeArtisanat,
                      String niveauDifficulte, BigDecimal prix, int stock,
                      String imageUrl, int idProduit) {
        this.idKit = idKit;
        this.nomKit = nomKit;
        this.description = description;
        this.typeArtisanat = typeArtisanat;
        this.niveauDifficulte = niveauDifficulte;
        this.prix = prix;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.idProduit = idProduit;
    }

    public KitHobbies(String nomKit, String description, String typeArtisanat,
                      String niveauDifficulte, BigDecimal prix, int stock,
                      String imageUrl, int idProduit) {
        this.nomKit = nomKit;
        this.description = description;
        this.typeArtisanat = typeArtisanat;
        this.niveauDifficulte = niveauDifficulte;
        this.prix = prix;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.idProduit = idProduit;
    }

    public int getIdKit() { return idKit; }
    public void setIdKit(int idKit) { this.idKit = idKit; }
    public String getNomKit() { return nomKit; }
    public void setNomKit(String nomKit) { this.nomKit = nomKit; }
    public String getNom() { return nomKit; }
    public void setNom(String nom) { this.nomKit = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTypeArtisanat() { return typeArtisanat; }
    public void setTypeArtisanat(String typeArtisanat) { this.typeArtisanat = typeArtisanat; }
    public String getNiveauDifficulte() { return niveauDifficulte; }
    public void setNiveauDifficulte(String niveauDifficulte) { this.niveauDifficulte = niveauDifficulte; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }
    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }

    @Override
    public String toString() {
        return "KitHobbies{idKit=" + idKit + ", nomKit='" + nomKit + "', prix=" + prix + '}';
    }
}