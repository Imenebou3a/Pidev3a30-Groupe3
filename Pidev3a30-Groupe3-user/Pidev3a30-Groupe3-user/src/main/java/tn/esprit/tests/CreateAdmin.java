package tn.esprit.tests;

import tn.esprit.entities.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateAdmin {
    public static void main(String[] args) {
        ServiceUtilisateur su = new ServiceUtilisateur();

        try {
            // Nettoyer la base
            System.out.println("===== NETTOYAGE DE LA BASE =====");
            Connection conn = MyDataBase.getInstance().getMyConnection();
            Statement st = conn.createStatement();
            st.executeUpdate("DELETE FROM utilisateur");
            st.executeUpdate("ALTER TABLE utilisateur AUTO_INCREMENT = 1");
            System.out.println("✅ Base nettoyée !\n");

            // Créer un admin
            System.out.println("===== CRÉATION ADMIN =====");
            Utilisateur admin = new Utilisateur(
                    "Admin",
                    "Système",
                    "admin@lammetna.tn",
                    "admin123",
                    "71234567"
            );
            admin.setRole("ADMIN");

            if (su.inscrire(admin)) {
                su.activerCompte(admin.getId());
                System.out.println("✅ Admin créé et activé !");
                System.out.println("Email: admin@lammetna.tn");
                System.out.println("Mot de passe: admin123");
            }

            // Créer un utilisateur normal
            System.out.println("\n===== CRÉATION USER =====");
            Utilisateur user = new Utilisateur(
                    "Ben Salah",
                    "Ahmed",
                    "ahmed@email.com",
                    "user123456",
                    "98765432"
            );

            if (su.inscrire(user)) {
                su.activerCompte(user.getId());
                System.out.println("✅ User créé et activé !");
                System.out.println("Email: ahmed@email.com");
                System.out.println("Mot de passe: user123456");
            }

            // Créer un hôte
            System.out.println("\n===== CRÉATION HÔTE =====");
            Utilisateur hote = new Utilisateur(
                    "Amari",
                    "Salma",
                    "salma@email.com",
                    "hote123456",
                    "21234567"
            );

            if (su.inscrire(hote)) {
                su.changerRole(hote.getId(), "HOTE");
                su.activerCompte(hote.getId());
                System.out.println("✅ Hôte créé et activé !");
                System.out.println("Email: salma@email.com");
                System.out.println("Mot de passe: hote123456");
            }

            System.out.println("\n===== RÉSUMÉ DES COMPTES =====");
            System.out.println("ADMIN:");
            System.out.println("  Email: admin@lammetna.tn");
            System.out.println("  Mot de passe: admin123");
            System.out.println("\nUSER:");
            System.out.println("  Email: ahmed@email.com");
            System.out.println("  Mot de passe: user123456");
            System.out.println("\nHÔTE:");
            System.out.println("  Email: salma@email.com");
            System.out.println("  Mot de passe: hote123456");

        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}