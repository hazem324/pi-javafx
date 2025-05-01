package models;

import entities.*;
import java.time.LocalDateTime;
import java.util.List;

public class Post {

    private int id;
    private String content;
    private String postImg;
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;
    private Integer likes;

    private Community community;
    private User user;
    private List<PostComment> postComments;

    public Post() {
    }

    public Post(int id, String content, String postImg, LocalDateTime creationDate,
                LocalDateTime modificationDate, Integer likes,
                Community community, User user, List<PostComment> postComments) {
        this.id = id;
        this.content = content;
        this.postImg = postImg;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.likes = likes;
        this.community = community;
        this.user = user;
        this.postComments = postComments;
    }

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

    public String getPostImg() {
        return postImg;
    }

    public void setPostImg(String postImg) {
        this.postImg = postImg;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<PostComment> getPostComments() {
        return postComments;
    }

    public void setPostComments(List<PostComment> postComments) {
        this.postComments = postComments;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", postImg='" + postImg + '\'' +
                ", creationDate=" + creationDate +
                ", modificationDate=" + modificationDate +
                ", likes=" + likes +
                ", community=" + (community != null ? community.getName() : "null") +
                ", user=" + (user != null ? user.getFirstName() + " " + user.getLastName() : "null") +
                ", postComments=" + (postComments != null ? postComments.size() : "null") +
                '}';
    }
}
