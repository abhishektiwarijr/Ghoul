package com.jr.ghoul;

import android.app.Application;
import android.content.SharedPreferences;

import com.jr.ghoul.dbhandler.DataBaseHelper;

public class GhoulApp extends Application {
    private DataBaseHelper dataBaseHelper;
    private static GhoulApp instance;
    private final String DEFAULT_PREF = "default_pref";
    private SharedPreferences default_prefs;

    public static GhoulApp getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
        this.default_prefs = getSharedPreferences("default_pref", 0);
        dataBaseHelper = new DataBaseHelper(this);
    }

    public DataBaseHelper getDbHelper() {
        return dataBaseHelper;
    }

    public SharedPreferences getDefaultPreference() {
        return this.default_prefs;
    }
}
