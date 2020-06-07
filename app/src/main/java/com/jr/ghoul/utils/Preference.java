package com.jr.ghoul.utils;

import com.jr.ghoul.GhoulApp;

public class Preference {
    private static final String isDBAttached = "isDBAttached";

    public static boolean isDBAttached() {
        return GhoulApp.getInstance().getDefaultPreference().getBoolean(isDBAttached, false);
    }

    public static void setAttachedDb() {
        GhoulApp.getInstance().getDefaultPreference().edit().putBoolean(isDBAttached, false).apply();
    }
}
