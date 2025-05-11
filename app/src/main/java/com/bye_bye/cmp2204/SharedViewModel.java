package com.bye_bye.cmp2204;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

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
    
    // Flag to indicate reset is in progress to prevent race conditions
    private volatile boolean isResetInProgress = false;

    /**
     * Set the currently selected chat session
     */
    public synchronized void selectSession(ChatSession session) {
        // Don't allow session selection while reset is in progress
        if (isResetInProgress && session == null) {
            Log.d("SharedViewModel", "Ignoring null session during reset");
            return;
        }
        
        if (session != null) {
            Log.d("SharedViewModel", "Selecting session: " + session.getId() + " - " + session.getTitle());
        } else {
            Log.d("SharedViewModel", "Setting session to null");
        }
        
        selectedSession.postValue(session);
        
        // Clear any reset flag when a session is explicitly selected
        if (sessionReset.getValue() && session != null) {
            sessionReset.postValue(false);
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
    public synchronized void resetSession() {
        Log.d("SharedViewModel", "Resetting session");
        isResetInProgress = true;
        
        try {
            // Clear the current session
            selectedSession.postValue(null);
            
            // Set the reset flag to true
            sessionReset.postValue(true);
        } finally {
            // Ensure we always clear the flag even if an exception occurs
            isResetInProgress = false;
        }
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
        modelChanged.postValue(changed);
    }
    
    /**
     * Check if the model has changed
     */
    public LiveData<Boolean> isModelChanged() {
        return modelChanged;
    }
    
    /**
     * Check if reset is currently in progress
     */
    public boolean isResetInProgress() {
        return isResetInProgress;
    }
} 
