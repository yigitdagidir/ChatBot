package com.bye_bye.cmp2204;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private final ChatRepository repository;
    private LiveData<List<ChatMessage>> messages;
    private long currentSessionId;
    private final MutableLiveData<String> currentSessionTitle;
    private final SharedViewModel sharedViewModel;
    
    private final Observer<ChatSession> sessionObserver = new Observer<ChatSession>() {
        @Override
        public void onChanged(ChatSession session) {
            if (session != null) {
                // Update current session data
                currentSessionId = session.getId();
                currentSessionTitle.setValue(session.getTitle());
                
                // Load messages for this session
                loadMessagesForSession(currentSessionId);
            } else {
                // Session is null - we may need to create a new one, but only if we're active
                currentSessionId = -1;
                messages = new MutableLiveData<>(new ArrayList<>());
            }
        }
    };
    
    private final Observer<Boolean> resetObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean reset) {
            if (reset != null && reset) {
                // A reset has occurred - create a new session
                createNewSession();
            }
        }
    };

    public ChatViewModel(Application application) {
        super(application);
        repository = new ChatRepository(application);
        messages = new MutableLiveData<>(new ArrayList<>());
        currentSessionId = -1; // Invalid ID until set
        currentSessionTitle = new MutableLiveData<>("New Chat");
        
        // Get the SharedViewModel
        ViewModelStoreOwner viewModelStoreOwner = (ViewModelStoreOwner) application;
        sharedViewModel = new ViewModelProvider(viewModelStoreOwner).get(SharedViewModel.class);
        
        // Observe session changes
        sharedViewModel.getSelectedSession().observeForever(sessionObserver);
        
        // Observe session reset events
        sharedViewModel.isSessionReset().observeForever(resetObserver);
        
        // Create a default session if needed (initially)
        if (currentSessionId <= 0) {
            createDefaultSession();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Stop observing when ViewModel is destroyed
        sharedViewModel.getSelectedSession().removeObserver(sessionObserver);
        sharedViewModel.isSessionReset().removeObserver(resetObserver);
    }
    
    private void loadMessagesForSession(long sessionId) {
        if (sessionId > 0) {
            messages = repository.getMessagesForSession(sessionId);
        }
    }
    
    /**
     * Creates a default session if no session is currently active
     */
    private void createDefaultSession() {
        // This will be used if we don't have a session yet (first launch)
        ChatSession defaultSession = new ChatSession("New Chat", "openai");
        long sessionId = repository.insertSessionSync(defaultSession);
        defaultSession.setId(sessionId);
        
        currentSessionId = sessionId;
        currentSessionTitle.setValue(defaultSession.getTitle());
        
        // Add a welcome message
        ChatMessage welcomeMessage = new ChatMessage(
            "Hello! How can I assist you today?", 
            false, 
            sessionId
        );
        repository.insertMessageSync(welcomeMessage);
        
        // Inform the shared view model
        sharedViewModel.selectSession(defaultSession);
        
        // Make sure we're observing the right messages
        loadMessagesForSession(sessionId);
    }
    
    /**
     * Creates a new chat session and makes it the current session
     */
    public void createNewSession() {
        ChatSession newSession = new ChatSession("New Chat", "openai");
        long sessionId = repository.insertSessionSync(newSession);
        newSession.setId(sessionId);
        
        currentSessionId = sessionId;
        currentSessionTitle.setValue(newSession.getTitle());
        
        // Add a welcome message
        ChatMessage welcomeMessage = new ChatMessage(
            "Hello! How can I assist you today?", 
            false, 
            sessionId
        );
        repository.insertMessageSync(welcomeMessage);
        
        // Inform the shared view model
        sharedViewModel.selectSession(newSession);
        
        // Make sure we're observing the right messages
        loadMessagesForSession(sessionId);
    }
    
    /**
     * Ensures we have a valid session before sending messages
     */
    private void ensureValidSession() {
        if (currentSessionId <= 0) {
            createDefaultSession();
        }
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }
    
    public LiveData<String> getCurrentSessionTitle() {
        return currentSessionTitle;
    }

    public void sendMessage(String message) {
        // Make sure we have a valid session
        ensureValidSession();

        // Add user message
        ChatMessage userMessage = new ChatMessage(message, true, currentSessionId);
        repository.insertMessage(userMessage);

        // Add bot response (echo for now until API integration)
        ChatMessage botMessage = new ChatMessage("Echo: " + message, false, currentSessionId);
        repository.insertMessage(botMessage);
    }

    public long getCurrentSessionId() {
        return currentSessionId;
    }
} 