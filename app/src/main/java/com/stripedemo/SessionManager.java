package com.stripedemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/**
 * SessionManager : store all primitives local data
 */
public class SessionManager {
    // Shared Preferences
    private SharedPreferences obj_Preferences;

    // Editor for Shared preferences
    private Editor obj_Editor;
    // Context
    private Context obj_Context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "Stripe_here_pref";

    // Constructor
    public SessionManager(Context context) {
        this.obj_Context = context;
        obj_Preferences = obj_Context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    public void setBooleanDetail(String key, Boolean value) {
        obj_Editor = obj_Preferences.edit();
        obj_Editor.putBoolean(key, value);
        // commit changes
        obj_Editor.commit();
    }

    public boolean getBooleanDetail(String key) {
        obj_Editor = obj_Preferences.edit();
        boolean status = obj_Preferences.getBoolean(key, false);
        obj_Editor.commit();
        return status;
    }

    public void setStringDetail(String key, String value) {
        obj_Editor = obj_Preferences.edit();
        obj_Editor.putString(key, value);
        // commit changes
        obj_Editor.commit();
    }

    public String getStringDetail(String key) {
        obj_Editor = obj_Preferences.edit();
        String status = obj_Preferences.getString(key, "");
        obj_Editor.commit();
        return status;
    }

    public void setIntDetail(String key, int value) {
        obj_Editor = obj_Preferences.edit();
        obj_Editor.putInt(key, value);
        // commit changes
        obj_Editor.commit();
    }

    public int getIntDetail(String key) {
        obj_Editor = obj_Preferences.edit();
        int status = obj_Preferences.getInt(key, 0);
        obj_Editor.commit();
        return status;
    }

    public void logoutUser() {
        // Clearing all data from Shared Preferences
        obj_Editor = obj_Preferences.edit();
        obj_Editor.clear();
        obj_Editor.commit();
    }
}