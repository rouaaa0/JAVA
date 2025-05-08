package models;

import java.util.List;
import java.util.Map;

public class Post {
    private int id;
    private String content;
    private String image;
    private String createdAt;
    private String updatedAt;
    private Blog blog;
    private int likeCount;
    private int dislikeCount;
    private boolean spellChecked; // Attribute to indicate if content has been spell-checked
    private Map<String, List<String>> spellErrors; // To store spelling errors and their suggestions
    private double sentimentScore;
    private String sentimentLabel;

    // Added to track the current user's reaction to this post
    private String userReaction; // "LIKE", "DISLIKE", or null

    // Constructors
    public Post() {
        this.spellChecked = false;
    }

    public Post(int id, String content, String image, String createdAt, String updatedAt, Blog blog) {
        this.id = id;
        this.content = content;
        this.image = image;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.blog = blog;
        this.spellChecked = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.spellChecked = false; // Reset spell check status
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public boolean isSpellChecked() {
        return spellChecked;
    }

    public void setSpellChecked(boolean spellChecked) {
        this.spellChecked = spellChecked;
    }

    public Map<String, List<String>> getSpellErrors() {
        return spellErrors;
    }

    public void setSpellErrors(Map<String, List<String>> spellErrors) {
        this.spellErrors = spellErrors;
    }

    // Methods to increment
    public void incrementLike() {
        this.likeCount++;
    }

    public void incrementDislike() {
        this.dislikeCount++;
    }

    // Getter and setter for userReaction
    public String getUserReaction() {
        return userReaction;
    }

    public void setUserReaction(String userReaction) {
        this.userReaction = userReaction;
    }

    // Helper methods to check if current user has liked/disliked
    public boolean hasUserLiked() {
        return "LIKE".equals(userReaction);
    }

    public boolean hasUserDisliked() {
        return "DISLIKE".equals(userReaction);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", blog=" + (blog != null ? blog.getTitle() : "null") +
                ", likeCount=" + likeCount +
                ", dislikeCount=" + dislikeCount +
                ", spellChecked=" + spellChecked +
                ", userReaction='" + userReaction + '\'' +
                '}';
    }

    // Getters and setters for sentiment analysis
    public double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public String getSentimentLabel() {
        return sentimentLabel;
    }

    public void setSentimentLabel(String sentimentLabel) {
        this.sentimentLabel = sentimentLabel;
    }
}