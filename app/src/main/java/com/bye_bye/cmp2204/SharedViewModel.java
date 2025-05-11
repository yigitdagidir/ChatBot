package com.bye_bye.cmp2204;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Shared ViewModel to pass data between fragments
 */
public class SharedViewModel extends ViewModel {
    // Current session data
    private final MutableLiveData<ChatSession> selectedSession = new MutableLiveData<>();
    
    // Flag to indicate a reset has occurred
    private final MutableLiveData<Boolean> sessionReset = new MutableLiveData<>(false);
    
    // Flag to indicate a model change
    private final MutableLiveData<Boolean> modelChanged = new MutableLiveData<>(false);

    /**
     * Set the currently selected chat session
     */
    public void selectSession(ChatSession session) {
        selectedSession.setValue(session);
        
        // Clear any reset flag when a session is explicitly selected
        if (sessionReset.getValue()) {
            sessionReset.setValue(false);
        }
    }

    /**
     * Get the currently selected chat session as LiveData
     */
    public LiveData<ChatSession> getSelectedSession() {
        return selectedSession;
    }
    
    /**
     * Reset the current session, clearing the selection.
     * This will trigger ViewModels to create a new session.
     */
    public void resetSession() {
        // Clear the current session
        selectedSession.setValue(null);
        
        // Set the reset flag to true
        sessionReset.setValue(true);
    }
    
    /**
     * Check if a session reset has occurred
     */
    public LiveData<Boolean> isSessionReset() {
        return sessionReset;
    }
    
    /**
     * Set the model changed flag
     */
    public void setModelChanged(boolean changed) {
        modelChanged.setValue(changed);
    }
    
    /**
     * Check if the model has changed
     */
    public LiveData<Boolean> isModelChanged() {
        return modelChanged;
    }
} 
