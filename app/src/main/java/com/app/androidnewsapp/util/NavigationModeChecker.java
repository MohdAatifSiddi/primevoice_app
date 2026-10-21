package com.app.androidnewsapp.util;

import android.content.ContentResolver;
import android.provider.Settings;
import android.content.Context;

public class NavigationModeChecker {

    public static boolean isGestureNavigationEnabled(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            // Android 10 (API level 29) introduced "navigation_mode"
            // with values 0 (3-button), 1 (2-button), 2 (gesture)
            int navigationMode = Settings.Secure.getInt(contentResolver, "navigation_mode");
            return navigationMode == 2; // 2 typically indicates gesture navigation
        } catch (Settings.SettingNotFoundException e) {
            // Handle cases where the setting might not exist (older Android versions)
            // or if there's an issue retrieving it.
            // For older versions, it's likely 3-button navigation by default.
            e.printStackTrace();
            return false; // Assume 3-button if setting not found or error
        }
    }

    public static boolean is3ButtonNavigationEnabled(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            int navigationMode = Settings.Secure.getInt(contentResolver, "navigation_mode");
            return navigationMode == 0; // 0 typically indicates 3-button navigation
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return true; // Assume 3-button if setting not found or error
        }
    }
}