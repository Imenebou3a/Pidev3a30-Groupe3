package tn.esprit.services;

import tn.esprit.entities.Reclamation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceReclamation implements IService<Reclamation> {

    private Connection connection;

    public ServiceReclamation() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(Reclamation r) throws SQLException {
        String sql = "INSERT INTO reclamation (utilisateur_id, sujet, description, categorie, statut, priorite, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, r.getUtilisateurId());
        ps.setString(2, r.getSujet());
        ps.setString(3, r.getDescription());
        ps.setString(4, r.getCategorie());
        ps.setString(5, r.getStatut());
        ps.setString(6, r.getPriorite());
        ps.setTimestamp(7, Timestamp.valueOf(r.getDateCreation()));
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            r.setId(rs.getInt(1));
        }
    }

    @Override
    public void modifier(Reclamation r) throws SQLException {
        String sql = "UPDATE reclamation SET sujet=?, description=?, categorie=?, statut=?, priorite=?, reponse_admin=?, date_modification=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, r.getSujet());
        ps.setString(2, r.getDescription());
        ps.setString(3, r.getCategorie());
        ps.setString(4, r.getStatut());
        ps.setString(5, r.getPriorite());
        ps.setString(6, r.getReponseAdmin());
        ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(8, r.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM reclamation WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Reclamation> afficher() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT r.*, CONCAT(u.prenom, ' ', u.nom) as nom_utilisateur " +
                "FROM reclamation r " +
                "LEFT JOIN utilisateur u ON r.utilisateur_id = u.id " +
                "ORDER BY r.date_creation DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            reclamations.add(extractReclamationFromResultSet(rs));
        }
        return reclamations;
    }

    public List<Reclamation> getReclamationsByUser(int utilisateurId) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT r.*, CONCAT(u.prenom, ' ', u.nom) as nom_utilisateur " +
                "FROM reclamation r " +
                "LEFT JOIN utilisateur u ON r.utilisateur_id = u.id " +
                "WHERE r.utilisateur_id=? " +
                "ORDER BY r.date_creation DESC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, utilisateurId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            reclamations.add(extractReclamationFromResultSet(rs));
        }
        return reclamations;
    }

    public List<Reclamation> getReclamationsByStatut(String statut) throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT r.*, CONCAT(u.prenom, ' ', u.nom) as nom_utilisateur " +
                "FROM reclamation r " +
                "LEFT JOIN utilisateur u ON r.utilisateur_id = u.id " +
                "WHERE r.statut=? " +
                "ORDER BY r.date_creation DESC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, statut);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            reclamations.add(extractReclamationFromResultSet(rs));
        }
        return reclamations;
    }

    public void changerStatut(int id, String nouveauStatut) throws SQLException {
        String sql = "UPDATE reclamation SET statut=?, date_modification=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, nouveauStatut);
        ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(3, id);
        ps.executeUpdate();
    }

    public void repondre(int id, String reponse, String statut) throws SQLException {
        String sql = "UPDATE reclamation SET reponse_admin=?, statut=?, date_modification=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, reponse);
        ps.setString(2, statut);
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(4, id);
        ps.executeUpdate();
    }

    private Reclamation extractReclamationFromResultSet(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setId(rs.getInt("id"));
        r.setUtilisateurId(rs.getInt("utilisateur_id"));
        r.setNomUtilisateur(rs.getString("nom_utilisateur"));
        r.setSujet(rs.getString("sujet"));
        r.setDescription(rs.getString("description"));
        r.setCategorie(rs.getString("categorie"));
        r.setStatut(rs.getString("statut"));
        r.setPriorite(rs.getString("priorite"));
        r.setReponseAdmin(rs.getString("reponse_admin"));

        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            r.setDateCreation(dateCreation.toLocalDateTime());
        }

        Timestamp dateModification = rs.getTimestamp("date_modification");
        if (dateModification != null) {
            r.setDateModification(dateModification.toLocalDateTime());
        }

        return r;
    }
}