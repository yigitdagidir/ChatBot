package com.bye_bye.cmp2204;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private final MutableLiveData<List<ChatMessage>> messages;
    private long currentSessionId;

    public ChatViewModel(Application application) {
        super(application);
        messages = new MutableLiveData<>(new ArrayList<>());
        currentSessionId = System.currentTimeMillis();
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public void sendMessage(String message) {
        List<ChatMessage> currentMessages = messages.getValue();
        if (currentMessages == null) {
            currentMessages = new ArrayList<>();
        }

        // Add user message
        ChatMessage userMessage = new ChatMessage(message, true, currentSessionId);
        currentMessages.add(userMessage);
        messages.setValue(currentMessages);

        // TODO: Send message to AI service and handle response
        // For now, just echo the message
        ChatMessage botMessage = new ChatMessage("Echo: " + message, false, currentSessionId);
        currentMessages.add(botMessage);
        messages.setValue(currentMessages);
    }

    public void setCurrentSessionId(long sessionId) {
        this.currentSessionId = sessionId;
        // TODO: Load messages for the new session
        messages.setValue(new ArrayList<>());
    }

    public long getCurrentSessionId() {
        return currentSessionId;
    }
} 