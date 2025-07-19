package com.example.notepad;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Context ctx;

    public SharedPrefManager(Context ctx) {
        this.ctx = ctx;
        // Change from "login" to "LoginPrefs"
        prefs = ctx.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveLogin(String username) {
        editor.putString("username", username);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    public String getUsername() {
        return prefs.getString("username", "");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
