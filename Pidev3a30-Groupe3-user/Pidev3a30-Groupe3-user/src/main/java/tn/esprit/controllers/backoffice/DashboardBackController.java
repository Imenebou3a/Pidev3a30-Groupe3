package tn.esprit.controllers.backoffice;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import tn.esprit.services.*;
import tn.esprit.entities.*;

import java.sql.SQLException;
import java.util.List;

public class DashboardBackController {

    @FXML private Label lblTotalProduits;
    @FXML private Label lblTotalKits;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalReclamations;
    @FXML private Label lblProduitsStockBas;
    @FXML private Label lblKitsStockBas;
    @FXML private Label lblProduitsStockBas2;
    @FXML private Label lblKitsStockBas2;
    @FXML private Label lblReclamationsEnAttente;
    @FXML private Label lblUsersActifs;
    
    // Labels from existing Dashboard.fxml
    @FXML private Label totalUsersLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalHotesLabel;
    @FXML private Label totalPendingLabel;
    @FXML private Label totalReclamationsLabel;
    @FXML private Label reclamationsEnAttenteLabel;
    @FXML private Label reclamationsEnCoursLabel;
    @FXML private Label reclamationsResoluesLabel;
    
    @FXML private PieChart chartReclamations;
    @FXML private PieChart chartUsers;

    private ServiceReclamation serviceReclamation = new ServiceReclamation();
    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private ProduitLocalService produitService = new ProduitLocalService();
    private KitHobbiesService kitService = new KitHobbiesService();

    @FXML
    public void initialize() {
        chargerStatistiques();
        chargerGraphiques();
    }

    private void chargerStatistiques() {
        try {
            // Statistiques principales
            List<ProduitLocal> produits = produitService.afficher();
            List<KitHobbies> kits = kitService.afficher();
            List<Utilisateur> users = serviceUtilisateur.afficher();
            List<Reclamation> reclamations = serviceReclamation.getAll();

            // Update labels if they exist
            if (lblTotalProduits != null) lblTotalProduits.setText(String.valueOf(produits.size()));
            if (lblTotalKits != null) lblTotalKits.setText(String.valueOf(kits.size()));
            if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(users.size()));
            if (lblTotalReclamations != null) lblTotalReclamations.setText(String.valueOf(reclamations.size()));

            // Update existing Dashboard labels
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(users.size()));
            if (totalReclamationsLabel != null) totalReclamationsLabel.setText(String.valueOf(reclamations.size()));

            // User statistics by role
            long admins = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            long hotes = users.stream().filter(u -> "HOTE".equals(u.getRole())).count();
            long usersActifs = users.stream().filter(u -> "ACTIF".equals(u.getStatut())).count();
            long pending = users.stream().filter(u -> "EN_ATTENTE".equals(u.getStatut())).count();

            if (totalAdminsLabel != null) totalAdminsLabel.setText(String.valueOf(admins));
            if (totalHotesLabel != null) totalHotesLabel.setText(String.valueOf(hotes));
            if (totalPendingLabel != null) totalPendingLabel.setText(String.valueOf(pending));
            if (lblUsersActifs != null) lblUsersActifs.setText(String.valueOf(usersActifs));

            // Reclamation statistics
            int enAttente = serviceReclamation.countByStatut("EN_ATTENTE");
            int enCours = serviceReclamation.countByStatut("EN_COURS");
            int resolues = serviceReclamation.countByStatut("RESOLUE");

            if (reclamationsEnAttenteLabel != null) reclamationsEnAttenteLabel.setText(String.valueOf(enAttente));
            if (reclamationsEnCoursLabel != null) reclamationsEnCoursLabel.setText(String.valueOf(enCours));
            if (reclamationsResoluesLabel != null) reclamationsResoluesLabel.setText(String.valueOf(resolues));
            if (lblReclamationsEnAttente != null) lblReclamationsEnAttente.setText(String.valueOf(enAttente));

            // Stock alerts
            long produitsStockBas = produits.stream().filter(p -> p.getStock() < 10).count();
            long kitsStockBas = kits.stream().filter(k -> k.getStock() < 5).count();

            if (lblProduitsStockBas != null) lblProduitsStockBas.setText(String.valueOf(produitsStockBas));
            if (lblKitsStockBas != null) lblKitsStockBas.setText(String.valueOf(kitsStockBas));
            if (lblProduitsStockBas2 != null) lblProduitsStockBas2.setText(String.valueOf(produitsStockBas));
            if (lblKitsStockBas2 != null) lblKitsStockBas2.setText(String.valueOf(kitsStockBas));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chargerGraphiques() {
        try {
            // Graphique réclamations par statut
            if (chartReclamations != null) {
                int enAttente = serviceReclamation.countByStatut("EN_ATTENTE");
                int enCours = serviceReclamation.countByStatut("EN_COURS");
                int resolues = serviceReclamation.countByStatut("RESOLUE");
                int rejetees = serviceReclamation.countByStatut("REJETEE");

                chartReclamations.setData(FXCollections.observableArrayList(
                    new PieChart.Data("En attente (" + enAttente + ")", enAttente),
                    new PieChart.Data("En cours (" + enCours + ")", enCours),
                    new PieChart.Data("Résolues (" + resolues + ")", resolues),
                    new PieChart.Data("Rejetées (" + rejetees + ")", rejetees)
                ));
            }

            // Graphique utilisateurs par rôle
            if (chartUsers != null) {
                List<Utilisateur> users = serviceUtilisateur.afficher();
                long admins = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
                long hotes = users.stream().filter(u -> "HOTE".equals(u.getRole())).count();
                long usersCount = users.stream().filter(u -> "USER".equals(u.getRole())).count();

                chartUsers.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Admins (" + admins + ")", admins),
                    new PieChart.Data("Hôtes (" + hotes + ")", hotes),
                    new PieChart.Data("Users (" + usersCount + ")", usersCount)
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
