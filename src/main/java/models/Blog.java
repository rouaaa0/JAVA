package models;

public class Blog {
    private int id;
    private String title;
    private String description;
    private String createdAtBlog; // Changed to String
    private String updatedAtBlog; // Changed to String

    // Default constructor
    public Blog() {
    }

    // Constructor with all fields
    public Blog(int id, String title, String description, String createdAtBlog, String updatedAtBlog) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAtBlog = createdAtBlog;
        this.updatedAtBlog = updatedAtBlog;
    }

    // Constructor without ID (for creating new blogs)
    public Blog(String title, String description, String createdAtBlog, String updatedAtBlog) {
        this.title = title;
        this.description = description;
        this.createdAtBlog = createdAtBlog;
        this.updatedAtBlog = updatedAtBlog;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAtBlog() {
        return createdAtBlog;
    }

    public void setCreatedAtBlog(String createdAtBlog) {
        this.createdAtBlog = createdAtBlog;
    }

    public String getUpdatedAtBlog() {
        return updatedAtBlog;
    }

    public void setUpdatedAtBlog(String updatedAtBlog) {
        this.updatedAtBlog = updatedAtBlog;
    }

    // Override toString for debugging purposes
    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", createdAtBlog='" + createdAtBlog + '\'' +
                ", updatedAtBlog='" + updatedAtBlog + '\'' +
                '}';
    }
}