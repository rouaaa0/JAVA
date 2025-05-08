package service;

import models.Message;
import models.User;
import utils.MyConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatService {
    private final Connection connection;

    // Dossier pour stocker les pièces jointes
    private static final String ATTACHMENTS_DIR = "attachments";
    private static final String AUDIO_DIR = "attachments/audio"; // Sous-dossier pour les fichiers audio

    public ChatService() {
        this.connection = MyConnection.getInstance().getCnx();

        // S'assurer que les dossiers des pièces jointes existent
        File attachmentsDir = new File(ATTACHMENTS_DIR);
        if (!attachmentsDir.exists()) {
            attachmentsDir.mkdirs();
        }

        File audioDir = new File(AUDIO_DIR);
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
    }

    public void sendMessage(User user, String content) throws SQLException {
        String sql = "INSERT INTO chat_messages (contenu, user_id, date_envoi) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, content);
            ps.setInt(2, user.getId());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    // Méthode pour envoyer un message avec pièce jointe
    public void sendMessageWithAttachment(User user, String content, File attachmentFile,
                                          String attachmentType) throws SQLException, IOException {
        // Générer un nom unique pour le fichier
        String originalFileName = attachmentFile.getName();
        String fileExtension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            fileExtension = originalFileName.substring(i);
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        String destinationPath = ATTACHMENTS_DIR + File.separator + uniqueFileName;

        // Pour les fichiers audio, utiliser le sous-dossier audio
        if (Message.AttachmentType.AUDIO.toString().equals(attachmentType)) {
            destinationPath = AUDIO_DIR + File.separator + uniqueFileName;
        }

        // Copier le fichier vers le dossier de stockage
        Path sourcePath = attachmentFile.toPath();
        Path targetPath = Paths.get(destinationPath);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Insérer le message avec la référence de la pièce jointe dans la base de données
        String sql = "INSERT INTO chat_messages (contenu, user_id, date_envoi, attachment_path, attachment_name, attachment_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, content);
            ps.setInt(2, user.getId());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, destinationPath);
            ps.setString(5, originalFileName);
            ps.setString(6, attachmentType);
            ps.executeUpdate();
        }
    }

    // Méthode pour envoyer un message vocal avec durée
    public void sendVoiceMessage(User user, String content, File audioFile, long audioDuration)
            throws SQLException, IOException {
        // Générer un nom unique pour le fichier
        String originalFileName = audioFile.getName();
        String fileExtension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            fileExtension = originalFileName.substring(i);
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        String destinationPath = AUDIO_DIR + File.separator + uniqueFileName;

        // Copier le fichier vers le dossier de stockage
        Path sourcePath = audioFile.toPath();
        Path targetPath = Paths.get(destinationPath);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Insérer le message avec la référence de la pièce jointe dans la base de données
        String sql = "INSERT INTO chat_messages (contenu, user_id, date_envoi, attachment_path, attachment_name, attachment_type, audio_duration) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, content);
            ps.setInt(2, user.getId());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, destinationPath);
            ps.setString(5, originalFileName);
            ps.setString(6, Message.AttachmentType.AUDIO.toString());
            ps.setLong(7, audioDuration);
            ps.executeUpdate();
        }
    }

    public void updateMessage(int messageId, String newContent) throws SQLException {
        String sql = "UPDATE chat_messages SET contenu = ?, date_modification = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newContent);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, messageId);
            ps.executeUpdate();
        }
    }

    public void deleteMessage(int messageId) throws SQLException {
        // Récupérer le chemin du fichier attaché s'il existe
        String attachmentPath = null;

        String selectQuery = "SELECT attachment_path FROM chat_messages WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    attachmentPath = rs.getString("attachment_path");
                }
            }
        }

        // Supprimer le message de la base de données
        String deleteQuery = "DELETE FROM chat_messages WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
            ps.setInt(1, messageId);
            ps.executeUpdate();
        }

        // Supprimer le fichier attaché s'il existe
        if (attachmentPath != null && !attachmentPath.isEmpty()) {
            try {
                Files.deleteIfExists(Paths.get(attachmentPath));
            } catch (IOException e) {
                e.printStackTrace();
                // Ne pas bloquer la suppression du message si la suppression du fichier échoue
            }
        }
    }

    public List<Message> getAllMessages() throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT cm.id, cm.contenu, cm.date_envoi, u.id as user_id, u.name, u.lastname, u.email, u.role, " +
                "cm.attachment_path, cm.attachment_name, cm.attachment_type, cm.audio_duration " +
                "FROM chat_messages cm JOIN user u ON cm.user_id = u.id " +
                "ORDER BY cm.date_envoi ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Message message;

                // Si c'est un message audio, utiliser le constructeur avec audioDuration
                if (Message.AttachmentType.AUDIO.toString().equals(rs.getString("attachment_type"))) {
                    message = new Message(
                            rs.getInt("id"),
                            rs.getString("contenu"),
                            rs.getString("name"),
                            rs.getString("lastname"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getTimestamp("date_envoi").toLocalDateTime(),
                            rs.getString("attachment_path"),
                            rs.getString("attachment_name"),
                            rs.getString("attachment_type"),
                            rs.getLong("audio_duration")
                    );
                } else {
                    message = new Message(
                            rs.getInt("id"),
                            rs.getString("contenu"),
                            rs.getString("name"),
                            rs.getString("lastname"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getTimestamp("date_envoi").toLocalDateTime()
                    );

                    // Ajouter les informations de pièce jointe si elles existent
                    String attachmentPath = rs.getString("attachment_path");
                    if (attachmentPath != null) {
                        message.setAttachmentPath(attachmentPath);
                        message.setAttachmentName(rs.getString("attachment_name"));
                        message.setAttachmentType(rs.getString("attachment_type"));
                    }
                }

                messages.add(message);
            }
        }
        return messages;
    }

    // Méthode pour obtenir un message par son ID
    public Message getMessageById(int messageId) throws SQLException {
        String sql = "SELECT cm.id, cm.contenu, cm.date_envoi, u.id as user_id, u.name, u.lastname, u.email, u.role, " +
                "cm.attachment_path, cm.attachment_name, cm.attachment_type, cm.audio_duration " +
                "FROM chat_messages cm JOIN user u ON cm.user_id = u.id " +
                "WHERE cm.id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Message message;

                    // Si c'est un message audio, utiliser le constructeur avec audioDuration
                    if (Message.AttachmentType.AUDIO.toString().equals(rs.getString("attachment_type"))) {
                        message = new Message(
                                rs.getInt("id"),
                                rs.getString("contenu"),
                                rs.getString("name"),
                                rs.getString("lastname"),
                                rs.getString("email"),
                                rs.getString("role"),
                                rs.getTimestamp("date_envoi").toLocalDateTime(),
                                rs.getString("attachment_path"),
                                rs.getString("attachment_name"),
                                rs.getString("attachment_type"),
                                rs.getLong("audio_duration")
                        );
                    } else {
                        message = new Message(
                                rs.getInt("id"),
                                rs.getString("contenu"),
                                rs.getString("name"),
                                rs.getString("lastname"),
                                rs.getString("email"),
                                rs.getString("role"),
                                rs.getTimestamp("date_envoi").toLocalDateTime()
                        );

                        // Ajouter les informations de pièce jointe si elles existent
                        String attachmentPath = rs.getString("attachment_path");
                        if (attachmentPath != null) {
                            message.setAttachmentPath(attachmentPath);
                            message.setAttachmentName(rs.getString("attachment_name"));
                            message.setAttachmentType(rs.getString("attachment_type"));
                        }
                    }

                    return message;
                }
            }
        }

        return null; // Message non trouvé
    }

    // Méthode pour copier un fichier attaché vers un emplacement spécifié par l'utilisateur
    public void saveAttachmentToLocation(String sourcePath, String destinationPath) throws IOException {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            throw new IOException("Le fichier source n'existe pas: " + sourcePath);
        }

        Files.copy(sourceFile.toPath(), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
    }
}