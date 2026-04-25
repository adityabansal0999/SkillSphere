package com.skillsphere.app.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project {
    private String id;
    private String title;
    private String icon;
    private String description;
    private List<String> categories;
    private List<String> skillsRequired;
    private String university;
    private String leadId;
    private String leadName;
    private List<String> members;
    private Map<String, MemberDetail> memberDetails;
    private int maxMembers;
    private int openSlots;
    private String status; // "forming", "active", "completed", "cancelled"
    private String visibility; // "public", "private"
    private long createdAt;
    private long deadline;
    private long completedAt;
    private long lastMessageAt;
    private long lastActivityAt;

    public static class MemberDetail {
        public String name;
        public String role; // "lead", "member"
        public long joinedAt;

        public MemberDetail() {}

        public MemberDetail(String name, String role, long joinedAt) {
            this.name = name;
            this.role = role;
            this.joinedAt = joinedAt;
        }
    }

    // Required empty constructor for Firestore
    public Project() {
        this.categories = new ArrayList<>();
        this.skillsRequired = new ArrayList<>();
        this.members = new ArrayList<>();
        this.memberDetails = new HashMap<>();
    }

    public Project(String id, String title, String leadId, String leadName) {
        this.id = id;
        this.title = title;
        this.leadId = leadId;
        this.leadName = leadName;
        this.categories = new ArrayList<>();
        this.skillsRequired = new ArrayList<>();
        this.members = new ArrayList<>();
        this.members.add(leadId);
        this.memberDetails = new HashMap<>();
        this.memberDetails.put(leadId, new MemberDetail(leadName, "lead", System.currentTimeMillis()));
        this.status = "forming";
        this.visibility = "public";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    public List<String> getCategories() { return categories; }
    public List<String> getSkillsRequired() { return skillsRequired; }
    public String getUniversity() { return university; }
    public String getLeadId() { return leadId; }
    public String getLeadName() { return leadName; }
    public List<String> getMembers() { return members; }
    public Map<String, MemberDetail> getMemberDetails() { return memberDetails; }
    public int getMaxMembers() { return maxMembers; }
    public int getOpenSlots() { return openSlots; }
    public String getStatus() { return status; }
    public String getVisibility() { return visibility; }
    public long getCreatedAt() { return createdAt; }
    public long getDeadline() { return deadline; }
    public long getCompletedAt() { return completedAt; }
    public long getLastMessageAt() { return lastMessageAt; }
    public long getLastActivityAt() { return lastActivityAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setDescription(String description) { this.description = description; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public void setSkillsRequired(List<String> skillsRequired) { this.skillsRequired = skillsRequired; }
    public void setUniversity(String university) { this.university = university; }
    public void setLeadId(String leadId) { this.leadId = leadId; }
    public void setLeadName(String leadName) { this.leadName = leadName; }
    public void setMembers(List<String> members) { this.members = members; }
    public void setMemberDetails(Map<String, MemberDetail> memberDetails) { this.memberDetails = memberDetails; }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
        updateOpenSlots();
    }

    public void setOpenSlots(int openSlots) { this.openSlots = openSlots; }
    public void setStatus(String status) { this.status = status; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setDeadline(long deadline) { this.deadline = deadline; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    public void setLastMessageAt(long lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public void setLastActivityAt(long lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    // Helper methods
    public void updateOpenSlots() {
        this.openSlots = maxMembers - (members != null ? members.size() : 0);
    }

    public boolean isUserMember(String userId) {
        return members != null && members.contains(userId);
    }

    public boolean isUserLead(String userId) {
        return leadId != null && leadId.equals(userId);
    }

    public boolean hasOpenSlots() {
        return openSlots > 0;
    }
}