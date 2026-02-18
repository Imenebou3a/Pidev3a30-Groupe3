package tn.esprit.services;

import tn.esprit.entities.MouvementStock;
import tn.esprit.entities.AlerteStock;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockService {
    
    private Connection connection;
    
    public StockService() {
        this.connection = MyDataBase.getConnection();
    }
    
    // ========== GESTION DES MOUVEMENTS ==========
    
    /**
     * Enregistre un mouvement de stock
     */
    public boolean enregistrerMouvement(MouvementStock mouvement) {
        String sql = "INSERT INTO mouvements_stock (id_produit, type_produit, type_mouvement, " +
                    "quantite, stock_avant, stock_apres, raison, id_commande, id_utilisateur) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, mouvement.getIdProduit());
            pst.setString(2, mouvement.getTypeProduit());
            pst.setString(3, mouvement.getTypeMouvement());
            pst.setInt(4, mouvement.getQuantite());
            pst.setInt(5, mouvement.getStockAvant());
            pst.setInt(6, mouvement.getStockApres());
            pst.setString(7, mouvement.getRaison());
            
            if (mouvement.getIdCommande() != null) {
                pst.setInt(8, mouvement.getIdCommande());
            } else {
                pst.setNull(8, Types.INTEGER);
            }
            
            if (mouvement.getIdUtilisateur() != null) {
                pst.setInt(9, mouvement.getIdUtilisateur());
            } else {
                pst.setNull(9, Types.INTEGER);
            }
            
            int rows = pst.executeUpdate();
            System.out.println("✅ Mouvement de stock enregistré: " + mouvement);
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur enregistrement mouvement: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Décrémente le stock d'un produit (lors d'une vente)
     */
    public boolean decrementerStock(int idProduit, String typeProduit, int quantite, 
                                    Integer idCommande, String raison) {
        try {
            // 1. Récupérer le stock actuel
            int stockActuel = getStockActuel(idProduit, typeProduit);
            
            if (stockActuel < quantite) {
                System.err.println("❌ Stock insuffisant! Actuel: " + stockActuel + ", Demandé: " + quantite);
                return false;
            }
            
            // 2. Mettre à jour le stock
            int nouveauStock = stockActuel - quantite;
            boolean updated = mettreAJourStock(idProduit, typeProduit, nouveauStock);
            
            if (!updated) {
                return false;
            }
            
            // 3. Enregistrer le mouvement
            MouvementStock mouvement = new MouvementStock(
                idProduit, typeProduit, "SORTIE", 
                quantite, stockActuel, nouveauStock, raison
            );
            mouvement.setIdCommande(idCommande);
            
            return enregistrerMouvement(mouvement);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur décrémentation stock: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Incrémente le stock d'un produit (réapprovisionnement)
     */
    public boolean incrementerStock(int idProduit, String typeProduit, int quantite, String raison) {
        try {
            // 1. Récupérer le stock actuel
            int stockActuel = getStockActuel(idProduit, typeProduit);
            
            // 2. Mettre à jour le stock
            int nouveauStock = stockActuel + quantite;
            boolean updated = mettreAJourStock(idProduit, typeProduit, nouveauStock);
            
            if (!updated) {
                return false;
            }
            
            // 3. Enregistrer le mouvement
            MouvementStock mouvement = new MouvementStock(
                idProduit, typeProduit, "ENTREE", 
                quantite, stockActuel, nouveauStock, raison
            );
            
            return enregistrerMouvement(mouvement);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur incrémentation stock: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ajuste le stock manuellement
     */
    public boolean ajusterStock(int idProduit, String typeProduit, int nouveauStock, String raison) {
        try {
            int stockActuel = getStockActuel(idProduit, typeProduit);
            int difference = nouveauStock - stockActuel;
            
            boolean updated = mettreAJourStock(idProduit, typeProduit, nouveauStock);
            
            if (!updated) {
                return false;
            }
            
            MouvementStock mouvement = new MouvementStock(
                idProduit, typeProduit, "AJUSTEMENT", 
                Math.abs(difference), stockActuel, nouveauStock, raison
            );
            
            return enregistrerMouvement(mouvement);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur ajustement stock: " + e.getMessage());
            return false;
        }
    }
    
    // ========== MÉTHODES PRIVÉES ==========
    
    private int getStockActuel(int idProduit, String typeProduit) throws SQLException {
        String table = typeProduit.equals("PRODUIT") ? "produits_locaux" : "kits_hobbies";
        String idColumn = typeProduit.equals("PRODUIT") ? "id_produit" : "id_kit";
        
        String sql = "SELECT stock FROM " + table + " WHERE " + idColumn + " = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idProduit);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("stock");
            }
            throw new SQLException("Produit non trouvé");
        }
    }
    
    private boolean mettreAJourStock(int idProduit, String typeProduit, int nouveauStock) {
        String table = typeProduit.equals("PRODUIT") ? "produits_locaux" : "kits_hobbies";
        String idColumn = typeProduit.equals("PRODUIT") ? "id_produit" : "id_kit";
        
        String sql = "UPDATE " + table + " SET stock = ? WHERE " + idColumn + " = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, nouveauStock);
            pst.setInt(2, idProduit);
            
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour stock: " + e.getMessage());
            return false;
        }
    }
    
    // ========== CONSULTATION DES MOUVEMENTS ==========
    
    /**
     * Récupère tous les mouvements de stock
     */
    public List<MouvementStock> getMouvements() {
        List<MouvementStock> mouvements = new ArrayList<>();
        String sql = "SELECT * FROM vue_mouvements_stock ORDER BY date_mouvement DESC LIMIT 100";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                MouvementStock m = new MouvementStock();
                m.setIdMouvement(rs.getInt("id_mouvement"));
                m.setIdProduit(rs.getInt("id_produit"));
                m.setTypeProduit(rs.getString("type_produit"));
                m.setNomProduit(rs.getString("nom_produit"));
                m.setTypeMouvement(rs.getString("type_mouvement"));
                m.setQuantite(rs.getInt("quantite"));
                m.setStockAvant(rs.getInt("stock_avant"));
                m.setStockApres(rs.getInt("stock_apres"));
                m.setDateMouvement(rs.getTimestamp("date_mouvement").toLocalDateTime());
                m.setRaison(rs.getString("raison"));
                
                Integer idCmd = rs.getInt("id_commande");
                if (!rs.wasNull()) m.setIdCommande(idCmd);
                
                mouvements.add(m);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération mouvements: " + e.getMessage());
        }
        
        return mouvements;
    }
    
    /**
     * Récupère les mouvements d'un produit spécifique
     */
    public List<MouvementStock> getMouvementsProduit(int idProduit, String typeProduit) {
        List<MouvementStock> mouvements = new ArrayList<>();
        String sql = "SELECT * FROM vue_mouvements_stock " +
                    "WHERE id_produit = ? AND type_produit = ? " +
                    "ORDER BY date_mouvement DESC";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idProduit);
            pst.setString(2, typeProduit);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                MouvementStock m = new MouvementStock();
                m.setIdMouvement(rs.getInt("id_mouvement"));
                m.setIdProduit(rs.getInt("id_produit"));
                m.setTypeProduit(rs.getString("type_produit"));
                m.setNomProduit(rs.getString("nom_produit"));
                m.setTypeMouvement(rs.getString("type_mouvement"));
                m.setQuantite(rs.getInt("quantite"));
                m.setStockAvant(rs.getInt("stock_avant"));
                m.setStockApres(rs.getInt("stock_apres"));
                m.setDateMouvement(rs.getTimestamp("date_mouvement").toLocalDateTime());
                m.setRaison(rs.getString("raison"));
                
                mouvements.add(m);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération mouvements produit: " + e.getMessage());
        }
        
        return mouvements;
    }
    
    // ========== GESTION DES ALERTES ==========
    
    /**
     * Récupère toutes les alertes actives
     */
    public List<AlerteStock> getAlertesActives() {
        List<AlerteStock> alertes = new ArrayList<>();
        String sql = "SELECT * FROM vue_alertes_actives ORDER BY stock_actuel ASC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                AlerteStock alerte = new AlerteStock();
                alerte.setIdAlerte(rs.getInt("id_alerte"));
                alerte.setIdProduit(rs.getInt("id_produit"));
                alerte.setTypeProduit(rs.getString("type_produit"));
                alerte.setNomProduit(rs.getString("nom_produit"));
                alerte.setStockActuel(rs.getInt("stock_actuel"));
                alerte.setSeuilAlerte(rs.getInt("seuil_alerte"));
                alerte.setDateAlerte(rs.getTimestamp("date_alerte").toLocalDateTime());
                alerte.setVue(rs.getBoolean("vue"));
                alerte.setCategorie(rs.getString("categorie"));
                
                alertes.add(alerte);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération alertes: " + e.getMessage());
        }
        
        return alertes;
    }
    
    /**
     * Marque une alerte comme vue
     */
    public boolean marquerAlerteVue(int idAlerte) {
        String sql = "UPDATE alertes_stock SET vue = TRUE WHERE id_alerte = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idAlerte);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur marquage alerte: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Résout une alerte (après réapprovisionnement)
     */
    public boolean resoudreAlerte(int idAlerte) {
        String sql = "UPDATE alertes_stock SET resolue = TRUE, vue = TRUE WHERE id_alerte = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idAlerte);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur résolution alerte: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Compte le nombre d'alertes non vues
     */
    public int compterAlertesNonVues() {
        String sql = "SELECT COUNT(*) FROM alertes_stock WHERE vue = FALSE AND resolue = FALSE";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage alertes: " + e.getMessage());
        }
        
        return 0;
    }
}
