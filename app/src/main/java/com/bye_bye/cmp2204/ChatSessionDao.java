package com.bye_bye.cmp2204;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/**
 * Data Access Object for chat sessions
 */
@Dao
public interface ChatSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChatSession session);
    
    @Update
    void update(ChatSession session);
    
    @Delete
    void delete(ChatSession session);
    
    @Query("DELETE FROM sessions")
    void deleteAll();
    
    @Query("SELECT * FROM sessions ORDER BY last_message_time DESC")
    LiveData<List<ChatSession>> getAllSessions();
    
    @Query("SELECT * FROM sessions WHERE id = :id")
    LiveData<ChatSession> getSessionById(long id);
    
    @Query("UPDATE sessions SET last_message_time = :timestamp WHERE id = :sessionId")
    void updateLastMessageTime(long sessionId, long timestamp);
    
    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    ChatSession getSessionByIdSync(long id);

    @Query("SELECT COUNT(*) FROM sessions")
    int getSessionCount();

} 