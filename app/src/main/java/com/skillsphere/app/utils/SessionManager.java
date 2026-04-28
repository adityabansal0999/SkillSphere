package com.skillsphere.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.skillsphere.app.models.User;

public class SessionManager {

    private static SessionManager instance;
    private final SharedPreferences prefs;
    private User currentUser;

    // Toggle preference keys
    private static final String KEY_PUSH_NOTIFICATIONS = "pref_push_notifications";
    private static final String KEY_DO_NOT_DISTURB = "pref_do_not_disturb";
    private static final String KEY_SHOW_ONLINE_STATUS = "pref_show_online_status";
    private static final String KEY_APPEAR_IN_DISCOVER = "pref_appear_in_discover";
    private static final String KEY_DARK_MODE = "pref_dark_mode";

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUser(User user) {
        this.currentUser = user;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putString(Constants.KEY_USER_ID, user.getId());
        editor.putString(Constants.KEY_USER_NAME, user.getName());
        editor.putString(Constants.KEY_USER_EMAIL, user.getEmail());
        // Cache toggle states from user object
        editor.putBoolean(KEY_PUSH_NOTIFICATIONS, user.isPushNotifications());
        editor.putBoolean(KEY_DO_NOT_DISTURB, user.isDoNotDisturb());
        editor.putBoolean(KEY_SHOW_ONLINE_STATUS, user.isShowOnlineStatus());
        editor.putBoolean(KEY_APPEAR_IN_DISCOVER, user.isAppearInDiscover());
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, null);
    }

    public String getUserName() {
        return prefs.getString(Constants.KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(Constants.KEY_USER_EMAIL, "");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // Toggle preference getters / setters (local cache, also synced to Firebase)
    public boolean getPushNotifications() {
        return prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true);
    }

    public void setPushNotifications(boolean value) {
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, value).apply();
        if (currentUser != null) currentUser.setPushNotifications(value);
    }

    public boolean getDoNotDisturb() {
        return prefs.getBoolean(KEY_DO_NOT_DISTURB, false);
    }

    public void setDoNotDisturb(boolean value) {
        prefs.edit().putBoolean(KEY_DO_NOT_DISTURB, value).apply();
        if (currentUser != null) currentUser.setDoNotDisturb(value);
    }

    public boolean getShowOnlineStatus() {
        return prefs.getBoolean(KEY_SHOW_ONLINE_STATUS, true);
    }

    public void setShowOnlineStatus(boolean value) {
        prefs.edit().putBoolean(KEY_SHOW_ONLINE_STATUS, value).apply();
        if (currentUser != null) currentUser.setShowOnlineStatus(value);
    }

    public boolean getAppearInDiscover() {
        return prefs.getBoolean(KEY_APPEAR_IN_DISCOVER, true);
    }

    public void setAppearInDiscover(boolean value) {
        prefs.edit().putBoolean(KEY_APPEAR_IN_DISCOVER, value).apply();
        if (currentUser != null) currentUser.setAppearInDiscover(value);
    }

    public boolean getDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean value) {
        prefs.edit().putBoolean(KEY_DARK_MODE, value).apply();
    }

    public void logout() {
        currentUser = null;
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public void updateUserName(String name) {
        prefs.edit().putString(Constants.KEY_USER_NAME, name).apply();
        if (currentUser != null) currentUser.setName(name);
    }
}