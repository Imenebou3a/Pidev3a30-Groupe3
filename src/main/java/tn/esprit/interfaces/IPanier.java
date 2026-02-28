package tn.esprit.interfaces;

import tn.esprit.entities.LignePanier;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interface pour la gestion du panier
 */
public interface IPanier {
    
    void ajouterProduit(int idProduit, String nom, BigDecimal prix, String details, String imageUrl);
    
    void ajouterKit(int idKit, String nom, BigDecimal prix, String details, String imageUrl);
    
    void modifierQuantite(int idLigne, int nouvelleQuantite);
    
    void supprimerLigne(int idLigne);
    
    void viderPanier();
    
    List<LignePanier> getLignes();
    
    BigDecimal getTotal();
    
    int getNombreArticles();
}
