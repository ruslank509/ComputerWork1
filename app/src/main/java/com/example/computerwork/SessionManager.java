package com.example.computerwork;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "ComputerWorkPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_LOGIN = "userLogin";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_PASSWORD = "userPassword";
    private static final String KEY_LAST_LOGIN_TIME = "lastLoginTime";

    private static final long SESSION_DURATION = 24 * 24 * 60 * 60 * 1000;

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = preferences.edit();
    }
    public boolean hasSavedLogin() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false) &&
                !preferences.getString(KEY_USER_LOGIN, "").isEmpty();
    }

    public boolean isSessionValid() {
        if (!hasSavedLogin()) return false;

        long lastLogin = preferences.getLong(KEY_LAST_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastLogin) <= SESSION_DURATION;
    }
    public void saveLoginData(String login, String password, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_LOGIN, login);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_PASSWORD, password);
        editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }

    public String getSavedLogin() {
        return preferences.getString(KEY_USER_LOGIN, "");
    }

    public String getSavedPassword() {
        return preferences.getString(KEY_USER_PASSWORD, "");
    }

    public String getSavedRole() {
        return preferences.getString(KEY_USER_ROLE, "");
    }

    public void updateLastActivity() {
        editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
    public void softLogout() {
        editor.remove(KEY_USER_PASSWORD);
        editor.remove(KEY_LAST_LOGIN_TIME);
        editor.apply();
    }
}
