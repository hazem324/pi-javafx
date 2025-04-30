
package models;

import java.time.LocalDateTime;
public class CommunityMembers {






    private int id;
    private int communityId;
    private int userId;
    private LocalDateTime joinedAt;

    public CommunityMembers() {}

    public CommunityMembers(int communityId, int userId, LocalDateTime joinedAt) {
        this.communityId = communityId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommunityId() {
        return communityId;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Override
    public String toString() {
        return "CommunityMember{" +
                "id=" + id +
                ", communityId=" + communityId +
                ", userId=" + userId +
                ", joinedAt=" + joinedAt +
                '}';
    }
}
