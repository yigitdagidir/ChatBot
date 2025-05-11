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

    private final AppDatabase db;
    private final ChatMessageDao messageDao;
    private final ChatSessionDao sessionDao;
    private final ExecutorService executor;
    
    // LiveData for sessions and messages
    private final LiveData<List<ChatSession>> allSessions;
    
    public ChatRepository(Application application)
    {
        db = AppDatabase.getDatabase(application);
        messageDao = db.messageDao();
        sessionDao = db.sessionDao();
        executor = Executors.newSingleThreadExecutor();
        allSessions = sessionDao.getAllSessions();
    }

    
    public LiveData<List<ChatSession>> getAllSessions() {
        return allSessions;
    }

    /**
     * Gets a session by ID synchronously (returns null if not found)
     */
    public ChatSession getSessionByIdSync(long id) {
        return sessionDao.getSessionByIdSync(id);
    }
    
    /**
     * Inserts a session asynchronously
     */
    public void insertSession(ChatSession session)
    {
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

    /**
     * Deletes all sessions and messages synchronously in a transaction
     * For use in cases where we need to ensure deletion is complete before continuing
     */
    public void wipeDatabaseNow() {
        try {
            db.runInTransaction(() -> {
                try {
                    db.messageDao().deleteAll();
                    db.sessionDao().deleteAll();
                } catch (Exception e) {
                    android.util.Log.e("ChatRepository", "Error during database wipe", e);
                }
            });
            android.util.Log.d("ChatRepository", "Database wiped successfully");
        } catch (Exception e) {
            android.util.Log.e("ChatRepository", "Transaction failed during database wipe", e);
            try {
                db.messageDao().deleteAll();
                android.util.Log.d("ChatRepository", "Messages deleted in fallback");
            } catch (Exception e1) {
                android.util.Log.e("ChatRepository", "Failed to delete messages in fallback", e1);
            }
            
            try {
                db.sessionDao().deleteAll();
                android.util.Log.d("ChatRepository", "Sessions deleted in fallback");
            } catch (Exception e2) {
                android.util.Log.e("ChatRepository", "Failed to delete sessions in fallback", e2);
            }
        }
    }
    
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
            try {
                sessionDao.updateLastMessageTime(message.getSessionId(), message.getTimestamp());
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    ChatSession existingSession = sessionDao.getSessionByIdSync(message.getSessionId());
                    if (existingSession == null) {
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
        try {
            sessionDao.updateLastMessageTime(message.getSessionId(), message.getTimestamp());
        } catch (Exception e) {
            e.printStackTrace();
            try {
                ChatSession existingSession = sessionDao.getSessionByIdSync(message.getSessionId());
                if (existingSession == null) {
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

}

