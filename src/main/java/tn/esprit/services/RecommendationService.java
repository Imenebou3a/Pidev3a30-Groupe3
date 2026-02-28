package tn.esprit.services;

import tn.esprit.entities.Favori;
import tn.esprit.entities.Recommendation;
import tn.esprit.utils.DataSource;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de recommandations intelligentes basé sur les favoris et l'historique
 */
public class RecommendationService {
    
    private Connection connection;
    private FavoriService favoriService;
    
    public RecommendationService() {
        connection = DataSource.getInstance().getConnection();
        favoriService = new FavoriService();
    }
    
    /**
     * Obtenir des recommandations personnalisées pour un utilisateur
     * Basé sur ses favoris et son historique d'achat
     */
    public List<Recommendation> getRecommandations(int idUtilisateur, int limite) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // 1. Analyser les favoris de l'utilisateur
        List<Favori> favoris = favoriService.getFavorisUtilisateur(idUtilisateur);
        
        if (favoris.isEmpty()) {
            // Si pas de favoris, recommander les produits populaires
            return getProduitsPopulaires(limite);
        }
        
        // 2. Extraire les catégories et régions préférées
        Map<String, Integer> categoriesCount = new HashMap<>();
        Map<String, Integer> regionsCount = new HashMap<>();
        
        for (Favori favori : favoris) {
            if (favori.isProduit()) {
                analyserProduit(favori.getIdItem(), categoriesCount, regionsCount);
            } else {
                analyserKit(favori.getIdItem(), categoriesCount);
            }
        }
        
        // 3. Trouver les catégories/régions les plus aimées
        String categoriePreferee = getMostFrequent(categoriesCount);
        String regionPreferee = getMostFrequent(regionsCount);
        
        System.out.println("📊 Préférences détectées:");
        System.out.println("   Catégorie: " + categoriePreferee);
        System.out.println("   Région: " + regionPreferee);
        
        // 4. Recommander des produits similaires
        recommendations.addAll(recommanderProduitsParCategorie(idUtilisateur, categoriePreferee, limite / 2));
        recommendations.addAll(recommanderProduitsParRegion(idUtilisateur, regionPreferee, limite / 2));
        recommendations.addAll(recommanderKitsParType(idUtilisateur, categoriePreferee, limite / 2));
        
        // 5. Trier par score de pertinence et limiter
        return recommendations.stream()
                .sorted((r1, r2) -> Double.compare(r2.getScorePertinence(), r1.getScorePertinence()))
                .limit(limite)
                .collect(Collectors.toList());
    }
    
    /**
     * Alias pour compatibilité avec les contrôleurs
     */
    public List<Recommendation> genererRecommandations(int idUtilisateur, int limite) {
        return getRecommandations(idUtilisateur, limite);
    }
    
    /**
     * Analyser un produit pour extraire catégorie et région
     */
    private void analyserProduit(int idProduit, Map<String, Integer> categoriesCount, Map<String, Integer> regionsCount) {
        String query = "SELECT categorie, region FROM produit_local WHERE id_produit = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idProduit);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String categorie = rs.getString("categorie");
                String region = rs.getString("region");
                
                categoriesCount.put(categorie, categoriesCount.getOrDefault(categorie, 0) + 1);
                regionsCount.put(region, regionsCount.getOrDefault(region, 0) + 1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur analyse produit: " + e.getMessage());
        }
    }
    
    /**
     * Analyser un kit pour extraire le type
     */
    private void analyserKit(int idKit, Map<String, Integer> categoriesCount) {
        String query = "SELECT type_artisanat FROM kit_hobby_artisanal WHERE id_kit = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idKit);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String type = rs.getString("type_artisanat");
                categoriesCount.put(type, categoriesCount.getOrDefault(type, 0) + 1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur analyse kit: " + e.getMessage());
        }
    }
    
    /**
     * Trouver l'élément le plus fréquent dans une map
     */
    private String getMostFrequent(Map<String, Integer> map) {
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    /**
     * Recommander des produits de la même catégorie
     */
    private List<Recommendation> recommanderProduitsParCategorie(int idUtilisateur, String categorie, int limite) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        if (categorie == null) return recommendations;
        
        String query = "SELECT p.* FROM produit_local p " +
                      "WHERE p.categorie = ? " +
                      "AND p.id_produit NOT IN (SELECT id_item FROM favoris WHERE id_utilisateur = ? AND type_item = 'PRODUIT') " +
                      "ORDER BY RAND() LIMIT ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, categorie);
            ps.setInt(2, idUtilisateur);
            ps.setInt(3, limite);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Recommendation rec = new Recommendation();
                rec.setTypeItem("PRODUIT");
                rec.setIdItem(rs.getInt("id_produit"));
                rec.setNomItem(rs.getString("nom"));
                rec.setDescription(rs.getString("description"));
                rec.setPrix(rs.getBigDecimal("prix"));
                rec.setImageUrl(rs.getString("image_url"));
                rec.setScorePertinence(85.0 + Math.random() * 15);
                rec.setRaisonRecommandation("Même catégorie que vos favoris: " + categorie);
                
                recommendations.add(rec);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recommandation catégorie: " + e.getMessage());
        }
        
        return recommendations;
    }
    
    /**
     * Recommander des produits de la même région
     */
    private List<Recommendation> recommanderProduitsParRegion(int idUtilisateur, String region, int limite) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        if (region == null) return recommendations;
        
        String query = "SELECT p.* FROM produit_local p " +
                      "WHERE p.region = ? " +
                      "AND p.id_produit NOT IN (SELECT id_item FROM favoris WHERE id_utilisateur = ? AND type_item = 'PRODUIT') " +
                      "ORDER BY RAND() LIMIT ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, region);
            ps.setInt(2, idUtilisateur);
            ps.setInt(3, limite);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Recommendation rec = new Recommendation();
                rec.setTypeItem("PRODUIT");
                rec.setIdItem(rs.getInt("id_produit"));
                rec.setNomItem(rs.getString("nom"));
                rec.setDescription(rs.getString("description"));
                rec.setPrix(rs.getBigDecimal("prix"));
                rec.setImageUrl(rs.getString("image_url"));
                rec.setScorePertinence(75.0 + Math.random() * 15);
                rec.setRaisonRecommandation("Artisanat de " + region);
                
                recommendations.add(rec);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recommandation région: " + e.getMessage());
        }
        
        return recommendations;
    }
    
    /**
     * Recommander des kits du même type
     */
    private List<Recommendation> recommanderKitsParType(int idUtilisateur, String type, int limite) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        if (type == null) return recommendations;
        
        String query = "SELECT k.* FROM kit_hobby_artisanal k " +
                      "WHERE k.type_artisanat = ? " +
                      "AND k.id_kit NOT IN (SELECT id_item FROM favoris WHERE id_utilisateur = ? AND type_item = 'KIT') " +
                      "ORDER BY RAND() LIMIT ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, type);
            ps.setInt(2, idUtilisateur);
            ps.setInt(3, limite);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Recommendation rec = new Recommendation();
                rec.setTypeItem("KIT");
                rec.setIdItem(rs.getInt("id_kit"));
                rec.setNomItem(rs.getString("nom_kit"));
                rec.setDescription(rs.getString("description"));
                rec.setPrix(rs.getBigDecimal("prix"));
                rec.setImageUrl(rs.getString("image_url"));
                rec.setScorePertinence(80.0 + Math.random() * 15);
                rec.setRaisonRecommandation("Kit " + type + " recommandé pour vous");
                
                recommendations.add(rec);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recommandation kits: " + e.getMessage());
        }
        
        return recommendations;
    }
    
    /**
     * Obtenir les produits les plus populaires (fallback)
     */
    private List<Recommendation> getProduitsPopulaires(int limite) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        String query = "SELECT p.*, COUNT(f.id_favori) as popularite " +
                      "FROM produit_local p " +
                      "LEFT JOIN favoris f ON f.id_item = p.id_produit AND f.type_item = 'PRODUIT' " +
                      "GROUP BY p.id_produit " +
                      "ORDER BY popularite DESC, RAND() " +
                      "LIMIT ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, limite);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Recommendation rec = new Recommendation();
                rec.setTypeItem("PRODUIT");
                rec.setIdItem(rs.getInt("id_produit"));
                rec.setNomItem(rs.getString("nom"));
                rec.setDescription(rs.getString("description"));
                rec.setPrix(rs.getBigDecimal("prix"));
                rec.setImageUrl(rs.getString("image_url"));
                rec.setScorePertinence(70.0 + Math.random() * 10);
                rec.setRaisonRecommandation("Produit populaire");
                
                recommendations.add(rec);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur produits populaires: " + e.getMessage());
        }
        
        return recommendations;
    }
}
