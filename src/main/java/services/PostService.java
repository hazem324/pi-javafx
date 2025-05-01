package services;

import interfaces.IService;
import models.Community;
import models.Post;
import models.PostComment;
import entities.*;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostService implements IService<Post> {

    private Connection cnx;

    public PostService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    public static class PostSummary {
        private int id;
        private String author;
        private String content;
        private String communityName;

        public PostSummary(int id, String author, String content, String communityName) {
            this.id = id;
            this.author = author;
            this.content = content;
            this.communityName = communityName;
        }

        public int getId() { return id; }
        public String getAuthor() { return author; }
        public String getContent() { return content; }
        public String getCommunityName() { return communityName; }
    }

    @Override
    public void ajouter(Post post) throws SQLException {
        String sql = "INSERT INTO post (content, post_img, creation_date, modification_date, likes, community_id, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getContent());
            ps.setString(2, post.getPostImg());
            ps.setTimestamp(3, post.getCreationDate() != null ? Timestamp.valueOf(post.getCreationDate()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(4, post.getModificationDate() != null ? Timestamp.valueOf(post.getModificationDate()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(5, post.getLikes() != null ? post.getLikes() : 0);
            ps.setInt(6, post.getCommunity().getId());
            ps.setInt(7, post.getUser().getId());
            ps.executeUpdate();

            // Retrieve generated post ID
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                post.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public void modifier(Post post) throws SQLException {
        String sql = "UPDATE post SET content = ?, post_img = ?, creation_date = ?, modification_date = ?, " +
                "likes = ?, community_id = ?, user_id = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, post.getContent());
            ps.setString(2, post.getPostImg());
            ps.setTimestamp(3, post.getCreationDate() != null ? Timestamp.valueOf(post.getCreationDate()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(4, post.getModificationDate() != null ? Timestamp.valueOf(post.getModificationDate()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(5, post.getLikes() != null ? post.getLikes() : 0);
            ps.setInt(6, post.getCommunity().getId());
            ps.setInt(7, post.getUser().getId());
            ps.setInt(8, post.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM post WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Post> recuperer() throws SQLException {
        return getPostsByCommunityId(0); // Fetch all posts if community ID is 0
    }

    public List<Post> getPostsByCommunityId(int communityId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.id, p.content, p.post_img, p.creation_date, p.modification_date, p.likes, " +
                "p.community_id, p.user_id, c.name AS community_name, u.first_name, u.last_name " +
                "FROM post p " +
                "LEFT JOIN community c ON p.community_id = c.id " +
                "LEFT JOIN user u ON p.user_id = u.id " +
                (communityId > 0 ? "WHERE p.community_id = ? " : "") +
                "ORDER BY p.creation_date DESC";
    
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (communityId > 0) {
                ps.setInt(1, communityId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setContent(rs.getString("content"));
                    post.setPostImg(rs.getString("post_img"));
                    Timestamp creationTs = rs.getTimestamp("creation_date");
                    post.setCreationDate(creationTs != null ? creationTs.toLocalDateTime() : null);
                    Timestamp modificationTs = rs.getTimestamp("modification_date");
                    post.setModificationDate(modificationTs != null ? modificationTs.toLocalDateTime() : null);
                    post.setLikes(rs.getObject("likes") != null ? rs.getInt("likes") : 0);
    
                    // Set Community
                    Community community = new Community();
                    community.setId(rs.getInt("community_id"));
                    community.setName(rs.getString("community_name"));
                    post.setCommunity(community);
    
                    // Set Author
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    post.setUser(user);
                    // Fetch comments
                    post.setPostComments(getCommentsByPostId(post.getId()));
    
                    posts.add(post);
                }
            }
        }
        return posts;
    }
    

    public List<PostSummary> getPostAuthorContentCommunity() throws SQLException {
        List<PostSummary> summaries = new ArrayList<>();
        String sql = "SELECT p.id, p.content, c.name AS community_name, " +
                "CONCAT(u.first_name, ' ', u.last_name) AS author " +
                "FROM post p " +
                "LEFT JOIN community c ON p.community_id = c.id " +
                "LEFT JOIN user u ON p.user_id = u.id " +
                "ORDER BY p.id DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String author = rs.getString("author");
                String content = rs.getString("content");
                String communityName = rs.getString("community_name");
                summaries.add(new PostSummary(id, author, content, communityName));
            }
        }
        return summaries;
    }

    public void addPostToCommunity(String content, String postImg, int communityId, int userId) throws SQLException {
        Post post = new Post();
        post.setContent(content);
        post.setPostImg(postImg);
        post.setCreationDate(LocalDateTime.now());
        post.setModificationDate(LocalDateTime.now());
        post.setLikes(0);

        Community community = new Community();
        community.setId(communityId);
        post.setCommunity(community);

        User user = new User();
        user.setId(userId);
        user.setFirstName("User One"); // Adjust as needed or fetch from DB
        post.setUser(user);

        ajouter(post);
    }

    public boolean likePost(int postId) {
        try {
            String sql = "UPDATE post SET likes = likes + 1 WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, postId);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addComment(PostComment comment) {
        try {
            String sql = "INSERT INTO post_comment (pcomment_content, post_id, user_id, creation_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setString(1, comment.getPcommentContent());
                ps.setInt(2, comment.getPost().getId());
                ps.setInt(3, comment.getUser().getId());
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<PostComment> getCommentsByPostId(int postId) throws SQLException {
        List<PostComment> comments = new ArrayList<>();
        String sql = "SELECT pc.id, pc.pcomment_content, pc.post_id, pc.user_id, pc.creation_date, " +
                "u.first_name, u.last_name " +
                "FROM post_comment pc " +
                "LEFT JOIN user u ON pc.user_id = u.id " +
                "WHERE pc.post_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PostComment comment = new PostComment();
                    comment.setId(rs.getInt("id"));
                    comment.setPcommentContent(rs.getString("pcomment_content"));

                    Post post = new Post();
                    post.setId(rs.getInt("post_id"));
                    comment.setPost(post);

                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    comment.setUser(user);

                    Timestamp creationTs = rs.getTimestamp("creation_date");
                    comment.setCreationDate(creationTs != null ? creationTs.toLocalDateTime() : null);

                    comments.add(comment);
                }
            }
        }
        return comments;
    }

    // New method to get a post by ID
    public Post getPostById(int id) throws SQLException {
        String sql = "SELECT p.id, p.content, p.post_img, p.creation_date, p.modification_date, p.likes, " +
                "p.community_id, p.user_id, c.name AS community_name, u.first_name, u.last_name " +
                "FROM post p " +
                "LEFT JOIN community c ON p.community_id = c.id " +
                "LEFT JOIN user u ON p.user_id = u.id " +
                "WHERE p.id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setContent(rs.getString("content"));
                    post.setPostImg(rs.getString("post_img"));
                    Timestamp creationTs = rs.getTimestamp("creation_date");
                    post.setCreationDate(creationTs != null ? creationTs.toLocalDateTime() : null);
                    Timestamp modificationTs = rs.getTimestamp("modification_date");
                    post.setModificationDate(modificationTs != null ? modificationTs.toLocalDateTime() : null);
                    post.setLikes(rs.getObject("likes") != null ? rs.getInt("likes") : 0);

                    // Set Community
                    Community community = new Community();
                    community.setId(rs.getInt("community_id"));
                    community.setName(rs.getString("community_name"));
                    post.setCommunity(community);

                    // Set Author
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    post.setUser(user);

                    // Fetch comments
                    post.setPostComments(getCommentsByPostId(post.getId()));

                    return post;
                }
            }
        }
        return null;
    }

    // New method to get the latest post ID
    public int getLatestPostId() throws SQLException {
        String sql = "SELECT id FROM post ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        throw new SQLException("No posts found in the database");
    }
}