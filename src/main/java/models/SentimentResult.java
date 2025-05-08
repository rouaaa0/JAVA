package models;

/**
 * Model class for sentiment analysis results.
 */
public class SentimentResult {
    private double score;
    private String sentiment;

    public SentimentResult(double score, String sentiment) {
        this.score = score;
        this.sentiment = sentiment;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    @Override
    public String toString() {
        return "Sentiment: " + sentiment + " (Score: " + score + ")";
    }
}