package com.skillsphere.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.skillsphere.app.models.User;

public class SessionManager {

    private static SessionManager instance;
    private final SharedPreferences prefs;
    private User currentUser;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Save user login session
     */
    public void saveUser(User user) {
        this.currentUser = user;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putString(Constants.KEY_USER_ID, user.getId());
        editor.putString(Constants.KEY_USER_NAME, user.getName());
        editor.putString(Constants.KEY_USER_EMAIL, user.getEmail());
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get current user ID
     */
    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, null);
    }

    /**
     * Get current user name
     */
    public String getUserName() {
        return prefs.getString(Constants.KEY_USER_NAME, "");
    }

    /**
     * Get current user email
     */
    public String getUserEmail() {
        return prefs.getString(Constants.KEY_USER_EMAIL, "");
    }

    /**
     * Get cached user object
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Set cached user object
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Clear session and logout
     */
    public void logout() {
        currentUser = null;
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Update user name in session
     */
    public void updateUserName(String name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.KEY_USER_NAME, name);
        editor.apply();
        if (currentUser != null) {
            currentUser.setName(name);
        }
    }
}