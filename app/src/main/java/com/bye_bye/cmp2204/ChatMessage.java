package com.bye_bye.cmp2204;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "messages")
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;
    
    @ColumnInfo(name = "message")
    private String message;
    
    @ColumnInfo(name = "is_from_user")
    private boolean isFromUser;
    
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    
    @ColumnInfo(name = "session_id")
    private long sessionId;

    // Constructor used by Room
    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
        this.message = "";
        this.isFromUser = false;
        this.sessionId = 0;
    }
    
    // Constructor used by application code
    @Ignore
    public ChatMessage(String message, boolean isFromUser, long sessionId) {
        this.message = message;
        this.isFromUser = isFromUser;
        this.timestamp = System.currentTimeMillis();
        this.sessionId = sessionId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFromUser() {
        return isFromUser;
    }

    public void setFromUser(boolean fromUser) {
        isFromUser = fromUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }
} 