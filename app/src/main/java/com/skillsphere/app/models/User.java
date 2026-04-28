package com.skillsphere.app.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String name;
    private String email;
    private String username;
    private String photoUrl;
    private String bio = "";
    private String university = "";
    private String department = "";
    private String year = "";
    private String phone = "";
    private String location = "";
    private String linkedin = "";
    private List<String> skills = new ArrayList<>();
    private int projectsCount = 0;
    private int tasksCompletedCount = 0;
    private boolean isOnline = true;
    private long lastActive = System.currentTimeMillis();
    private boolean appearInDiscover = true;
    private boolean showOnlineStatus = true;
    private boolean pushNotifications = true;
    private boolean doNotDisturb = false;
    private long createdAt = System.currentTimeMillis();

    // Required empty constructor for Firestore
    public User() {}

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPhotoUrl() { return photoUrl; }
    public String getBio() { return bio; }
    public String getUniversity() { return university; }
    public String getDepartment() { return department; }
    public String getYear() { return year; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
    public String getLinkedin() { return linkedin; }
    public List<String> getSkills() { return skills; }
    public int getProjectsCount() { return projectsCount; }
    public int getTasksCompletedCount() { return tasksCompletedCount; }
    public boolean isOnline() { return isOnline; }
    public long getLastActive() { return lastActive; }
    public boolean isAppearInDiscover() { return appearInDiscover; }
    public boolean isShowOnlineStatus() { return showOnlineStatus; }
    public boolean isPushNotifications() { return pushNotifications; }
    public boolean isDoNotDisturb() { return doNotDisturb; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setBio(String bio) { this.bio = bio; }
    public void setUniversity(String university) { this.university = university; }
    public void setDepartment(String department) { this.department = department; }
    public void setYear(String year) { this.year = year; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLocation(String location) { this.location = location; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public void setProjectsCount(int projectsCount) { this.projectsCount = projectsCount; }
    public void setTasksCompletedCount(int tasksCompletedCount) { this.tasksCompletedCount = tasksCompletedCount; }
    public void setOnline(boolean online) { isOnline = online; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }
    public void setAppearInDiscover(boolean appearInDiscover) { this.appearInDiscover = appearInDiscover; }
    public void setShowOnlineStatus(boolean showOnlineStatus) { this.showOnlineStatus = showOnlineStatus; }
    public void setPushNotifications(boolean pushNotifications) { this.pushNotifications = pushNotifications; }
    public void setDoNotDisturb(boolean doNotDisturb) { this.doNotDisturb = doNotDisturb; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getInitials() {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + "" + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}