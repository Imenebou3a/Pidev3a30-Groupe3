package tn.esprit.services;

import tn.esprit.entities.Commande;
import tn.esprit.entities.LignePanier;
import tn.esprit.utils.MyDataBase;

import java.sql.*;

/**
 * Service pour gérer les commandes en base de données
 */
public class CommandeService {
    
    private Connection connection;
    private StockService stockService;
    
    public CommandeService() {
        this.connection = MyDataBase.getConnection();
        this.stockService = new StockService();
    }
    
    /**
     * Enregistre une commande en base de données
     * @param commande La commande à enregistrer
     * @return L'ID de la commande créée, ou -1 en cas d'erreur
     */
    public int enregistrerCommande(Commande commande) {
        String sqlCommande = "INSERT INTO commandes (nom_client, email_client, telephone_client, " +
                "adresse_client, sous_total, frais_livraison, total, date_commande, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        String sqlLigne = "INSERT INTO lignes_commande (id_commande, type_produit, id_item, " +
                "nom_item, prix_unitaire, quantite, sous_total) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
            connection.setAutoCommit(false);
            
            // Insérer la commande
            try (PreparedStatement pstCommande = connection.prepareStatement(sqlCommande, 
                    Statement.RETURN_GENERATED_KEYS)) {
                
                pstCommande.setString(1, commande.getNomClient());
                pstCommande.setString(2, commande.getEmailClient());
                pstCommande.setString(3, commande.getTelephoneClient());
                pstCommande.setString(4, commande.getAdresseClient());
                pstCommande.setBigDecimal(5, commande.getSousTotal());
                pstCommande.setBigDecimal(6, commande.getFraisLivraison());
                pstCommande.setBigDecimal(7, commande.getTotal());
                pstCommande.setTimestamp(8, Timestamp.valueOf(commande.getDateCommande()));
                pstCommande.setString(9, commande.getStatut());
                
                int rowsAffected = pstCommande.executeUpdate();
                
                if (rowsAffected > 0) {
                    ResultSet rs = pstCommande.getGeneratedKeys();
                    if (rs.next()) {
                        int idCommande = rs.getInt(1);
                        commande.setIdCommande(idCommande);
                        
                        // Insérer les lignes de commande ET décrémenter le stock
                        try (PreparedStatement pstLigne = connection.prepareStatement(sqlLigne)) {
                            for (LignePanier ligne : commande.getLignes()) {
                                pstLigne.setInt(1, idCommande);
                                pstLigne.setString(2, ligne.getTypeProduit());
                                pstLigne.setInt(3, ligne.getIdItem());
                                pstLigne.setString(4, ligne.getNom());
                                pstLigne.setBigDecimal(5, ligne.getPrixUnitaire());
                                pstLigne.setInt(6, ligne.getQuantite());
                                pstLigne.setBigDecimal(7, ligne.getSousTotal());
                                pstLigne.addBatch();
                                
                                // 🔥 NOUVEAU: Décrémenter automatiquement le stock
                                boolean stockDecremented = stockService.decrementerStock(
                                    ligne.getIdItem(),
                                    ligne.getTypeProduit(),
                                    ligne.getQuantite(),
                                    idCommande,
                                    "Vente - Commande #" + idCommande
                                );
                                
                                if (!stockDecremented) {
                                    System.err.println("⚠️ Attention: Stock non décrémenté pour " + ligne.getNom());
                                    // On continue quand même la commande
                                }
                            }
                            pstLigne.executeBatch();
                        }
                        
                        connection.commit();
                        System.out.println("✅ Commande enregistrée avec succès (ID: " + idCommande + ")");
                        return idCommande;
                    }
                }
            }
            
            connection.rollback();
            return -1;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("❌ Erreur lors du rollback : " + ex.getMessage());
            }
            System.err.println("❌ Erreur lors de l'enregistrement de la commande : " + e.getMessage());
            return -1;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la réactivation de l'autocommit : " + e.getMessage());
            }
        }
    }
}
