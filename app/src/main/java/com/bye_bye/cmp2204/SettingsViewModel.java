package com.bye_bye.cmp2204;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> isDarkTheme;
    private final MutableLiveData<String> selectedModel;
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
        
        // Load saved model preference
        selectedModel = new MutableLiveData<>(dataStoreManager.getSelectedModel());
    }

    public LiveData<Boolean> isDarkTheme() {
        return isDarkTheme;
    }

    public void setDarkTheme(boolean isDark) {
        dataStoreManager.setDarkTheme(isDark);
        isDarkTheme.setValue(isDark);
    }
    
    public LiveData<String> getSelectedModel() {
        return selectedModel;
    }
    
    public void setSelectedModel(String model) {
        dataStoreManager.setSelectedModel(model);
        selectedModel.setValue(model);
        
        // Notify that the model has changed
        sharedViewModel.setModelChanged(true);
    }

    /**
     * Clears all chat history and creates a new session
     * @return The newly created session ID
     */
    public ChatSession clearChatHistory() {
        ChatSession newSession = null;
        
        try {
            // Log operation start
            Log.d("SettingsViewModel", "Clearing chat history - starting database wipe");
            
            // Perform database wipe synchronously
            repository.wipeDatabaseNow();
            
            // Signal reset in progress
            Log.d("SettingsViewModel", "Database wiped, signaling reset to other components");
            sharedViewModel.resetSession();
            
            // Create a new session after reset
            Log.d("SettingsViewModel", "Creating new session after reset");
            newSession = createSessionAfterReset();
            
            // Select the new session to broadcast to all components
            Log.d("SettingsViewModel", "Selecting new session: " + newSession.getId());
            sharedViewModel.selectSession(newSession);
            
            // Success log
            Log.d("SettingsViewModel", "Chat history cleared successfully");
        } catch (Exception e) {
            // Log error but don't crash
            Log.e("SettingsViewModel", "Error clearing chat history", e);
            
            // Try to recover by creating a new session anyway
            try {
                newSession = createSessionAfterReset();
                sharedViewModel.selectSession(newSession);
                Log.d("SettingsViewModel", "Created recovery session after error");
            } catch (Exception e2) {
                Log.e("SettingsViewModel", "Failed to create recovery session", e2);
            }
        }
        
        return newSession;
    }

    /**
     * Creates a new session after database reset
     * @return The newly created session
     */
    private ChatSession createSessionAfterReset() {
        String title = new SimpleDateFormat("'Chat •' MMM dd HH:mm",
                Locale.getDefault()).format(new Date());
        
        ChatSession newSession = new ChatSession(title, dataStoreManager.getSelectedModel());
        long id = repository.insertSessionSync(newSession);
        newSession.setId(id);
        
        // Add welcome message
        repository.insertMessageSync(new ChatMessage(
                "Hello! How can I assist you today?", false, id));
                
        return newSession;
    }
}
