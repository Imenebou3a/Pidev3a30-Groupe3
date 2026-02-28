package tn.esprit.services;

import tn.esprit.entities.Favori;
import tn.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les favoris (produits et kits)
 */
public class FavoriService {
    
    private Connection connection;
    
    public FavoriService() {
        connection = DataSource.getInstance().getConnection();
    }
    
    /**
     * Ajouter un produit/kit aux favoris
     */
    public boolean ajouterFavori(Favori favori) {
        String query = "INSERT INTO favoris (id_utilisateur, type_item, id_item) VALUES (?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, favori.getIdUtilisateur());
            ps.setString(2, favori.getTypeItem());
            ps.setInt(3, favori.getIdItem());
            
            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("✅ Favori ajouté: " + favori.getTypeItem() + " #" + favori.getIdItem());
                return true;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                System.out.println("⚠️ Déjà dans les favoris");
            } else {
                System.err.println("❌ Erreur ajout favori: " + e.getMessage());
            }
        }
        return false;
    }
    
    /**
     * Retirer un produit/kit des favoris
     */
    public boolean retirerFavori(int idUtilisateur, String typeItem, int idItem) {
        String query = "DELETE FROM favoris WHERE id_utilisateur = ? AND type_item = ? AND id_item = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idUtilisateur);
            ps.setString(2, typeItem);
            ps.setInt(3, idItem);
            
            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("✅ Favori retiré");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur retrait favori: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Vérifier si un item est dans les favoris
     */
    public boolean estFavori(int idUtilisateur, String typeItem, int idItem) {
        String query = "SELECT COUNT(*) FROM favoris WHERE id_utilisateur = ? AND type_item = ? AND id_item = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idUtilisateur);
            ps.setString(2, typeItem);
            ps.setInt(3, idItem);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification favori: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Récupérer tous les favoris d'un utilisateur
     */
    public List<Favori> getFavorisUtilisateur(int idUtilisateur) {
        List<Favori> favoris = new ArrayList<>();
        String query = "SELECT * FROM favoris WHERE id_utilisateur = ? ORDER BY date_ajout DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Favori favori = new Favori();
                favori.setIdFavori(rs.getInt("id_favori"));
                favori.setIdUtilisateur(rs.getInt("id_utilisateur"));
                favori.setTypeItem(rs.getString("type_item"));
                favori.setIdItem(rs.getInt("id_item"));
                favori.setDateAjout(rs.getTimestamp("date_ajout").toLocalDateTime());
                
                // Charger les détails du produit/kit
                chargerDetailsItem(favori);
                
                favoris.add(favori);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération favoris: " + e.getMessage());
        }
        
        return favoris;
    }
    
    // Alias pour compatibilité
    public List<Favori> getFavorisByUtilisateur(int idUtilisateur) {
        return getFavorisUtilisateur(idUtilisateur);
    }
    
    /**
     * Charger les détails d'un produit ou kit favori
     */
    private void chargerDetailsItem(Favori favori) {
        if (favori.isProduit()) {
            // Pour les produits
            String query = "SELECT nom, prix, image_url FROM produit_local WHERE id_produit = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, favori.getIdItem());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    favori.setNomItem(rs.getString("nom"));
                    favori.setPrix(rs.getString("prix") + " TND");
                    favori.setImageUrl(rs.getString("image_url"));
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur chargement détails produit: " + e.getMessage());
            }
        } else {
            // Pour les kits
            String query = "SELECT nom_kit, prix, image_url FROM kit_hobby_artisanal WHERE id_kit = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, favori.getIdItem());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    favori.setNomItem(rs.getString("nom_kit"));
                    favori.setPrix(rs.getString("prix") + " TND");
                    favori.setImageUrl(rs.getString("image_url"));
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur chargement détails kit: " + e.getMessage());
            }
        }
    }
    
    /**
     * Compter le nombre de favoris d'un utilisateur
     */
    public int compterFavoris(int idUtilisateur) {
        String query = "SELECT COUNT(*) FROM favoris WHERE id_utilisateur = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage favoris: " + e.getMessage());
        }
        return 0;
    }
}
