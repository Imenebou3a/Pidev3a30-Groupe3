package tn.esprit.services;

import tn.esprit.entities.Utilisateur;
import tn.esprit.utils.MyDataBase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceUtilisateur implements IService<Utilisateur> {

    private Connection connection;

    public ServiceUtilisateur() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    // ===== INSCRIPTION =====
    public boolean inscrire(Utilisateur u) throws SQLException {
        if (emailExiste(u.getEmail())) {
            return false; // Email déjà utilisé
        }

        String sql = "INSERT INTO utilisateur (nom, prenom, email, motDePasse, telephone, role, statut, dateInscription) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setString(3, u.getEmail());
        ps.setString(4, hashPassword(u.getMotDePasse()));
        ps.setString(5, u.getTelephone());
        ps.setString(6, u.getRole());
        ps.setString(7, u.getStatut());
        ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

        int rowsAffected = ps.executeUpdate();

        if (rowsAffected > 0) {
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                u.setId(rs.getInt(1));
            }
            return true;
        }
        return false;
    }

    // ===== CONNEXION =====
    public Utilisateur login(String email, String motDePasse) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email=? AND motDePasse=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, hashPassword(motDePasse));

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Utilisateur u = extractUtilisateurFromResultSet(rs);

            // Vérifier si le compte est actif
            if (!u.isActif()) {
                return null; // Compte désactivé
            }

            // Mettre à jour le dernier login
            updateDernierLogin(u.getId());

            return u;
        }
        return null;
    }

    // ===== CRUD OPERATIONS =====
    @Override
    public void ajouter(Utilisateur u) throws SQLException {
        inscrire(u);
    }

    @Override
    public void modifier(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, telephone=?, adresse=?, bio=?, role=?, statut=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getTelephone());
        ps.setString(5, u.getAdresse());
        ps.setString(6, u.getBio());
        ps.setString(7, u.getRole());
        ps.setString(8, u.getStatut());
        ps.setInt(9, u.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Utilisateur> afficher() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY dateInscription DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            utilisateurs.add(extractUtilisateurFromResultSet(rs));
        }
        return utilisateurs;
    }

    // ===== MÉTHODES SPÉCIFIQUES =====
    public Utilisateur getById(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return extractUtilisateurFromResultSet(rs);
        }
        return null;
    }

    public void activerCompte(int id) throws SQLException {
        changerStatut(id, "ACTIF");
    }

    public void desactiverCompte(int id) throws SQLException {
        changerStatut(id, "DESACTIVE");
    }

    public void changerRole(int id, String nouveauRole) throws SQLException {
        String sql = "UPDATE utilisateur SET role=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, nouveauRole);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    public void changerMotDePasse(int id, String nouveauMotDePasse) throws SQLException {
        String sql = "UPDATE utilisateur SET motDePasse=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, hashPassword(nouveauMotDePasse));
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    public List<Utilisateur> rechercherParNom(String nom) throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE nom LIKE ? OR prenom LIKE ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "%" + nom + "%");
        ps.setString(2, "%" + nom + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            utilisateurs.add(extractUtilisateurFromResultSet(rs));
        }
        return utilisateurs;
    }

    public List<Utilisateur> getUtilisateursParRole(String role) throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, role);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            utilisateurs.add(extractUtilisateurFromResultSet(rs));
        }
        return utilisateurs;
    }

    // ===== MÉTHODES UTILITAIRES =====
    private boolean emailExiste(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    private void changerStatut(int id, String statut) throws SQLException {
        String sql = "UPDATE utilisateur SET statut=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, statut);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    private void updateDernierLogin(int id) throws SQLException {
        String sql = "UPDATE utilisateur SET dernierLogin=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    private Utilisateur extractUtilisateurFromResultSet(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setTelephone(rs.getString("telephone"));
        u.setAdresse(rs.getString("adresse"));
        u.setRole(rs.getString("role"));
        u.setStatut(rs.getString("statut"));
        u.setBio(rs.getString("bio"));
        u.setPhoto(rs.getString("photo"));

        Timestamp dateInscription = rs.getTimestamp("dateInscription");
        if (dateInscription != null) {
            u.setDateInscription(dateInscription.toLocalDateTime());
        }

        Timestamp dernierLogin = rs.getTimestamp("dernierLogin");
        if (dernierLogin != null) {
            u.setDernierLogin(dernierLogin.toLocalDateTime());
        }

        return u;
    }

    // Hashage du mot de passe avec SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}