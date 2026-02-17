package tn.esprit.services;

import tn.esprit.entities.ProduitLocal;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitLocalService {

    private Connection connection;

    public ProduitLocalService() {
        this.connection = MyDataBase.getConnection();
    }

    public void ajouter(ProduitLocal produit) {
        String sql = "INSERT INTO produit_local (nom, description, prix, categorie, region, stock, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, produit.getNom());
            pst.setString(2, produit.getDescription());
            pst.setBigDecimal(3, produit.getPrix());
            pst.setString(4, produit.getCategorie());
            pst.setString(5, produit.getRegion());
            pst.setInt(6, produit.getStock());
            pst.setString(7, produit.getImageUrl());
            pst.executeUpdate();
            System.out.println("Produit ajoute: " + produit.getNom());
        } catch (SQLException e) {
            System.err.println("Erreur ajout produit: " + e.getMessage());
        }
    }

    public void modifier(ProduitLocal produit) {
        String sql = "UPDATE produit_local SET nom=?, description=?, prix=?, categorie=?, region=?, stock=?, image_url=? WHERE id_produit=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, produit.getNom());
            pst.setString(2, produit.getDescription());
            pst.setBigDecimal(3, produit.getPrix());
            pst.setString(4, produit.getCategorie());
            pst.setString(5, produit.getRegion());
            pst.setInt(6, produit.getStock());
            pst.setString(7, produit.getImageUrl());
            pst.setInt(8, produit.getIdProduit());
            pst.executeUpdate();
            System.out.println("Produit modifie: " + produit.getNom());
        } catch (SQLException e) {
            System.err.println("Erreur modification produit: " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM produit_local WHERE id_produit=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Produit supprime (ID: " + id + ")");
        } catch (SQLException e) {
            System.err.println("Erreur suppression produit: " + e.getMessage());
        }
    }

    public List<ProduitLocal> afficher() {
        List<ProduitLocal> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit_local ORDER BY id_produit DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) produits.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur affichage produits: " + e.getMessage());
        }
        return produits;
    }

    public ProduitLocal getById(int id) {
        String sql = "SELECT * FROM produit_local WHERE id_produit=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getById produit: " + e.getMessage());
        }
        return null;
    }

    public List<ProduitLocal> rechercherParNom(String nom) {
        List<ProduitLocal> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit_local WHERE nom LIKE ? ORDER BY nom";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, "%" + nom + "%");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) produits.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche produit: " + e.getMessage());
        }
        return produits;
    }

    public List<ProduitLocal> filtrerParCategorie(String categorie) {
        List<ProduitLocal> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit_local WHERE categorie=? ORDER BY nom";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, categorie);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) produits.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur filtre categorie: " + e.getMessage());
        }
        return produits;
    }

    public List<ProduitLocal> filtrerParRegion(String region) {
        List<ProduitLocal> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit_local WHERE region=? ORDER BY nom";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, region);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) produits.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur filtre region: " + e.getMessage());
        }
        return produits;
    }

    private ProduitLocal mapResultSet(ResultSet rs) throws SQLException {
        return new ProduitLocal(
                rs.getInt("id_produit"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getBigDecimal("prix"),
                rs.getString("categorie"),
                rs.getString("region"),
                rs.getInt("stock"),
                rs.getString("image_url")
        );
    }
}