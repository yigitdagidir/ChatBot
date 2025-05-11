package com.bye_bye.cmp2204;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

/**
 * Application class that provides application-level services like shared ViewModels
 */
public class ChatApplication extends Application implements ViewModelStoreOwner {
    private ViewModelStore viewModelStore;

    @Override
    public void onCreate() {
        super.onCreate();
        viewModelStore = new ViewModelStore();
        DataStoreManager dataStoreManager = new DataStoreManager(this);
        boolean isDark = dataStoreManager.getDarkTheme();
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    @Override
    public ViewModelStore getViewModelStore() {
        return viewModelStore;
    }
}
