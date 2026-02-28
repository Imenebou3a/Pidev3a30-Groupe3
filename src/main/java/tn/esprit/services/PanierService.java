package tn.esprit.services;

import tn.esprit.entities.LignePanier;
import tn.esprit.interfaces.IPanier;
import tn.esprit.utils.MyDataBase;
import tn.esprit.utils.Session;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service Singleton pour gérer le panier de la session
 * Pattern créatif : un seul panier partagé pendant toute la session
 * Avec sauvegarde en base de données par utilisateur
 */
public class PanierService implements IPanier {
    
    private static PanierService instance;
    private List<LignePanier> lignes;
    private AtomicInteger compteurId;
    private Connection connection;
    
    private PanierService() {
        lignes = new ArrayList<>();
        compteurId = new AtomicInteger(1);
        connection = MyDataBase.getConnection();
    }
    
    // Singleton pattern
    public static synchronized PanierService getInstance() {
        if (instance == null) {
            instance = new PanierService();
        }
        return instance;
    }
    
    /**
     * Charger le panier depuis la base de données pour l'utilisateur connecté
     */
    public void chargerPanierUtilisateur() {
        if (Session.getInstance().getUtilisateurConnecte() == null) {
            return;
        }
        
        int idUtilisateur = Session.getInstance().getUtilisateurConnecte().getId();
        lignes.clear();
        
        String sql = "SELECT * FROM panier_utilisateur WHERE id = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idUtilisateur);
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    LignePanier ligne = new LignePanier(
                        rs.getString("type_produit"),
                        rs.getInt("id_item"),
                        rs.getString("nom"),
                        rs.getBigDecimal("prix_unitaire"),
                        rs.getInt("quantite"),
                        rs.getString("image_url"),
                        rs.getString("details")
                    );
                    ligne.setIdLigne(compteurId.getAndIncrement());
                    lignes.add(ligne);
                }
                System.out.println("✅ Panier chargé: " + lignes.size() + " article(s)");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement panier: " + e.getMessage());
        }
    }
    
    /**
     * Sauvegarder le panier en base de données
     */
    private void sauvegarderPanier() {
        if (Session.getInstance().getUtilisateurConnecte() == null) {
            return;
        }
        
        int idUtilisateur = Session.getInstance().getUtilisateurConnecte().getId();
        
        // Supprimer l'ancien panier
        String sqlDelete = "DELETE FROM panier_utilisateur WHERE id = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sqlDelete)) {
            pst.setInt(1, idUtilisateur);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression ancien panier: " + e.getMessage());
            return;
        }
        
        // Insérer le nouveau panier
        String sqlInsert = "INSERT INTO panier_utilisateur (id, type_produit, id_item, " +
                          "nom, prix_unitaire, quantite, details, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = connection.prepareStatement(sqlInsert)) {
            for (LignePanier ligne : lignes) {
                pst.setInt(1, idUtilisateur);
                pst.setString(2, ligne.getTypeProduit());
                pst.setInt(3, ligne.getIdItem());
                pst.setString(4, ligne.getNom());
                pst.setBigDecimal(5, ligne.getPrixUnitaire());
                pst.setInt(6, ligne.getQuantite());
                pst.setString(7, ligne.getDetails());
                pst.setString(8, ligne.getImageUrl());
                pst.addBatch();
            }
            pst.executeBatch();
        } catch (SQLException e) {
            System.err.println("❌ Erreur sauvegarde panier: " + e.getMessage());
        }
    }
    
    @Override
    public void ajouterProduit(int idProduit, String nom, BigDecimal prix, String details, String imageUrl) {
        // Vérifier si le produit existe déjà
        for (LignePanier ligne : lignes) {
            if ("PRODUIT".equals(ligne.getTypeProduit()) && ligne.getIdItem() == idProduit) {
                ligne.setQuantite(ligne.getQuantite() + 1);
                sauvegarderPanier();
                return;
            }
        }
        
        // Sinon créer nouvelle ligne
        LignePanier nouvelle = new LignePanier("PRODUIT", idProduit, nom, prix, 1, imageUrl, details);
        nouvelle.setIdLigne(compteurId.getAndIncrement());
        lignes.add(nouvelle);
        sauvegarderPanier();
    }
    
    @Override
    public void ajouterKit(int idKit, String nom, BigDecimal prix, String details, String imageUrl) {
        // Vérifier si le kit existe déjà
        for (LignePanier ligne : lignes) {
            if ("KIT".equals(ligne.getTypeProduit()) && ligne.getIdItem() == idKit) {
                ligne.setQuantite(ligne.getQuantite() + 1);
                sauvegarderPanier();
                return;
            }
        }
        
        // Sinon créer nouvelle ligne
        LignePanier nouvelle = new LignePanier("KIT", idKit, nom, prix, 1, imageUrl, details);
        nouvelle.setIdLigne(compteurId.getAndIncrement());
        lignes.add(nouvelle);
        sauvegarderPanier();
    }
    
    @Override
    public void modifierQuantite(int idLigne, int nouvelleQuantite) {
        if (nouvelleQuantite <= 0) {
            supprimerLigne(idLigne);
            return;
        }
        
        for (LignePanier ligne : lignes) {
            if (ligne.getIdLigne() == idLigne) {
                ligne.setQuantite(nouvelleQuantite);
                sauvegarderPanier();
                break;
            }
        }
    }
    
    @Override
    public void supprimerLigne(int idLigne) {
        lignes.removeIf(ligne -> ligne.getIdLigne() == idLigne);
        sauvegarderPanier();
    }
    
    @Override
    public void viderPanier() {
        lignes.clear();
        sauvegarderPanier();
    }
    
    @Override
    public List<LignePanier> getLignes() {
        return new ArrayList<>(lignes);
    }
    
    @Override
    public BigDecimal getTotal() {
        return lignes.stream()
                .map(LignePanier::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public int getNombreArticles() {
        return lignes.stream()
                .mapToInt(LignePanier::getQuantite)
                .sum();
    }
    
    // Méthode utilitaire pour obtenir une ligne spécifique
    public LignePanier getLigne(int idLigne) {
        return lignes.stream()
                .filter(l -> l.getIdLigne() == idLigne)
                .findFirst()
                .orElse(null);
    }
}
