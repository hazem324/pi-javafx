package models;

public class Likes {
    private int id;
    private int post;
    private String userId;

    public Likes() {}

    public Likes(int post, String userId) {
        this.post = post;
        this.userId = userId;
    }

    public Likes(int id, int post, String userId) {
        this.id = id;
        this.post = post;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", post=" + post +
                ", userId='" + userId + '\'' +
                '}';
    }
}

