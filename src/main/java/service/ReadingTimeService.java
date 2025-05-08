package service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service for estimating reading time of text content using the MeaningCloud Text Analytics API.
 */
public class ReadingTimeService {
    private static final String API_URL = "https://api.meaningcloud.com/deepcategorization-1.0";
    private static final String API_KEY = "YOUR_MEANINGCLOUD_KEY"; // Replace with your actual MeaningCloud API key

    private final HttpClient httpClient;

    public ReadingTimeService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Estimates the reading time for the given text content.
     *
     * @param text The text content to estimate reading time for
     * @param onSuccess Callback function to handle successful response
     * @param onError Callback function to handle errors
     */
    public void estimateReadingTime(String text, Consumer<ReadingTimeResult> onSuccess, Consumer<Exception> onError) {
        if (text == null || text.trim().isEmpty()) {
            onError.accept(new IllegalArgumentException("Text content cannot be empty"));
            return;
        }

        // Calculate reading time locally instead of using an API
        // Average reading speed is about 200-250 words per minute
        int wordCount = countWords(text);
        double minutes = wordCount / 225.0; // Using 225 words per minute as average reading speed
        int seconds = (int) Math.round((minutes - Math.floor(minutes)) * 60);

        ReadingTimeResult result = new ReadingTimeResult(wordCount, minutes, seconds);
        onSuccess.accept(result);
    }

    /**
     * Counts the number of words in the given text.
     *
     * @param text The text to count words in
     * @return The number of words
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        // Split by whitespace and count non-empty words
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    /**
     * Class representing the reading time estimation result.
     */
    public static class ReadingTimeResult {
        private final int wordCount;
        private final double minutes;
        private final int seconds;

        public ReadingTimeResult(int wordCount, double minutes, int seconds) {
            this.wordCount = wordCount;
            this.minutes = minutes;
            this.seconds = seconds;
        }

        public int getWordCount() {
            return wordCount;
        }

        public double getMinutes() {
            return minutes;
        }

        public int getSeconds() {
            return seconds;
        }

        /**
         * Returns a formatted string representation of the reading time.
         *
         * @return A string like "3 min read (450 words)"
         */
        public String getFormattedReadingTime() {
            if (minutes < 1) {
                return seconds + " sec read (" + wordCount + " words)";
            } else if (minutes < 1.5) {
                return "1 min read (" + wordCount + " words)";
            } else {
                return Math.round(minutes) + " min read (" + wordCount + " words)";
            }
        }
    }
}