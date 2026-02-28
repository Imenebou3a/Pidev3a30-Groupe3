package tn.esprit.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.entities.ProduitLocal;
import tn.esprit.entities.KitHobbies;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton pour gérer le panier d'achat
 */
public class Panier {
    private static Panier instance;
    
    private Map<Integer, ItemPanier> produits = new HashMap<>();
    private Map<Integer, ItemPanier> kits = new HashMap<>();
    
    private Panier() {}
    
    public static Panier getInstance() {
        if (instance == null) {
            instance = new Panier();
        }
        return instance;
    }
    
    // Ajouter un produit
    public void ajouterProduit(ProduitLocal produit, int quantite) {
        int id = produit.getIdProduit();
        if (produits.containsKey(id)) {
            ItemPanier item = produits.get(id);
            item.setQuantite(item.getQuantite() + quantite);
        } else {
            produits.put(id, new ItemPanier(produit.getNom(), produit.getPrix(), quantite, "PRODUIT"));
        }
    }
    
    // Ajouter un kit
    public void ajouterKit(KitHobbies kit, int quantite) {
        int id = kit.getIdKit();
        if (kits.containsKey(id)) {
            ItemPanier item = kits.get(id);
            item.setQuantite(item.getQuantite() + quantite);
        } else {
            kits.put(id, new ItemPanier(kit.getNomKit(), kit.getPrix(), quantite, "KIT"));
        }
    }
    
    // Retirer un produit
    public void retirerProduit(int id) {
        produits.remove(id);
    }
    
    // Retirer un kit
    public void retirerKit(int id) {
        kits.remove(id);
    }
    
    // Obtenir le nombre total d'articles
    public int getNombreArticles() {
        int total = 0;
        for (ItemPanier item : produits.values()) {
            total += item.getQuantite();
        }
        for (ItemPanier item : kits.values()) {
            total += item.getQuantite();
        }
        return total;
    }
    
    // Obtenir le total
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPanier item : produits.values()) {
            total = total.add(item.getPrix().multiply(BigDecimal.valueOf(item.getQuantite())));
        }
        for (ItemPanier item : kits.values()) {
            total = total.add(item.getPrix().multiply(BigDecimal.valueOf(item.getQuantite())));
        }
        return total;
    }
    
    // Vider le panier
    public void vider() {
        produits.clear();
        kits.clear();
    }
    
    // Obtenir tous les items
    public ObservableList<ItemPanier> getTousLesItems() {
        ObservableList<ItemPanier> items = FXCollections.observableArrayList();
        items.addAll(produits.values());
        items.addAll(kits.values());
        return items;
    }
    
    // Classe interne pour représenter un item du panier
    public static class ItemPanier {
        private String nom;
        private BigDecimal prix;
        private int quantite;
        private String type;
        
        public ItemPanier(String nom, BigDecimal prix, int quantite, String type) {
            this.nom = nom;
            this.prix = prix;
            this.quantite = quantite;
            this.type = type;
        }
        
        public String getNom() { return nom; }
        public BigDecimal getPrix() { return prix; }
        public int getQuantite() { return quantite; }
        public void setQuantite(int quantite) { this.quantite = quantite; }
        public String getType() { return type; }
        
        public BigDecimal getSousTotal() {
            return prix.multiply(BigDecimal.valueOf(quantite));
        }
    }
}
