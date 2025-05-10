package com.bye_bye.cmp2204;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

public class SettingsViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> isDarkTheme;
    private final DataStoreManager dataStoreManager;
    private final ChatRepository repository;
    private final SharedViewModel sharedViewModel;

    public SettingsViewModel(Application application) {
        super(application);
        dataStoreManager = new DataStoreManager(application);
        repository = new ChatRepository(application);
        
        // Get the shared view model from application
        ViewModelStoreOwner viewModelStoreOwner = (ViewModelStoreOwner) application;
        sharedViewModel = new ViewModelProvider(viewModelStoreOwner).get(SharedViewModel.class);
        
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
        repository.wipeDatabaseNow();        // ← synchronous – guaranteed empty DB
        sharedViewModel.resetSession();      // flag so Chat / Sidebar react
    }
}