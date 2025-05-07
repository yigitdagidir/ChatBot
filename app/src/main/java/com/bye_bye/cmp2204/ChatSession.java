package com.bye_bye.cmp2204;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sessions")
public class ChatSession {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private long lastMessageTime;
    private String modelType; // "openai" or "gemini"

    public ChatSession(String title, String modelType) {
        this.title = title;
        this.lastMessageTime = System.currentTimeMillis();
        this.modelType = modelType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
} 