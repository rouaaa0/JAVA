package models;

public class Post {
    private int id;
    private String content;
    private String image;
    private String createdAt; // Changed to String
    private String updatedAt; // Changed to String
    private Blog blog; // Relationship with Blog

    // Default constructor
    public Post() {
    }

    // Constructor with all fields
    public Post(int id, String content, String image, String createdAt, String updatedAt, Blog blog) {
        this.id = id;
        this.content = content;
        this.image = image;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.blog = blog;
    }

    // Constructor without ID (for creating new posts)
    public Post(String content, String image, String createdAt, String updatedAt, Blog blog) {
        this.content = content;
        this.image = image;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.blog = blog;
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

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", image='" + image + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", blog=" + (blog != null ? blog.toString() : "null") +
                '}';
    }
}