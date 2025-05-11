package com.bye_bye.cmp2204;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Manager class for DataStore operations
 */
public class DataStoreManager {
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_SELECTED_MODEL = "selected_model";
    private static final String DEFAULT_MODEL = "gemini-2.0-flash";
    
    private final SharedPreferences preferences;

    public DataStoreManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Get the dark theme preference
     */
    public boolean getDarkTheme() {
        return preferences.getBoolean(KEY_DARK_THEME, false);
    }

    /**
     * Set the dark theme preference
     */
    public void setDarkTheme(boolean isDarkTheme) {
        preferences.edit().putBoolean(KEY_DARK_THEME, isDarkTheme).apply();
    }
    
    /**
     * Get the currently selected model
     */
    public String getSelectedModel() {
        return preferences.getString(KEY_SELECTED_MODEL, DEFAULT_MODEL);
    }
    
    /**
     * Set the selected model
     */
    public void setSelectedModel(String model) {
        preferences.edit().putString(KEY_SELECTED_MODEL, model).apply();
    }
}
