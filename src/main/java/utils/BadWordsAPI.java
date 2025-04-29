package utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BadWordsAPI {

    private static final String USER_ID = "AmineSassy";
    private static final String API_KEY = "SETr8jqQ245lfhB8vfHwI683vZ1jdYlZsnuoHEH8zV9YKvGK";
    private static final String API_URL = "https://neutrinoapi.net/bad-word-filter";

    public static String filterBadWords(String text) {
        try {
            // Préparation des données POST
            String urlParameters = "content=" + text +
                    "&censor-character=*";

            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            // Création de la connexion
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configuration de la requête
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-ID", USER_ID);
            conn.setRequestProperty("API-Key", API_KEY);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            // Envoi des données
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);
            }

            // Lecture de la réponse
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }

                // 🔥 Extraction manuelle de "censored-content" sans utiliser de bibliothèque JSON
                String json = response.toString();
                String start = "\"censored-content\":\"";
                int startIndex = json.indexOf(start);
                if (startIndex != -1) {
                    startIndex += start.length();
                    int endIndex = json.indexOf("\"", startIndex);
                    if (endIndex != -1) {
                        return json.substring(startIndex, endIndex);
                    }
                }

                return "Erreur : champ 'censored-content' introuvable !";

            } else {
                return "Erreur API : " + responseCode + " - " + conn.getResponseMessage();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }
    }
}
