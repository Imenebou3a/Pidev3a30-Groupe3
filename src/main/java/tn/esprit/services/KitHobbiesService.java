package tn.esprit.services;

import tn.esprit.entities.KitHobbies;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KitHobbiesService {

    private Connection connection;

    public KitHobbiesService() {
        this.connection = MyDataBase.getConnection();
    }

    public void ajouter(KitHobbies kit) {
        String sql = "INSERT INTO kit_hobby_artisanal (nom_kit, description, type_artisanat, niveau_difficulte, prix, stock, image_url, id_produit) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, kit.getNomKit());
            pst.setString(2, kit.getDescription());
            pst.setString(3, kit.getTypeArtisanat());
            pst.setString(4, kit.getNiveauDifficulte());
            pst.setBigDecimal(5, kit.getPrix());
            pst.setInt(6, kit.getStock());
            pst.setString(7, kit.getImageUrl());
            pst.setInt(8, kit.getIdProduit());
            pst.executeUpdate();
            System.out.println("Kit ajoute: " + kit.getNomKit());
        } catch (SQLException e) {
            System.err.println("Erreur ajout kit: " + e.getMessage());
        }
    }

    public void modifier(KitHobbies kit) {
        String sql = "UPDATE kit_hobby_artisanal SET nom_kit=?, description=?, type_artisanat=?, niveau_difficulte=?, prix=?, stock=?, image_url=?, id_produit=? WHERE id_kit=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, kit.getNomKit());
            pst.setString(2, kit.getDescription());
            pst.setString(3, kit.getTypeArtisanat());
            pst.setString(4, kit.getNiveauDifficulte());
            pst.setBigDecimal(5, kit.getPrix());
            pst.setInt(6, kit.getStock());
            pst.setString(7, kit.getImageUrl());
            pst.setInt(8, kit.getIdProduit());
            pst.setInt(9, kit.getIdKit());
            pst.executeUpdate();
            System.out.println("Kit modifie: " + kit.getNomKit());
        } catch (SQLException e) {
            System.err.println("Erreur modification kit: " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM kit_hobby_artisanal WHERE id_kit=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Kit supprime (ID: " + id + ")");
        } catch (SQLException e) {
            System.err.println("Erreur suppression kit: " + e.getMessage());
        }
    }

    public List<KitHobbies> afficher() {
        List<KitHobbies> kits = new ArrayList<>();
        String sql = "SELECT * FROM kit_hobby_artisanal ORDER BY id_kit DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) kits.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur affichage kits: " + e.getMessage());
        }
        return kits;
    }

    public KitHobbies getById(int id) {
        String sql = "SELECT * FROM kit_hobby_artisanal WHERE id_kit=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getById kit: " + e.getMessage());
        }
        return null;
    }

    public List<KitHobbies> rechercherParNom(String nom) {
        List<KitHobbies> kits = new ArrayList<>();
        String sql = "SELECT * FROM kit_hobby_artisanal WHERE nom_kit LIKE ? ORDER BY nom_kit";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, "%" + nom + "%");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) kits.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche kit: " + e.getMessage());
        }
        return kits;
    }

    public List<KitHobbies> filtrerParType(String type) {
        List<KitHobbies> kits = new ArrayList<>();
        String sql = "SELECT * FROM kit_hobby_artisanal WHERE type_artisanat=? ORDER BY nom_kit";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, type);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) kits.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur filtrage par type: " + e.getMessage());
        }
        return kits;
    }

    public List<KitHobbies> filtrerParNiveau(String niveau) {
        List<KitHobbies> kits = new ArrayList<>();
        String sql = "SELECT * FROM kit_hobby_artisanal WHERE niveau_difficulte=? ORDER BY nom_kit";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, niveau);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) kits.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur filtrage par niveau: " + e.getMessage());
        }
        return kits;
    }

    private KitHobbies mapResultSet(ResultSet rs) throws SQLException {
        return new KitHobbies(
                rs.getInt("id_kit"),
                rs.getString("nom_kit"),
                rs.getString("description"),
                rs.getString("type_artisanat"),
                rs.getString("niveau_difficulte"),
                rs.getBigDecimal("prix"),
                rs.getInt("stock"),
                rs.getString("image_url"),
                rs.getInt("id_produit")
        );
    }
}