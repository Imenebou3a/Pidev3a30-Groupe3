package tn.esprit.tests;

import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {

        ServiceUtilisateur su = new ServiceUtilisateur();

        try {
            // ===== NETTOYAGE (optionnel pour les tests) =====
            System.out.println("===== NETTOYAGE DE LA BASE =====");
            Connection conn = MyDataBase.getInstance().getMyConnection();
            Statement st = conn.createStatement();
            st.executeUpdate("DELETE FROM utilisateur");
            st.executeUpdate("ALTER TABLE utilisateur AUTO_INCREMENT = 1");
            System.out.println("✅ Base nettoyée !\n");

            // ===== TEST INSCRIPTION =====
            System.out.println("===== TEST INSCRIPTION =====");
            Utilisateur nouvelUtilisateur = new Utilisateur(
                    "Ben Sghayer",
                    "Ibrahim",
                    "ibrahim@email.com",
                    "password123",
                    "98765432"
            );

            if (su.inscrire(nouvelUtilisateur)) {
                System.out.println("✅ Inscription réussie !");
                System.out.println("ID généré : " + nouvelUtilisateur.getId());
                System.out.println("Statut initial : " + nouvelUtilisateur.getStatut());
            } else {
                System.out.println("❌ Email déjà utilisé !");
            }

            // ===== TEST LOGIN AVANT ACTIVATION (doit échouer) =====
            System.out.println("\n===== TEST LOGIN AVANT ACTIVATION =====");
            Utilisateur tentative1 = su.login("ibrahim@email.com", "password123");

            if (tentative1 == null) {
                System.out.println("❌ Connexion refusée : compte en attente d'activation");
            } else {
                System.out.println("✅ Connexion réussie");
            }

            // ===== TEST ACTIVATION COMPTE =====
            System.out.println("\n===== TEST ACTIVATION =====");
            su.activerCompte(nouvelUtilisateur.getId());
            System.out.println("✅ Compte activé !");

            // ===== TEST LOGIN APRÈS ACTIVATION (doit réussir) =====
            System.out.println("\n===== TEST LOGIN APRÈS ACTIVATION =====");
            Utilisateur userConnecte = su.login("ibrahim@email.com", "password123");

            if (userConnecte != null) {
                System.out.println("✅ Connexion réussie !");
                System.out.println("Bienvenue " + userConnecte.getNomComplet());
                System.out.println("Rôle : " + userConnecte.getRole());
                System.out.println("Statut : " + userConnecte.getStatut());
                System.out.println("Téléphone : " + userConnecte.getTelephone());
            } else {
                System.out.println("❌ Email ou mot de passe incorrect !");
            }

            // ===== TEST MODIFICATION PROFIL =====
            System.out.println("\n===== TEST MODIFICATION PROFIL =====");
            userConnecte.setAdresse("Tunis, Tunisie");
            userConnecte.setBio("Développeur passionné");
            su.modifier(userConnecte);
            System.out.println("✅ Profil mis à jour !");

            // ===== TEST CHANGEMENT DE RÔLE =====
            System.out.println("\n===== TEST CHANGEMENT DE RÔLE =====");
            su.changerRole(userConnecte.getId(), "HOTE");
            System.out.println("✅ Rôle changé en HOTE !");

            // ===== TEST CHANGEMENT DE MOT DE PASSE =====
            System.out.println("\n===== TEST CHANGEMENT DE MOT DE PASSE =====");
            su.changerMotDePasse(userConnecte.getId(), "newPassword456");
            System.out.println("✅ Mot de passe changé !");

            // Vérifier le nouveau mot de passe
            Utilisateur testNewPassword = su.login("ibrahim@email.com", "newPassword456");
            if (testNewPassword != null) {
                System.out.println("✅ Nouveau mot de passe validé !");
            }

            // ===== TEST DÉSACTIVATION =====
            System.out.println("\n===== TEST DÉSACTIVATION =====");
            su.desactiverCompte(userConnecte.getId());
            System.out.println("✅ Compte désactivé !");

            Utilisateur tentative2 = su.login("ibrahim@email.com", "newPassword456");
            if (tentative2 == null) {
                System.out.println("❌ Connexion refusée : compte désactivé");
            }

            // Réactiver pour la suite
            su.activerCompte(userConnecte.getId());
            System.out.println("✅ Compte réactivé !");

            // ===== TEST AFFICHAGE =====
            System.out.println("\n===== LISTE DES UTILISATEURS =====");
            su.afficher().forEach(System.out::println);

            // ===== TEST AJOUT D'AUTRES UTILISATEURS =====
            System.out.println("\n===== AJOUT D'AUTRES UTILISATEURS =====");

            Utilisateur user2 = new Utilisateur("Amri", "Salma", "salma@email.com", "pass123", "21234567");
            su.inscrire(user2);
            su.activerCompte(user2.getId());
            System.out.println("✅ Salma ajoutée et activée");

            Utilisateur user3 = new Utilisateur("Gharbi", "Mohamed", "mohamed@email.com", "pass123", "22345678");
            user3.setRole("ADMIN");
            su.inscrire(user3);
            su.activerCompte(user3.getId());
            System.out.println("✅ Mohamed ajouté (ADMIN) et activé");

            // ===== TEST RECHERCHE =====
            System.out.println("\n===== TEST RECHERCHE PAR NOM =====");
            su.rechercherParNom("Amri").forEach(u ->
                    System.out.println("Trouvé : " + u.getNomComplet())
            );

            // ===== TEST FILTRAGE PAR RÔLE =====
            System.out.println("\n===== LISTE DES HÔTES =====");
            su.getUtilisateursParRole("HOTE").forEach(u ->
                    System.out.println("• " + u.getNomComplet() + " (" + u.getEmail() + ")")
            );

            System.out.println("\n===== LISTE DES ADMINS =====");
            su.getUtilisateursParRole("ADMIN").forEach(u ->
                    System.out.println("• " + u.getNomComplet() + " (" + u.getEmail() + ")")
            );

            System.out.println("\n===== LISTE DES USERS =====");
            su.getUtilisateursParRole("USER").forEach(u ->
                    System.out.println("• " + u.getNomComplet() + " (" + u.getEmail() + ")")
            );

            // ===== TEST RÉCUPÉRATION PAR ID =====
            System.out.println("\n===== TEST RÉCUPÉRATION PAR ID =====");
            Utilisateur userById = su.getById(1);
            if (userById != null) {
                System.out.println("Utilisateur ID 1 : " + userById.getNomComplet());
            }

            // ===== STATISTIQUES FINALES =====
            System.out.println("\n===== STATISTIQUES =====");
            System.out.println("Total utilisateurs : " + su.afficher().size());
            System.out.println("Hôtes : " + su.getUtilisateursParRole("HOTE").size());
            System.out.println("Admins : " + su.getUtilisateursParRole("ADMIN").size());
            System.out.println("Users : " + su.getUtilisateursParRole("USER").size());

        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }
}