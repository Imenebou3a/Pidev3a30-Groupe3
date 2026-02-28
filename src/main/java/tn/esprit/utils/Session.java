package tn.esprit.utils;

import tn.esprit.entities.Utilisateur;

public class Session {
    private static Session instance;
    private Utilisateur utilisateurConnecte;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public boolean isConnecte() {
        return utilisateurConnecte != null;
    }

    public void deconnecter() {
        utilisateurConnecte = null;
    }

    public boolean isAdmin() {
        return utilisateurConnecte != null && "ADMIN".equals(utilisateurConnecte.getRole());
    }

    public boolean isHote() {
        return utilisateurConnecte != null && "HOTE".equals(utilisateurConnecte.getRole());
    }
}