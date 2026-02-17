package tn.esprit.services;

import tn.esprit.entities.Reclamation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceReclamation {

    private Connection connection;

    public ServiceReclamation() {
        connection = MyDataBase.getConnection();
    }

    private Reclamation map(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setId(rs.getInt("id"));
        r.setSujet(rs.getString("sujet"));
        r.setDescription(rs.getString("description"));
        r.setStatut(rs.getString("statut"));
        r.setPriorite(rs.getString("priorite"));
        r.setIdUtilisateur(rs.getInt("id_utilisateur"));
        r.setReponseAdmin(rs.getString("reponse_admin"));
        try { r.setCategorie(rs.getString("categorie")); } catch (SQLException ignored) {}
        Timestamp ts = rs.getTimestamp("date_creation");
        if (ts != null) r.setDateCreation(ts.toLocalDateTime());
        Timestamp tr = rs.getTimestamp("date_resolution");
        if (tr != null) r.setDateResolution(tr.toLocalDateTime());
        try { r.setNomUtilisateur(rs.getString("nom_utilisateur")); } catch (SQLException ignored) {}
        return r;
    }

    public void ajouter(Reclamation r) throws SQLException {
        String sql = "INSERT INTO reclamation (sujet, description, statut, priorite, categorie, id_utilisateur) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, r.getSujet());
        ps.setString(2, r.getDescription());
        ps.setString(3, r.getStatut() != null ? r.getStatut() : "EN_ATTENTE");
        ps.setString(4, r.getPriorite() != null ? r.getPriorite() : "MOYENNE");
        ps.setString(5, r.getCategorie() != null ? r.getCategorie() : "AUTRE");
        ps.setInt(6, r.getIdUtilisateur());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM reclamation WHERE id = ?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    /** Used by DashboardController */
    public List<Reclamation> afficher() throws SQLException {
        return getAll();
    }

    /** Used by backoffice ReclamationController */
    public List<Reclamation> getAll() throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT r.*, CONCAT(u.prenom, ' ', u.nom) as nom_utilisateur " +
                "FROM reclamation r LEFT JOIN utilisateur u ON r.id_utilisateur = u.id " +
                "ORDER BY r.date_creation DESC";
        ResultSet rs = connection.createStatement().executeQuery(sql);
        while (rs.next()) list.add(map(rs));
        return list;
    }

    /** Used by MesReclamationsController */
    public List<Reclamation> getReclamationsByUser(int idUser) throws SQLException {
        return getByUtilisateur(idUser);
    }

    /** Alias for compatibility */
    public List<Reclamation> getByUtilisateur(int idUser) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM reclamation WHERE id_utilisateur = ? ORDER BY date_creation DESC");
        ps.setInt(1, idUser);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    /** Used by DashboardController */
    public void repondre(int id, String reponse, String statut) throws SQLException {
        updateStatut(id, statut, reponse);
    }

    /** Used by backoffice ReclamationController */
    public void updateStatut(int id, String statut, String reponse) throws SQLException {
        String sql = "UPDATE reclamation SET statut = ?, reponse_admin = ?, date_resolution = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, statut);
        ps.setString(2, reponse);
        ps.setTimestamp(3, "RESOLUE".equals(statut) ? Timestamp.valueOf(LocalDateTime.now()) : null);
        ps.setInt(4, id);
        ps.executeUpdate();
    }

    public int countByStatut(String statut) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM reclamation WHERE statut = ?");
        ps.setString(1, statut);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }
}