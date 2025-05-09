package models;

import entities.*;
import java.time.LocalDateTime;

public class PostComment {

    private int id;
    private String pcommentContent;
    private LocalDateTime creationDate;
    private User user;
    private Post post;

    public PostComment() {
    }

    public PostComment(int id, String pcommentContent, LocalDateTime creationDate, User user, Post post) {
        this.id = id;
        this.pcommentContent = pcommentContent;
        this.creationDate = creationDate;
        this.user = user;
        this.post = post;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPcommentContent() {
        return pcommentContent;
    }

    public void setPcommentContent(String pcommentContent) {
        this.pcommentContent = pcommentContent;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public User getUser() {
        return user;
    }

    
    public void setUser(User currentUser) {
        this.user = currentUser;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Override
    public String toString() {
        return "PostComment{" +
                "id=" + id +
                ", pcommentContent='" + pcommentContent + '\'' +
                ", creationDate=" + creationDate +
                ", user=" + (user != null ? user.getFirstName() + " " + user.getLastName() : "null") +
                ", post=" + (post != null ? post.getId() : "null") +
                '}';
    }

   
}