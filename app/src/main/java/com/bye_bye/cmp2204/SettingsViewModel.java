package com.bye_bye.cmp2204;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SettingsViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> isDarkTheme;
    private final DataStoreManager dataStoreManager;

    public SettingsViewModel(Application application) {
        super(application);
        dataStoreManager = new DataStoreManager(application);
        
        // Load saved theme preference
        isDarkTheme = new MutableLiveData<>(dataStoreManager.getDarkTheme());
    }

    public LiveData<Boolean> isDarkTheme() {
        return isDarkTheme;
    }

    public void setDarkTheme(boolean isDark) {
        dataStoreManager.setDarkTheme(isDark);
        isDarkTheme.setValue(isDark);
    }

    public void clearChatHistory() {
        // TODO: Implement clear history functionality
        // This will be implemented when we add Room database support
    }
} 