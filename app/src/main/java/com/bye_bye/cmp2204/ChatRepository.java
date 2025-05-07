package com.bye_bye.cmp2204;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for chat data, providing a clean API to the rest of the app
 */
public class ChatRepository {
    private final ChatMessageDao messageDao;
    private final ChatSessionDao sessionDao;
    private final ExecutorService executor;
    
    // LiveData for sessions and messages
    private final LiveData<List<ChatSession>> allSessions;
    
    public ChatRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        messageDao = db.messageDao();
        sessionDao = db.sessionDao();
        executor = Executors.newSingleThreadExecutor();
        
        allSessions = sessionDao.getAllSessions();
    }
    
    // Session operations
    
    public LiveData<List<ChatSession>> getAllSessions() {
        return allSessions;
    }
    
    public LiveData<ChatSession> getSessionById(long id) {
        return sessionDao.getSessionById(id);
    }
    
    /**
     * Inserts a session asynchronously
     */
    public void insertSession(ChatSession session) {
        executor.execute(() -> {
            long id = sessionDao.insert(session);
            if (session.getId() == 0) {
                session.setId(id);
            }
        });
    }
    
    /**
     * Inserts a session synchronously, returns the ID
     * Only use this for initialization
     */
    public long insertSessionSync(ChatSession session) {
        long id = sessionDao.insert(session);
        if (session.getId() == 0) {
            session.setId(id);
        }
        return id;
    }
    
    public void updateSession(ChatSession session) {
        executor.execute(() -> sessionDao.update(session));
    }
    
    public void deleteSession(ChatSession session) {
        executor.execute(() -> {
            sessionDao.delete(session);
            messageDao.deleteAllFromSession(session.getId());
        });
    }
    
    public void deleteAllSessions() {
        executor.execute(() -> {
            sessionDao.deleteAll();
            messageDao.deleteAll();
        });
    }
    
    /**
     * Deletes all sessions and messages synchronously 
     * For use in cases where we need to ensure deletion is complete before continuing
     */
    public void deleteAllSessionsSync() {
        try {
            sessionDao.deleteAll();
            messageDao.deleteAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Message operations
    
    public LiveData<List<ChatMessage>> getMessagesForSession(long sessionId) {
        return messageDao.getMessagesForSession(sessionId);
    }
    
    /**
     * Inserts a message asynchronously
     */
    public void insertMessage(ChatMessage message) {
        executor.execute(() -> {
            long id = messageDao.insert(message);
            if (message.getId() == 0) {
                message.setId(id);
            }
            
            // Use the dedicated method to update just the timestamp
            try {
                sessionDao.updateLastMessageTime(message.getSessionId(), message.getTimestamp());
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to creating a session if needed
                try {
                    ChatSession existingSession = sessionDao.getSessionByIdSync(message.getSessionId());
                    if (existingSession == null) {
                        // Session doesn't exist, create it
                        ChatSession newSession = new ChatSession("New Chat", "openai");
                        newSession.setId(message.getSessionId());
                        newSession.setLastMessageTime(message.getTimestamp());
                        sessionDao.insert(newSession);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Inserts a message synchronously, returns the ID
     * Only use this for initialization
     */
    public long insertMessageSync(ChatMessage message) {
        long id = messageDao.insert(message);
        if (message.getId() == 0) {
            message.setId(id);
        }
        
        // Update the session timestamp safely
        try {
            sessionDao.updateLastMessageTime(message.getSessionId(), message.getTimestamp());
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback in case session doesn't exist
            try {
                ChatSession existingSession = sessionDao.getSessionByIdSync(message.getSessionId());
                if (existingSession == null) {
                    // Create session if needed
                    ChatSession newSession = new ChatSession("New Chat", "openai");
                    newSession.setId(message.getSessionId());
                    newSession.setLastMessageTime(message.getTimestamp());
                    sessionDao.insert(newSession);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        
        return id;
    }
    
    public void deleteAllMessagesForSession(long sessionId) {
        executor.execute(() -> messageDao.deleteAllFromSession(sessionId));
    }
} 