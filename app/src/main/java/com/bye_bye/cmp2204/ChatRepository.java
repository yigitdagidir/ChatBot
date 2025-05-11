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
    
    // Session operations
    
    public LiveData<List<ChatSession>> getAllSessions() {
        return allSessions;
    }
    
    public LiveData<ChatSession> getSessionById(long id) {
        return sessionDao.getSessionById(id);
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


    public int getSessionCount() {
        return db.sessionDao().getSessionCount();
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
            // Use a transaction to ensure atomicity
            db.runInTransaction(() -> {
                try {
                    db.messageDao().deleteAll();     // delete messages first
                    db.sessionDao().deleteAll();     // then sessions
                } catch (Exception e) {
                    // Log the error but don't rethrow to allow transaction to complete
                    android.util.Log.e("ChatRepository", "Error during database wipe", e);
                }
            });
            android.util.Log.d("ChatRepository", "Database wiped successfully");
        } catch (Exception e) {
            // Log the error if transaction fails
            android.util.Log.e("ChatRepository", "Transaction failed during database wipe", e);
            
            // Attempt individual deletes without transaction as fallback
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

