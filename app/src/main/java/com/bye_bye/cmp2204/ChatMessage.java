package com.bye_bye.cmp2204;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String message;
    private boolean isFromUser;
    private long timestamp;
    private long sessionId;

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