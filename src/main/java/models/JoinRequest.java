package models;

import java.time.LocalDateTime;

public class JoinRequest {

    private int id;
    private int userId;
    private int communityId;
    private LocalDateTime joinDate;
    private String status;

    public JoinRequest() {}

    public JoinRequest(int userId, int communityId, LocalDateTime joinDate, String status) {
        this.userId = userId;
        this.communityId = communityId;
        this.joinDate = joinDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCommunityId() {
        return communityId;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "JoinRequest{" +
                "id=" + id +
                ", userId=" + userId +
                ", communityId=" + communityId +
                ", joinDate=" + joinDate +
                ", status='" + status + '\'' +
                '}';
    }

}
