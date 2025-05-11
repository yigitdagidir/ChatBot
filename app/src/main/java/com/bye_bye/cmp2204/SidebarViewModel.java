package com.bye_bye.cmp2204;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.text.SimpleDateFormat;
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
                currentSession.setValue(null);
            }
        }
    };

    public SidebarViewModel(Application application) {
        super(application);
        repository = new ChatRepository(application);
        sessions = repository.getAllSessions();
        currentSession = new MutableLiveData<>();
        

        ViewModelStoreOwner viewModelStoreOwner = (ViewModelStoreOwner) application;
        sharedViewModel = new ViewModelProvider(viewModelStoreOwner).get(SharedViewModel.class);
        

        sharedViewModel.getSelectedSession().observeForever(sessionObserver);
        

        sharedViewModel.isSessionReset().observeForever(resetObserver);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();

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

        // Add welcome message
        repository.insertMessageSync(new ChatMessage(
                "Hello! How can I assist you today?", false, id));

        sharedViewModel.selectSession(s);
    }


    public void setCurrentSession(ChatSession session) {
        if (session != null) {
            currentSession.setValue(session);
            

            sharedViewModel.selectSession(session);
        } else {

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

}