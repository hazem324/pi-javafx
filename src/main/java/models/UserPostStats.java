package models;


public class UserPostStats {
    private String fullName;
    private int postCount;

    public UserPostStats(String fullName, int postCount) {
        this.fullName = fullName;
        this.postCount = postCount;
    }

    public String getFullName() {
        return fullName;
    }

    public int getPostCount() {
        return postCount;
    }

    @Override
    public String toString() {
        return fullName + ": " + postCount + " posts";
    }
}
