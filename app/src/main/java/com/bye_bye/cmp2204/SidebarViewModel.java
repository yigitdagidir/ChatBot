package com.bye_bye.cmp2204;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SidebarViewModel extends AndroidViewModel {
    private final ChatRepository repository;
    private final LiveData<List<ChatSession>> sessions;
    private final MutableLiveData<ChatSession> currentSession;
    private final SharedViewModel sharedViewModel;
    
    private final Observer<ChatSession> sessionObserver = new Observer<ChatSession>() {
        @Override
        public void onChanged(ChatSession session) {
            if (session != null) {
                currentSession.setValue(session);
            }
        }
    };
    
    private final Observer<Boolean> resetObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean reset) {
            if (reset != null && reset) {
                // Reset has occurred, clear current session and wait for new one
                currentSession.setValue(null);
            }
        }
    };

    public SidebarViewModel(Application application) {
        super(application);
        repository = new ChatRepository(application);
        sessions = repository.getAllSessions();
        currentSession = new MutableLiveData<>();
        
        // Get the SharedViewModel
        ViewModelStoreOwner viewModelStoreOwner = (ViewModelStoreOwner) application;
        sharedViewModel = new ViewModelProvider(viewModelStoreOwner).get(SharedViewModel.class);
        
        // Observe the shared view model's selected session
        sharedViewModel.getSelectedSession().observeForever(sessionObserver);
        
        // Observe reset events
        sharedViewModel.isSessionReset().observeForever(resetObserver);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Stop observing when ViewModel is cleared
        sharedViewModel.getSelectedSession().removeObserver(sessionObserver);
        sharedViewModel.isSessionReset().removeObserver(resetObserver);

    }

    public LiveData<List<ChatSession>> getSessions() {
        return sessions;
    }

    public LiveData<ChatSession> getCurrentSession() {
        return currentSession;
    }

    public void createNewSession() {
        String title = new SimpleDateFormat("'Chat •' MMM dd HH:mm",
                Locale.getDefault()).format(new Date());

        ChatSession s = new ChatSession(title, "openai");
        long id       = repository.insertSessionSync(s);
        s.setId(id);

        // welcome
        repository.insertMessageSync(new ChatMessage(
                "Hello! How can I assist you today?", false, id));

        sharedViewModel.selectSession(s);              // <-- broadcast
    }


    public void setCurrentSession(ChatSession session) {
        if (session != null) {
            currentSession.setValue(session);
            
            // Share the selected session with other components
            sharedViewModel.selectSession(session);
        } else {
            // Handle null session - select/create a default one
            createNewSession();
        }
    }

    public void updateSessionTitle(long sessionId, String newTitle) {
        if (sessionId > 0) {
            ChatSession session = new ChatSession(newTitle, "openai");
            session.setId(sessionId);
            repository.updateSession(session);
        }
    }

    public void deleteSession(ChatSession session) {
        if (session != null) {
            repository.deleteSession(session);
            
            if (currentSession.getValue() != null && 
                currentSession.getValue().getId() == session.getId()) {
                currentSession.setValue(null);
                
                // Create a new session if this was the last one
                createNewSession();
            }
        }
    }
} 