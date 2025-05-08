package service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    // Configurez ces valeurs selon vos besoins
    private static final String FROM_EMAIL = "chebbimaram0@gmail.com"; // Remplacez par votre email Gmail
    private static final String APP_PASSWORD = "hlsbbpyoruhcweaf"; // Remplacez par votre mot de passe d'application
    private static final String EDUNOVA_NAME = "Edunova";
    private static final String EDUNOVA_COLOR = "#3498db"; // Couleur principale pour le branding

    /**
     * Envoie un nouveau mot de passe à l'utilisateur
     * @param toEmail Email du destinataire
     * @param newPassword Le nouveau mot de passe généré
     */
    public static void sendNewPassword(String toEmail, String newPassword) {
        // Configuration des propriétés pour la connexion SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Création de la session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            // Création du message
            Message message = new MimeMessage(session);
            // Utilisez un nom personnalisé pour l'expéditeur (Edunova)
            message.setFrom(new InternetAddress(FROM_EMAIL, EDUNOVA_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(EDUNOVA_NAME + " - Réinitialisation de votre mot de passe");

            // Contenu HTML pour un email plus professionnel
            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                            "    <div style='background-color: " + EDUNOVA_COLOR + "; padding: 20px; text-align: center;'>" +
                            "        <h1 style='color: white; margin: 0;'>" + EDUNOVA_NAME + "</h1>" +
                            "    </div>" +
                            "    <div style='padding: 20px; background-color: #f9f9f9; border: 1px solid #ddd;'>" +
                            "        <h2>Réinitialisation de mot de passe</h2>" +
                            "        <p>Bonjour,</p>" +
                            "        <p>Vous avez demandé une réinitialisation de votre mot de passe sur " + EDUNOVA_NAME + ".</p>" +
                            "        <p>Voici votre nouveau mot de passe temporaire:</p>" +
                            "        <div style='background-color: #f0f0f0; padding: 15px; border-left: 4px solid " + EDUNOVA_COLOR + "; margin: 20px 0; font-family: monospace; font-size: 16px;'>" +
                            "            " + newPassword +
                            "        </div>" +
                            "        <p><strong>Important:</strong> Veuillez vous connecter avec ce mot de passe et le changer immédiatement pour des raisons de sécurité.</p>" +
                            "        <p>Si vous n'avez pas demandé cette réinitialisation, veuillez nous contacter immédiatement.</p>" +
                            "    </div>" +
                            "    <div style='padding: 20px; text-align: center; font-size: 12px; color: #666;'>" +
                            "        <p>Cordialement,<br>L'équipe " + EDUNOVA_NAME + "</p>" +
                            "        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
                            "    </div>" +
                            "</div>";

            // Option 1: Email en HTML avec version texte comme fallback
            MimeMultipart multipart = new MimeMultipart("alternative");

            // Version texte simple (fallback)
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Bonjour,\n\n" +
                    "Vous avez demandé une réinitialisation de votre mot de passe sur " + EDUNOVA_NAME + ".\n" +
                    "Voici votre nouveau mot de passe temporaire : " + newPassword + "\n\n" +
                    "Veuillez vous connecter avec ce mot de passe et le changer immédiatement pour des raisons de sécurité.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe " + EDUNOVA_NAME);

            // Version HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

            // Envoi du message
            Transport.send(message);
            System.out.println("Email de réinitialisation envoyé avec succès à " + toEmail + " de la part d'" + EDUNOVA_NAME);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Génère un mot de passe aléatoire de la longueur spécifiée
     * @param length Longueur du mot de passe
     * @return Le mot de passe généré
     */
    public static String generateRandomPassword(int length) {
        String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()_-+=<>?";

        String allChars = upperChars + lowerChars + digits + specialChars;
        Random random = new Random();

        StringBuilder password = new StringBuilder();

        // Assure qu'au moins un caractère de chaque type est inclus
        password.append(upperChars.charAt(random.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Ajoute des caractères aléatoires supplémentaires jusqu'à atteindre la longueur demandée
        for (int i = 4; i < length; i++) {
            int index = random.nextInt(allChars.length());
            password.append(allChars.charAt(index));
        }

        // Mélange les caractères pour éviter un motif prévisible
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }
}