package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for sentiment analysis using the API Ninjas sentiment API.
 */
public class SentimentService {

    private static final String API_URL = "https://api.api-ninjas.com/v1/sentiment";
    private static final String API_KEY = "iLzwMZbBYUvli4TeEbvLww==R7pTNsYpzfvpH0Ay"; // Replace with your actual API key

    /**
     * Analyzes the sentiment of the provided text.
     *
     * @param text The text to analyze
     * @return A map containing the sentiment score and category
     * @throws Exception If there's an error during the API call
     */
    public Map<String, Object> analyzeSentiment(String text) throws Exception {
        // Prepare URL with text parameter
        String urlString = API_URL + "?text=" + java.net.URLEncoder.encode(text, "UTF-8");
        URL url = new URL(urlString);

        // Setup connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Api-Key", API_KEY);
        connection.setRequestProperty("Content-Type", "application/json");

        // Get response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse JSON response
            // For simplicity, we'll do basic parsing since the response is simple
            String jsonResponse = response.toString();
            double score = extractScoreFromJson(jsonResponse);
            String sentiment = determineSentiment(score);

            Map<String, Object> result = new HashMap<>();
            result.put("score", score);
            result.put("sentiment", sentiment);
            return result;
        } else {
            throw new Exception("API Error: HTTP response code " + responseCode);
        }
    }

    /**
     * Extracts the sentiment score from the JSON response.
     */
    private double extractScoreFromJson(String json) {
        // Simple JSON parsing for the score value
        int scoreIndex = json.indexOf("\"score\":");
        if (scoreIndex != -1) {
            int startIndex = scoreIndex + 8; // Length of "\"score\":"
            int endIndex = json.indexOf("}", startIndex);
            if (endIndex != -1) {
                String scoreStr = json.substring(startIndex, endIndex).trim();
                return Double.parseDouble(scoreStr);
            }
        }
        return 0.0; // Default if parsing fails
    }

    /**
     * Determines the sentiment category based on the score.
     */
    private String determineSentiment(double score) {
        if (score > 0.5) {
            return "Positive";
        } else if (score < -0.5) {
            return "Negative";
        } else {
            return "Neutral";
        }
    }
}