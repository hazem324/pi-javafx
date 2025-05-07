package models.statique;

public class CommunityPostStats {
    
    private String communityName;
    private int postCount;

   
    public CommunityPostStats() {
    }

   
    public CommunityPostStats(String communityName, int postCount) {
        this.communityName = communityName;
        this.postCount = postCount;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

   
    public int getPostCount() {
        return postCount;
    }

   
    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

   
    @Override
    public String toString() {
        return "CommunityPostStats{" +
               "communityName='" + communityName + '\'' +
               ", postCount=" + postCount +
               '}';
    }
}