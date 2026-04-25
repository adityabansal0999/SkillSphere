package com.skillsphere.app.models;

public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String senderInitials;
    private String type; // "text", "image"
    private String content;
    private String imageUrl;
    private long timestamp;

    // Required empty constructor for Firestore
    public Message() {}

    public Message(String senderId, String senderName, String content) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.type = "text";
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderInitials() { return senderInitials; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setSenderInitials(String senderInitials) { this.senderInitials = senderInitials; }
    public void setType(String type) { this.type = type; }
    public void setContent(String content) { this.content = content; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}