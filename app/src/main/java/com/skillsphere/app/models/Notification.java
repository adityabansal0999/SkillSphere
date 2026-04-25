package com.skillsphere.app.models;

public class Notification {
    private String id;
    private String type; // "join_request", "invite", "accepted", "rejected", "message", "task_assigned"
    private String title;
    private String body;
    private String projectId;
    private String projectTitle;
    private String fromUserId;
    private String fromUserName;
    private String actionType; // "accept_reject", "view_only"
    private String requestId;
    private boolean isRead;
    private long createdAt;

    // Required empty constructor for Firestore
    public Notification() {}

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getProjectId() { return projectId; }
    public String getProjectTitle() { return projectTitle; }
    public String getFromUserId() { return fromUserId; }
    public String getFromUserName() { return fromUserName; }
    public String getActionType() { return actionType; }
    public String getRequestId() { return requestId; }
    public boolean isRead() { return isRead; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setBody(String body) { this.body = body; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setRead(boolean read) { isRead = read; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}