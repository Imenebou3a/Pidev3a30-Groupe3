package tn.esprit.entities;

import java.time.LocalDateTime;

public class Reclamation {

    private int id;
    private String sujet;
    private String description;
    private String statut;
    private String priorite;
    private String categorie;
    private LocalDateTime dateCreation;
    private LocalDateTime dateResolution;
    private int idUtilisateur;
    private String nomUtilisateur;
    private String reponseAdmin;

    public Reclamation() {}

    // Constructor used by AddReclamationController:
    // new Reclamation(currentUser.getId(), sujet, description, categorie)
    public Reclamation(int idUtilisateur, String sujet, String description, String categorie) {
        this.idUtilisateur = idUtilisateur;
        this.sujet = sujet;
        this.description = description;
        this.categorie = categorie;
        this.statut = "EN_ATTENTE";
        this.priorite = "MOYENNE";
        this.dateCreation = LocalDateTime.now();
    }

    // Full constructor
    public Reclamation(String sujet, String description, String statut,
                       String priorite, String categorie, int idUtilisateur) {
        this.sujet = sujet;
        this.description = description;
        this.statut = statut;
        this.priorite = priorite;
        this.categorie = categorie;
        this.idUtilisateur = idUtilisateur;
        this.dateCreation = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateResolution() { return dateResolution; }
    public void setDateResolution(LocalDateTime dateResolution) { this.dateResolution = dateResolution; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getReponseAdmin() { return reponseAdmin; }
    public void setReponseAdmin(String reponseAdmin) { this.reponseAdmin = reponseAdmin; }
}