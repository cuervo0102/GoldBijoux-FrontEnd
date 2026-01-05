package com.example.jewelryapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.jewelryapp.models.User;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_TOKEN = "token";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Save user session (method 1)
    public void saveSession(User user, String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_PHONE, user.getPhone());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // Create login session (method 2 - same as saveSession, just different name)
    public void createLoginSession(User user, String token) {
        saveSession(user, token);
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Get user ID
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    // Get user name
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    // Get user email
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    // Get user phone
    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, "");
    }

    // Get user role
    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "customer");
    }

    // Check if user is admin
    public boolean isAdmin() {
        String role = getUserRole();
        return role != null && role.equals("admin");
    }

    // Get token
    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    // Logout - clear session
    public void logout() {
        editor.clear();
        editor.apply();
    }

    // Update user info
    public void updateUserInfo(User user) {
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_PHONE, user.getPhone());
        editor.apply();
    }
}
