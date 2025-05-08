package utils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

public class ProfanityFilter {
    private static final String API_URL = "https://api.api-ninjas.com/v1/profanityfilter";
    private static final String API_KEY = "iLzwMZbBYUvli4TeEbvLww==R7pTNsYpzfvpH0Ay";
    private static final int MAX_RETRIES = 2;
    private static final int TIMEOUT_SECONDS = 10;

    // List of words to censor in the local fallback method
    private static final List<String> BAD_WORDS = Arrays.asList(
            "damn", "hell", "ass", "fuck", "shit", "bitch", "crap",
            "merde", "putain", "con", "salope", "connard", "enculé", "foutre", "cul"
    );

    /**
     * Filter text for profanity using external API with fallback to local filtering
     */
    public static String filterText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // First try with the API
        String filteredText = tryApiFilter(text, 0);

        // If API filtering failed, use local fallback
        if (filteredText.equals(text)) {
            System.out.println("Using local fallback profanity filter");
            filteredText = filterLocalFallback(text);
        }

        return filteredText;
    }

    /**
     * Try to filter using the API with retries
     */
    private static String tryApiFilter(String text, int retryCount) {
        try {
            System.out.println("Texte original à filtrer: " + text);

            // Encoder correctement le texte pour URL
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            System.out.println("Texte encodé: " + encodedText);

            String fullUrl = API_URL + "?text=" + encodedText;
            System.out.println("URL complète: " + fullUrl);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("X-Api-Key", API_KEY)
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();

            System.out.println("Envoi de la requête...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Code de statut: " + response.statusCode());
            System.out.println("Réponse brute: " + response.body());

            // Vérifier le statut de la réponse
            if (response.statusCode() == 200) {
                try {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    System.out.println("Objet JSON: " + jsonResponse.toString(2));

                    // Vérifier si la réponse contient le texte censuré
                    if (jsonResponse.has("censored")) {
                        String censored = jsonResponse.getString("censored");
                        System.out.println("Texte censuré par API: " + censored);
                        return censored;
                    } else {
                        System.err.println("Réponse API sans champ 'censored'");
                        return text;
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du parsing JSON: " + e.getMessage());
                    return text;
                }
            } else if (response.statusCode() == 429 && retryCount < MAX_RETRIES) {
                // Rate limiting - backoff and retry
                System.out.println("Rate limiting détecté, attente avant nouvelle tentative...");
                TimeUnit.SECONDS.sleep(2 * (retryCount + 1)); // Exponential backoff
                return tryApiFilter(text, retryCount + 1);
            } else {
                System.err.println("Erreur API (" + response.statusCode() + ")");
                return text; // Will trigger the local fallback
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Exception pendant le filtrage API: " + e.getMessage());
            e.printStackTrace();
            return text; // Will trigger the local fallback
        }
    }

    /**
     * Local fallback method for profanity filtering when API fails
     */
    private static String filterLocalFallback(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        System.out.println("FILTRAGE LOCAL: démarrage");
        String filteredContent = content;

        for (String word : BAD_WORDS) {
            // Replace whole words only using regex
            String regex = "(?i)\\b" + word + "\\b";
            StringBuilder replacement = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                replacement.append("*");
            }
            filteredContent = filteredContent.replaceAll(regex, replacement.toString());
        }

        System.out.println("FILTRAGE LOCAL: terminé");
        System.out.println("Mots recherchés = " + String.join(", ", BAD_WORDS));
        System.out.println("Résultat du filtrage local: " + filteredContent);

        return filteredContent;
    }

    // Test method
    public static void main(String[] args) {
        String testText = "This is a damn test with some bad words like shit and ass. Also French: putain, merde.";
        String filtered = filterText(testText);

        System.out.println("=== RÉSULTAT FINAL ===");
        System.out.println("Original: " + testText);
        System.out.println("Filtré  : " + filtered);
    }
}