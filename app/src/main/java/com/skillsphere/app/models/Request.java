package com.skillsphere.app.models;

import java.util.List;

public class Request {
    private String id;
    private String type; // "join_request", "invite"
    private String fromUserId;
    private String fromUserName;
    private List<String> fromUserSkills;
    private String toUserId;
    private String projectId;
    private String projectTitle;
    private String status; // "pending", "accepted", "rejected"
    private long createdAt;
    private long respondedAt;
    private long expiresAt;

    // Required empty constructor for Firestore
    public Request() {}

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getFromUserId() { return fromUserId; }
    public String getFromUserName() { return fromUserName; }
    public List<String> getFromUserSkills() { return fromUserSkills; }
    public String getToUserId() { return toUserId; }
    public String getProjectId() { return projectId; }
    public String getProjectTitle() { return projectTitle; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
    public long getRespondedAt() { return respondedAt; }
    public long getExpiresAt() { return expiresAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }
    public void setFromUserSkills(List<String> fromUserSkills) { this.fromUserSkills = fromUserSkills; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setRespondedAt(long respondedAt) { this.respondedAt = respondedAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
}