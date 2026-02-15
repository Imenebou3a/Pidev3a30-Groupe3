package tn.esprit.entities;

import java.time.LocalDateTime;

public class Reclamation {

    private int id;
    private int utilisateurId;
    private String nomUtilisateur;
    private String sujet;
    private String description;
    private String categorie;
    private String statut;
    private String priorite;
    private String reponseAdmin;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    public Reclamation() {
        this.dateCreation = LocalDateTime.now();
        this.statut = "EN_ATTENTE";
        this.priorite = "MOYENNE";
        this.categorie = "AUTRE";
    }

    public Reclamation(int utilisateurId, String sujet, String description, String categorie) {
        this();
        this.utilisateurId = utilisateurId;
        this.sujet = sujet;
        this.description = description;
        this.categorie = categorie;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getReponseAdmin() { return reponseAdmin; }
    public void setReponseAdmin(String reponseAdmin) { this.reponseAdmin = reponseAdmin; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", sujet='" + sujet + '\'' +
                ", statut='" + statut + '\'' +
                ", categorie='" + categorie + '\'' +
                '}';
    }
}