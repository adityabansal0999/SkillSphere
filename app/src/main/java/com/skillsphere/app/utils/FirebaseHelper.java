package com.skillsphere.app.utils;

import android.text.format.DateUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class FirebaseHelper {

    private static FirebaseAuth auth;
    private static FirebaseFirestore db;
    private static FirebaseStorage storage;

    /**
     * Get FirebaseAuth instance
     */
    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    /**
     * Get Firestore instance
     */
    public static FirebaseFirestore getFirestore() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }

    /**
     * Get Storage instance
     */
    public static FirebaseStorage getStorage() {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        return storage;
    }

    /**
     * Get current Firebase user
     */
    public static FirebaseUser getCurrentFirebaseUser() {
        return getAuth().getCurrentUser();
    }

    /**
     * Get current user ID
     */
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentFirebaseUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Check if user is logged in
     */
    public static boolean isUserLoggedIn() {
        return getCurrentFirebaseUser() != null;
    }

    /**
     * Get user document reference
     */
    public static DocumentReference getUserRef(String userId) {
        return getFirestore().collection(Constants.COLLECTION_USERS).document(userId);
    }

    /**
     * Get project document reference
     */
    public static DocumentReference getProjectRef(String projectId) {
        return getFirestore().collection(Constants.COLLECTION_PROJECTS).document(projectId);
    }

    /**
     * Get storage reference for profile photo
     */
    public static StorageReference getProfilePhotoRef(String userId) {
        return getStorage().getReference()
                .child(Constants.STORAGE_PATH_PROFILE + userId + ".jpg");
    }

    /**
     * Format timestamp to relative time (e.g., "2 hours ago")
     */
    public static String getRelativeTime(long timestamp) {
        return DateUtils.getRelativeTimeSpanString(
                timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString();
    }

    /**
     * Format timestamp to date string
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format timestamp to time string
     */
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format timestamp to date + time
     */
    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Get greeting based on time of day
     */
    public static String getGreeting() {
        int hour = new Date().getHours();
        if (hour < 12) {
            return "🌞 Good Morning";
        } else if (hour < 17) {
            return "☀️ Good Afternoon";
        } else {
            return "🌆 Good Evening";
        }
    }

    /**
     * Calculate skill match percentage (demo version - returns random 70-95%)
     */
    public static int calculateMatchPercentage() {
        return 70 + new Random().nextInt(26); // 70-95%
    }

    /**
     * Get random avatar color class
     */
    public static String getRandomAvatarColor() {
        String[] colors = Constants.AVATAR_COLORS;
        return colors[new Random().nextInt(colors.length)];
    }

    /**
     * Create notification data map
     */
    public static Map<String, Object> createNotificationData(
            String type,
            String title,
            String body,
            String projectId,
            String projectTitle,
            String fromUserId,
            String fromUserName
    ) {
        Map<String, Object> notif = new HashMap<>();
        notif.put("type", type);
        notif.put("title", title);
        notif.put("body", body);
        notif.put("projectId", projectId);
        notif.put("projectTitle", projectTitle);
        notif.put("fromUserId", fromUserId);
        notif.put("fromUserName", fromUserName);
        notif.put("isRead", false);
        notif.put("createdAt", System.currentTimeMillis());
        return notif;
    }

    /**
     * Send notification to user
     */
    public static void sendNotification(
            String toUserId,
            String type,
            String title,
            String body,
            String projectId,
            String projectTitle,
            String fromUserId,
            String fromUserName
    ) {
        Map<String, Object> notif = createNotificationData(
                type, title, body, projectId, projectTitle, fromUserId, fromUserName
        );

        getFirestore()
                .collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(toUserId)
                .collection(Constants.SUB_COLLECTION_ITEMS)
                .add(notif);
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate password (min 6 characters)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}