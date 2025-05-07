package com.bye_bye.cmp2204;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Data storage manager for app preferences.
 * Using SharedPreferences for Java compatibility instead of DataStore.
 */
public class DataStoreManager {
    private static final String PREFERENCES_NAME = "settings";
    private static final String KEY_DARK_THEME = "dark_theme";

    private final SharedPreferences preferences;

    public DataStoreManager(Context context) {
        this.preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get the current theme setting
     * @return true if dark theme is enabled, false otherwise
     */
    public boolean getDarkTheme() {
        return preferences.getBoolean(KEY_DARK_THEME, false);
    }

    /**
     * Set the dark theme preference
     * @param isDark true to enable dark theme, false for light theme
     */
    public void setDarkTheme(boolean isDark) {
        preferences.edit().putBoolean(KEY_DARK_THEME, isDark).apply();
    }
} 