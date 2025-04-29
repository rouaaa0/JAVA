package models;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private String contenu;
    private String nom;
    private String lastname;
    private String email;
    private String role;
    private LocalDateTime date;

    // Champs pour les pièces jointes
    private String attachmentPath;    // Chemin du fichier sur le disque
    private String attachmentName;    // Nom original du fichier
    private String attachmentType;    // Type (IMAGE, FILE, AUDIO)
    private long audioDuration;       // Durée en secondes pour les messages audio

    public enum AttachmentType {
        NONE,
        IMAGE,
        FILE,
        AUDIO       // Type pour les messages vocaux
    }

    // Constructeur sans pièce jointe
    public Message(int id, String contenu, String nom, String lastname, String email, String role, LocalDateTime date) {
        this.id = id;
        this.contenu = contenu;
        this.nom = nom;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
        this.date = date;
        this.attachmentPath = null;
        this.attachmentName = null;
        this.attachmentType = AttachmentType.NONE.toString();
        this.audioDuration = 0;
    }

    // Constructeur avec pièce jointe
    public Message(int id, String contenu, String nom, String lastname, String email, String role, LocalDateTime date,
                   String attachmentPath, String attachmentName, String attachmentType) {
        this.id = id;
        this.contenu = contenu;
        this.nom = nom;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
        this.date = date;
        this.attachmentPath = attachmentPath;
        this.attachmentName = attachmentName;
        this.attachmentType = attachmentType;
        this.audioDuration = 0;
    }

    // Constructeur avec pièce jointe audio
    public Message(int id, String contenu, String nom, String lastname, String email, String role, LocalDateTime date,
                   String attachmentPath, String attachmentName, String attachmentType, long audioDuration) {
        this.id = id;
        this.contenu = contenu;
        this.nom = nom;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
        this.date = date;
        this.attachmentPath = attachmentPath;
        this.attachmentName = attachmentName;
        this.attachmentType = attachmentType;
        this.audioDuration = audioDuration;
    }

    // Getters
    public int getId() { return id; }
    public String getContenu() { return contenu; }
    public String getNom() { return nom; }
    public String getLastname() { return lastname; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public LocalDateTime getDate() { return date; }

    // Getters et setters pour les pièces jointes
    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public long getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(long audioDuration) {
        this.audioDuration = audioDuration;
    }

    public boolean hasAttachment() {
        return attachmentPath != null && !attachmentPath.isEmpty();
    }

    public boolean isImageAttachment() {
        return hasAttachment() && AttachmentType.IMAGE.toString().equals(attachmentType);
    }

    public boolean isFileAttachment() {
        return hasAttachment() && AttachmentType.FILE.toString().equals(attachmentType);
    }

    public boolean isAudioAttachment() {
        return hasAttachment() && AttachmentType.AUDIO.toString().equals(attachmentType);
    }

    // Formater la durée de l'audio en format MM:SS
    public String getFormattedAudioDuration() {
        if (audioDuration <= 0) return "00:00";

        long minutes = audioDuration / 60;
        long seconds = audioDuration % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }
}