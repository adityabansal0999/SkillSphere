package com.skillsphere.app.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String name;
    private String email;
    private String photoUrl;
    private String bio = "";
    private String university = "SkillSphere University";
    private String department = "";
    private String year = "";
    private List<String> skills = new ArrayList<>();
    private int projectsCount = 0;
    private int tasksCompletedCount = 0;
    private boolean isOnline = true;
    private long lastActive = System.currentTimeMillis();
    private boolean appearInDiscover = true;
    private boolean showOnlineStatus = true;
    private boolean pushNotifications = true;
    private long createdAt = System.currentTimeMillis();

    // Required empty constructor for Firestore
    public User() {
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
    public String getBio() { return bio; }
    public String getUniversity() { return university; }
    public String getDepartment() { return department; }
    public String getYear() { return year; }
    public List<String> getSkills() { return skills; }
    public int getProjectsCount() { return projectsCount; }
    public int getTasksCompletedCount() { return tasksCompletedCount; }
    public boolean isOnline() { return isOnline; }
    public long getLastActive() { return lastActive; }
    public boolean isAppearInDiscover() { return appearInDiscover; }
    public boolean isShowOnlineStatus() { return showOnlineStatus; }
    public boolean isPushNotifications() { return pushNotifications; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setBio(String bio) { this.bio = bio; }
    public void setUniversity(String university) { this.university = university; }
    public void setDepartment(String department) { this.department = department; }
    public void setYear(String year) { this.year = year; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public void setProjectsCount(int projectsCount) { this.projectsCount = projectsCount; }
    public void setTasksCompletedCount(int tasksCompletedCount) { this.tasksCompletedCount = tasksCompletedCount; }
    public void setOnline(boolean online) { isOnline = online; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }
    public void setAppearInDiscover(boolean appearInDiscover) { this.appearInDiscover = appearInDiscover; }
    public void setShowOnlineStatus(boolean showOnlineStatus) { this.showOnlineStatus = showOnlineStatus; }
    public void setPushNotifications(boolean pushNotifications) { this.pushNotifications = pushNotifications; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helper method to get initials for avatar
    public String getInitials() {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + "" + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}