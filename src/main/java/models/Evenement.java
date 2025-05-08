package models;

import java.time.LocalDate;

public class Evenement {
    private int id;
    private Integer clubId; // Nullable
    private String titre;
    private String description;
    private double prix;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double x;
    private double y;

    // Constructeur
    public Evenement(int id, Integer clubId, String titre, String description, double prix,
                     LocalDate dateDebut, LocalDate dateFin, double x, double y) {
        this.id = id;
        this.clubId = clubId;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.x = x;
        this.y = y;
    }

    public Evenement(String titre, String description, double prix, LocalDate dateDebut, LocalDate dateFin) {
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Evenement(int id, String titre, String description, double prix, LocalDate dateDebut, LocalDate dateFin) {
        this.id=id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
