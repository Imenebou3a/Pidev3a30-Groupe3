package tn.esprit.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlerteStock {
    private int idAlerte;
    private int idProduit;
    private String typeProduit; // PRODUIT ou KIT
    private String nomProduit;
    private int stockActuel;
    private int seuilAlerte;
    private LocalDateTime dateAlerte;
    private boolean vue;
    private boolean resolue;
    private String categorie; // Pour l'affichage

    // Constructeurs
    public AlerteStock() {
        this.dateAlerte = LocalDateTime.now();
        this.vue = false;
        this.resolue = false;
        this.seuilAlerte = 5;
    }

    public AlerteStock(int idProduit, String typeProduit, String nomProduit, int stockActuel) {
        this();
        this.idProduit = idProduit;
        this.typeProduit = typeProduit;
        this.nomProduit = nomProduit;
        this.stockActuel = stockActuel;
    }

    // Getters et Setters
    public int getIdAlerte() {
        return idAlerte;
    }

    public void setIdAlerte(int idAlerte) {
        this.idAlerte = idAlerte;
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

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public int getStockActuel() {
        return stockActuel;
    }

    public void setStockActuel(int stockActuel) {
        this.stockActuel = stockActuel;
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    public void setSeuilAlerte(int seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    public LocalDateTime getDateAlerte() {
        return dateAlerte;
    }

    public void setDateAlerte(LocalDateTime dateAlerte) {
        this.dateAlerte = dateAlerte;
    }

    public boolean isVue() {
        return vue;
    }

    public void setVue(boolean vue) {
        this.vue = vue;
    }

    public boolean isResolue() {
        return resolue;
    }

    public void setResolue(boolean resolue) {
        this.resolue = resolue;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    // Méthodes utilitaires
    public String getDateFormatee() {
        if (dateAlerte == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateAlerte.format(formatter);
    }

    public String getNiveauUrgence() {
        if (stockActuel == 0) return "CRITIQUE";
        if (stockActuel <= 2) return "URGENT";
        if (stockActuel <= 5) return "ATTENTION";
        return "NORMAL";
    }

    public String getIconeUrgence() {
        return switch (getNiveauUrgence()) {
            case "CRITIQUE" -> "🔴";
            case "URGENT" -> "🟠";
            case "ATTENTION" -> "🟡";
            default -> "🟢";
        };
    }

    @Override
    public String toString() {
        return "AlerteStock{" +
                "idAlerte=" + idAlerte +
                ", nomProduit='" + nomProduit + '\'' +
                ", stockActuel=" + stockActuel +
                ", seuilAlerte=" + seuilAlerte +
                ", urgence=" + getNiveauUrgence() +
                '}';
    }
}
