package tn.esprit.interfaces;

import tn.esprit.entities.ProduitLocal;
import java.util.List;

/**
 * Interface pour la gestion des produits locaux
 */
public interface IProduitLocal {
    
    void ajouter(ProduitLocal produit);
    
    void modifier(ProduitLocal produit);
    
    void supprimer(int id);
    
    List<ProduitLocal> afficher();
    
    ProduitLocal getById(int id);
    
    List<ProduitLocal> rechercherParNom(String nom);
    
    List<ProduitLocal> filtrerParCategorie(String categorie);
    
    List<ProduitLocal> filtrerParRegion(String region);
    
    List<ProduitLocal> getProduitsEnStock();
}
