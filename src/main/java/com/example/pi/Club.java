package com.example.pi;

import java.time.LocalDate;
import java.util.Date;

 public class Club {
    private int id;
    private String nom;
    private String logo;
    private String type;
    private LocalDate dateCreation;
    private int userId;

     public Club(int id, String nom, String logo, String type, LocalDate dateCreation, int userId) {
         this.id = id;
         this.nom = nom;
         this.logo = logo;
         this.type = type;
         this.dateCreation = dateCreation;
         this.userId = userId;
     }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
