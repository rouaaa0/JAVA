package service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SpellCheckService {

    private static final String API_URL = "https://api.api-ninjas.com/v1/spellcheck";
    private static final String API_KEY = "iLzwMZbBYUvli4TeEbvLww==R7pTNsYpzfvpH0Ay"; // Remplacez par votre clé API

    /**
     * Vérifie l'orthographe du texte fourni en utilisant l'API Spell Check
     * @param text Le texte à vérifier
     * @return Une liste de suggestions pour les mots mal orthographiés
     */
    public Map<String, List<String>> checkSpelling(String text) {
        Map<String, List<String>> suggestions = new HashMap<>();

        try {
            // Debug: Print the text being sent to API
            System.out.println("Checking spelling for: \"" + text + "\"");

            // Custom spelling check for common errors the API might miss
            Map<String, List<String>> customChecks = performCustomSpellCheck(text);
            if (!customChecks.isEmpty()) {
                System.out.println("Custom spell check found errors: " + customChecks.keySet());
                suggestions.putAll(customChecks);
            }

            // Encodage du texte pour l'URL
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            URL url = new URL(API_URL + "?text=" + encodedText);

            // Configuration de la connexion HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Api-Key", API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");

            // Lecture de la réponse
            int responseCode = connection.getResponseCode();
            System.out.println("API Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Print raw API response for debugging
                System.out.println("Raw API response: " + response.toString());

                // Traitement de la réponse JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray corrections = jsonResponse.getJSONArray("corrections");

                for (int i = 0; i < corrections.length(); i++) {
                    JSONObject correction = corrections.getJSONObject(i);
                    String word = correction.getString("word");
                    List<String> wordSuggestions = new ArrayList<>();

                    JSONArray suggestionsArray = correction.getJSONArray("suggestions");
                    for (int j = 0; j < suggestionsArray.length(); j++) {
                        wordSuggestions.add(suggestionsArray.getString(j));
                    }

                    suggestions.put(word, wordSuggestions);
                }
            } else {
                System.err.println("Erreur de l'API Spell Check: " + responseCode);

                // Try to read error response
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    System.err.println("Error response: " + errorResponse.toString());
                } catch (Exception e) {
                    System.err.println("Could not read error response: " + e.getMessage());
                }
            }

            connection.disconnect();

            // After processing the API response:
            if (suggestions.isEmpty()) {
                System.out.println("No spelling errors detected (both API and custom checks)");
            } else {
                System.out.println("Found " + suggestions.size() + " spelling errors in total");
                for (Map.Entry<String, List<String>> entry : suggestions.entrySet()) {
                    System.out.println("- Misspelled: '" + entry.getKey() + "', Suggestions: " + entry.getValue());
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification orthographique: " + e.getMessage());
            e.printStackTrace();
        }

        return suggestions;
    }

    /**
     * Effectue une vérification orthographique personnalisée pour les erreurs courantes
     * @param text Le texte à vérifier
     * @return Une carte des erreurs et suggestions
     */
    private Map<String, List<String>> performCustomSpellCheck(String text) {
        Map<String, List<String>> customErrors = new HashMap<>();

        // Diviser le texte en mots
        String[] words = text.split("\\s+");

        for (String word : words) {
            // Nettoyer le mot de la ponctuation
            String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

            // Vérifier les fautes d'orthographe courantes
            if (cleanWord.equals("kiddign")) {
                List<String> suggestions = new ArrayList<>();
                suggestions.add("kidding");
                customErrors.put("kiddign", suggestions);
            }

            // Autres fautes courantes à ajouter selon les besoins
            // Exemples de motifs de fautes courantes:
            if (cleanWord.endsWith("ign") && cleanWord.length() > 3) {
                String potential = cleanWord.substring(0, cleanWord.length() - 3) + "ing";
                checkAndAddPotentialError(cleanWord, potential, customErrors);
            }

            // Vérifier les inversions de lettres à la fin des mots
            if (cleanWord.length() > 2) {
                char secondLast = cleanWord.charAt(cleanWord.length() - 2);
                char last = cleanWord.charAt(cleanWord.length() - 1);
                String potentialCorrection = cleanWord.substring(0, cleanWord.length() - 2) + last + secondLast;
                checkAndAddPotentialError(cleanWord, potentialCorrection, customErrors);
            }
        }

        return customErrors;
    }

    /**
     * Vérifie si un mot potentiellement correct devrait être ajouté comme suggestion
     */
    private void checkAndAddPotentialError(String original, String potential, Map<String, List<String>> errors) {
        // Une liste de mots corrects pour vérification
        // Ceci est un exemple simple, dans un cas réel vous pourriez utiliser un dictionnaire
        String[] commonWords = {"running", "writing", "going", "coming", "doing", "making", "taking", "kidding"};

        for (String word : commonWords) {
            if (potential.equals(word)) {
                List<String> suggestions = new ArrayList<>();
                suggestions.add(word);
                errors.put(original, suggestions);
                break;
            }
        }
    }

    /**
     * Corrige automatiquement le texte en remplaçant les mots mal orthographiés par leur première suggestion
     * @param text Le texte à corriger
     * @return Le texte corrigé
     */
    public String autoCorrectText(String text) {
        Map<String, List<String>> spellingErrors = checkSpelling(text);
        String correctedText = text;

        if (spellingErrors.isEmpty()) {
            System.out.println("No spelling errors detected, returning original text");
            return correctedText;
        }

        System.out.println("Auto-correcting these spelling errors: " + spellingErrors.keySet());

        for (Map.Entry<String, List<String>> entry : spellingErrors.entrySet()) {
            String misspelledWord = entry.getKey();
            List<String> suggestions = entry.getValue();

            if (!suggestions.isEmpty()) {
                String originalText = correctedText;

                // Create a safer regex pattern with word boundaries
                String regex = "\\b" + Pattern.quote(misspelledWord) + "\\b";
                correctedText = correctedText.replaceAll(regex, suggestions.get(0));

                // If no replacement happened (possibly due to word boundaries), try without strict boundaries
                if (correctedText.equals(originalText)) {
                    System.out.println("Standard replacement failed for '" + misspelledWord + "', trying looser match");
                    correctedText = correctedText.replace(misspelledWord, suggestions.get(0));
                }

                // Verify if replacement happened
                if (!correctedText.equals(originalText)) {
                    System.out.println("Successfully replaced '" + misspelledWord + "' with '" + suggestions.get(0) + "'");
                } else {
                    System.out.println("Failed to replace '" + misspelledWord + "' - not found in text");
                }
            }
        }

        // Final verification
        if (correctedText.equals(text)) {
            System.out.println("Warning: No words were actually replaced in the text");
        } else {
            System.out.println("Original: \"" + text + "\"");
            System.out.println("Corrected: \"" + correctedText + "\"");
        }

        return correctedText;
    }

    /**
     * Obtient le nombre de mots mal orthographiés dans un texte
     * @param text Le texte à vérifier
     * @return Le nombre de mots mal orthographiés
     */
    public int getErrorCount(String text) {
        return checkSpelling(text).size();
    }
}