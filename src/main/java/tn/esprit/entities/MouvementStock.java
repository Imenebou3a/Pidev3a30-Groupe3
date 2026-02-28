package tn.esprit.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MouvementStock {
    private int idMouvement;
    private int idProduit;
    private String typeProduit; // PRODUIT ou KIT
    private String typeMouvement; // ENTREE, SORTIE, AJUSTEMENT
    private int quantite;
    private int stockAvant;
    private int stockApres;
    private LocalDateTime dateMouvement;
    private String raison;
    private Integer idCommande;
    private Integer idUtilisateur;
    private String nomProduit; // Pour l'affichage

    // Constructeurs
    public MouvementStock() {
        this.dateMouvement = LocalDateTime.now();
    }

    public MouvementStock(int idProduit, String typeProduit, String typeMouvement, 
                         int quantite, int stockAvant, int stockApres, String raison) {
        this.idProduit = idProduit;
        this.typeProduit = typeProduit;
        this.typeMouvement = typeMouvement;
        this.quantite = quantite;
        this.stockAvant = stockAvant;
        this.stockApres = stockApres;
        this.raison = raison;
        this.dateMouvement = LocalDateTime.now();
    }

    // Getters et Setters
    public int getIdMouvement() {
        return idMouvement;
    }

    public void setIdMouvement(int idMouvement) {
        this.idMouvement = idMouvement;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public String getTypeProduit() {
        return typeProduit;
    }

    public void setTypeProduit(String typeProduit) {
        this.typeProduit = typeProduit;
    }

    public String getTypeMouvement() {
        return typeMouvement;
    }

    public void setTypeMouvement(String typeMouvement) {
        this.typeMouvement = typeMouvement;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public int getStockAvant() {
        return stockAvant;
    }

    public void setStockAvant(int stockAvant) {
        this.stockAvant = stockAvant;
    }

    public int getStockApres() {
        return stockApres;
    }

    public void setStockApres(int stockApres) {
        this.stockApres = stockApres;
    }

    public LocalDateTime getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(LocalDateTime dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public String getRaison() {
        return raison;
    }

    public void setRaison(String raison) {
        this.raison = raison;
    }

    public Integer getIdCommande() {
        return idCommande;
    }

    public void setIdCommande(Integer idCommande) {
        this.idCommande = idCommande;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    // Méthodes utilitaires
    public String getDateFormatee() {
        if (dateMouvement == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateMouvement.format(formatter);
    }

    public String getTypeMouvementFormate() {
        return switch (typeMouvement) {
            case "ENTREE" -> "➕ Entrée";
            case "SORTIE" -> "➖ Sortie";
            case "AJUSTEMENT" -> "⚙️ Ajustement";
            default -> typeMouvement;
        };
    }

    public String getVariationFormatee() {
        String signe = typeMouvement.equals("ENTREE") ? "+" : "-";
        return signe + quantite;
    }

    @Override
    public String toString() {
        return "MouvementStock{" +
                "idMouvement=" + idMouvement +
                ", idProduit=" + idProduit +
                ", typeProduit='" + typeProduit + '\'' +
                ", typeMouvement='" + typeMouvement + '\'' +
                ", quantite=" + quantite +
                ", stockAvant=" + stockAvant +
                ", stockApres=" + stockApres +
                ", dateMouvement=" + dateMouvement +
                ", raison='" + raison + '\'' +
                '}';
    }
}
