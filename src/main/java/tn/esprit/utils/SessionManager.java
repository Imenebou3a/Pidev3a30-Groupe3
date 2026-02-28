package tn.esprit.utils;

import tn.esprit.entities.Utilisateur;

/**
 * Gestionnaire de session utilisateur (Singleton)
 * Stocke l'utilisateur connecté pour toute l'application
 */
public class SessionManager {
    
    private static SessionManager instance;
    private Utilisateur utilisateurConnecte;
    
    // Constructeur privé pour Singleton
    private SessionManager() {
        this.utilisateurConnecte = null;
    }
    
    /**
     * Récupère l'instance unique du SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Récupère l'utilisateur connecté
     */
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }
    
    /**
     * Définit l'utilisateur connecté
     */
    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        if (utilisateur != null) {
            System.out.println("✓ Session créée pour: " + utilisateur.getNom());
        }
    }
    
    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }
    
    /**
     * Récupère l'ID de l'utilisateur connecté
     */
    public int getIdUtilisateur() {
        if (utilisateurConnecte != null) {
            return utilisateurConnecte.getIdUtilisateur();
        }
        return -1;
    }
    
    /**
     * Déconnecte l'utilisateur
     */
    public void deconnecter() {
        if (utilisateurConnecte != null) {
            System.out.println("✓ Déconnexion de: " + utilisateurConnecte.getNom());
        }
        utilisateurConnecte = null;
    }
    
    /**
     * Réinitialise la session (pour les tests)
     */
    public void reset() {
        utilisateurConnecte = null;
        instance = null;
    }
}
