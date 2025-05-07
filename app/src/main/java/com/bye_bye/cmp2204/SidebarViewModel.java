package com.bye_bye.cmp2204;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;

public class SidebarViewModel extends AndroidViewModel {
    private final MutableLiveData<List<ChatSession>> sessions;
    private final MutableLiveData<ChatSession> currentSession;

    public SidebarViewModel(Application application) {
        super(application);
        sessions = new MutableLiveData<>(new ArrayList<>());
        currentSession = new MutableLiveData<>();
    }

    public LiveData<List<ChatSession>> getSessions() {
        return sessions;
    }

    public LiveData<ChatSession> getCurrentSession() {
        return currentSession;
    }

    public void createNewSession() {
        List<ChatSession> currentSessions = sessions.getValue();
        if (currentSessions == null) {
            currentSessions = new ArrayList<>();
        }

        // Create a new session with default title and model
        ChatSession newSession = new ChatSession("New Chat", "openai");
        currentSessions.add(newSession);
        sessions.setValue(currentSessions);
        currentSession.setValue(newSession);
    }

    public void setCurrentSession(ChatSession session) {
        currentSession.setValue(session);
    }

    public void updateSessionTitle(long sessionId, String newTitle) {
        List<ChatSession> currentSessions = sessions.getValue();
        if (currentSessions != null) {
            for (ChatSession session : currentSessions) {
                if (session.getId() == sessionId) {
                    session.setTitle(newTitle);
                    break;
                }
            }
            sessions.setValue(currentSessions);
        }
    }

    public void deleteSession(ChatSession session) {
        List<ChatSession> currentSessions = sessions.getValue();
        if (currentSessions != null) {
            currentSessions.remove(session);
            sessions.setValue(currentSessions);
            if (currentSession.getValue() != null && currentSession.getValue().getId() == session.getId()) {
                currentSession.setValue(null);
            }
        }
    }
} 