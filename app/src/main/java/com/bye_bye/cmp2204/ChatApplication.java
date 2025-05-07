package com.bye_bye.cmp2204;

import android.app.Application;
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
    }
    
    @Override
    public ViewModelStore getViewModelStore() {
        return viewModelStore;
    }
} 