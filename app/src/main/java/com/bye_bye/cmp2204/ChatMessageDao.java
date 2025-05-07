package com.bye_bye.cmp2204;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Data Access Object for chat messages
 */
@Dao
public interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChatMessage message);
    
    @Delete
    void delete(ChatMessage message);
    
    @Query("DELETE FROM messages")
    void deleteAll();
    
    @Query("DELETE FROM messages WHERE session_id = :sessionId")
    void deleteAllFromSession(long sessionId);
    
    @Query("SELECT * FROM messages WHERE session_id = :sessionId ORDER BY timestamp ASC")
    LiveData<List<ChatMessage>> getMessagesForSession(long sessionId);
    
    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT 1")
    ChatMessage getLatestMessage();
} 