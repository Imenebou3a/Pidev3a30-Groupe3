package tn.esprit.entities;

import java.time.LocalDateTime;

public class Utilisateur {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private String adresse;
    private String role; // USER, HOTE, ADMIN
    private String statut; // ACTIF, DESACTIVE, EN_ATTENTE
    private String photo;
    private String bio;
    private LocalDateTime dateInscription;
    private LocalDateTime dernierLogin;

    // Constructeurs
    public Utilisateur() {
        this.dateInscription = LocalDateTime.now();
        this.statut = "EN_ATTENTE";
        this.role = "USER";
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse, String telephone) {
        this();
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public LocalDateTime getDernierLogin() { return dernierLogin; }
    public void setDernierLogin(LocalDateTime dernierLogin) { this.dernierLogin = dernierLogin; }

    // Méthodes métier
    public boolean isActif() {
        return "ACTIF".equals(this.statut);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }

    public boolean isHote() {
        return "HOTE".equals(this.role);
    }

    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", role='" + role + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}