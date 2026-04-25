package com.skillsphere.app.models;

public class Task {
    private String id;
    private String projectId;
    private String title;
    private String description;
    private String assignedTo;
    private String assignedName;
    private String createdBy;
    private String status; // "pending", "in_progress", "done"
    private String priority; // "low", "medium", "high"
    private long dueDate;
    private long createdAt;
    private long completedAt;

    // Required empty constructor for Firestore
    public Task() {}

    public Task(String projectId, String title, String assignedTo, String assignedName) {
        this.projectId = projectId;
        this.title = title;
        this.assignedTo = assignedTo;
        this.assignedName = assignedName;
        this.status = "pending";
        this.priority = "medium";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAssignedTo() { return assignedTo; }
    public String getAssignedName() { return assignedName; }
    public String getCreatedBy() { return createdBy; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public long getDueDate() { return dueDate; }
    public long getCreatedAt() { return createdAt; }
    public long getCompletedAt() { return completedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public void setAssignedName(String assignedName) { this.assignedName = assignedName; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setStatus(String status) { this.status = status; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
}