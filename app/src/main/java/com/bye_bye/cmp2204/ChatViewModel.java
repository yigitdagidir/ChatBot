package com.bye_bye.cmp2204;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import androidx.lifecycle.Observer;

import java.text.SimpleDateFormat;
import java.util.*;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repo;
    private final MediatorLiveData<List<ChatMessage>> messages = new MediatorLiveData<>();
    private LiveData<List<ChatMessage>> roomSource;

    private final MutableLiveData<String> sessionTitle = new MutableLiveData<>("New Chat");
    private long currentSessionId = -1;

    /* ----- shared selection comes from here ----- */
    private SharedViewModel shared;
    private final DataStoreManager dataStoreManager;

    private GenerativeModel _gm;
    private GenerativeModelFutures model;
    private ChatFutures chat;
    private boolean isInitializing = false; // Flag to prevent multiple initializations

    public ChatViewModel(@NonNull Application app) {
        super(app);
        repo = new ChatRepository(app);

        try {
            shared = new ViewModelProvider((ViewModelStoreOwner) app)
                    .get(SharedViewModel.class);

            // Register observers only if shared is successfully initialized
            shared.isSessionReset().observeForever(resetObs);
            shared.isModelChanged().observeForever(modelChangedObs);
        } catch (Exception e) {
            Log.e("ChatViewModel", "Error initializing SharedViewModel: " + e.getMessage());
            // Continue without shared view model functionality
        }

        dataStoreManager = new DataStoreManager(app);

        // Initialize empty messages list
        messages.setValue(new ArrayList<>());

        // Initialize model from DataStore
        initializeModel();

        // Set up message observer to rebuild chat context when messages change
        messages.observeForever(messageList -> {
            if (!isInitializing && messageList != null && !messageList.isEmpty()) {
                Log.d("ChatViewModel", "Messages changed, rebuilding chat context");
                rebuildChatContext();
            }
        });

        /* whenever the sidebar (or anyone) selects a session → swap the message source */
        shared.getSelectedSession().observeForever(this::switchToSession);

        // Don't automatically create a new session here
        // We'll create it when the user sends a message
    }

    private void initializeModel() {
        isInitializing = true;
        try {
            String selectedModel = dataStoreManager.getSelectedModel();
            Log.d("ChatViewModel", "Initializing model: " + selectedModel);
            _gm = new GenerativeModel(selectedModel, BuildConfig.apiKey);
            model = GenerativeModelFutures.from(_gm);
            chat = model.startChat();
        } finally {
            isInitializing = false;
        }
    }

    /* ---------------- public for UI ---------------- */

    public LiveData<List<ChatMessage>> getMessages()            { return messages; }
    public LiveData<String>            getCurrentSessionTitle() { return sessionTitle; }

    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty()) return;

        // Don't send messages during reset
        if (shared != null && shared.isResetInProgress()) {
            Log.w("ChatViewModel", "Attempted to send message during reset, ignoring");
            return;
        }

        // Create a new session if none exists
        if (currentSessionId <= 0) {
            Log.d("ChatViewModel", "No current session, creating new one before sending message");
            createNewSession();
        }

        // Double check that we have a valid session ID before sending
        if (currentSessionId <= 0) {
            Log.e("ChatViewModel", "Failed to create session. Message not sent.");
            return;
        }

        // Verify session exists in database before sending
        ChatSession session = repo.getSessionByIdSync(currentSessionId);
        if (session == null) {
            Log.d("ChatViewModel", "Session no longer exists, creating new session");
            createNewSession();

            if (currentSessionId <= 0) {
                Log.e("ChatViewModel", "Failed to create session after second attempt. Message not sent.");
                return;
            }
            

            session = repo.getSessionByIdSync(currentSessionId);
            if (session == null) {
                Log.e("ChatViewModel", "Session still doesn't exist after creation. Message not sent.");
                return;
            }
        }

        final long sessionId = currentSessionId;
        Log.d("ChatViewModel", "Sending message to session " + sessionId);
        repo.insertMessage(new ChatMessage(text, true, sessionId));
        Content.Builder msg_builder = new Content.Builder();
        msg_builder.addText(text);
        msg_builder.setRole("user");
        Content msg = msg_builder.build();

        // Send to AI
        ListenableFuture<GenerateContentResponse> response = getChat().sendMessage(msg);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (repo.getSessionByIdSync(sessionId) != null) {
                    repo.insertMessage(
                            new ChatMessage(result.getText(), false, sessionId)
                    );
                } else {
                    Log.w("ChatViewModel", "Session was deleted before AI response arrived");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (repo.getSessionByIdSync(sessionId) != null) {
                    repo.insertMessage(
                            new ChatMessage("Error: " + t.getMessage(), false, sessionId)
                    );
                } else {
                    Log.w("ChatViewModel", "Session was deleted before error could be inserted");
                }
            }
        }, getApplication().getMainExecutor());
    }

    /** Toolbar / FAB use this to start a fresh conversation */
    public void createNewSession() {
        String title = new SimpleDateFormat("'Chat •' MMM dd HH:mm",
                Locale.getDefault()).format(new Date());

        String currentModel = dataStoreManager.getSelectedModel();
        ChatSession s = new ChatSession(title, currentModel);
        long id = repo.insertSessionSync(s);
        s.setId(id);

        shared.selectSession(s);
        switchToSession(s);
    }

    /**
     * Creates a new session with welcome message.
     * Used when explicitly creating a chat from UI or after reset.
     */
    public void createNewSessionWithWelcome() {
        String title = new SimpleDateFormat("'Chat •' MMM dd HH:mm",
                Locale.getDefault()).format(new Date());

        ChatSession s = new ChatSession(title, "openai");
        long id       = repo.insertSessionSync(s);
        s.setId(id);
        repo.insertMessageSync(new ChatMessage(
                "Hello! How can I assist you today?", false, id));

        if (shared != null) {
            shared.selectSession(s);
        }
        switchToSession(s);
    }

    /** Called automatically whenever SharedViewModel changes */
    public void switchToSession(ChatSession s) {
        if (s == null) {
            Log.d("ChatViewModel", "Received null session, waiting for reset to complete");
            return;
        }

        Log.d("ChatViewModel", "Switching to session: " + s.getTitle());
        currentSessionId = s.getId();
        sessionTitle.setValue(s.getTitle());

        if (roomSource != null) {
            messages.removeSource(roomSource);
        }
        roomSource = repo.getMessagesForSession(currentSessionId);
        messages.addSource(roomSource, newMessages -> {
            if (newMessages != null) {
                Log.d("ChatViewModel", "New messages loaded: " + newMessages.size());
                messages.setValue(newMessages);
            }
        });
    }

    /**
     * Rebuilds the chat context with all current messages.
     * Should be called when messages change or when switching sessions.
     */
    private void rebuildChatContext() {
        if (isInitializing) return;

        List<ChatMessage> messageList = messages.getValue();
        if (messageList == null || messageList.isEmpty()) {
            Log.d("ChatViewModel", "No messages to build context, using empty chat");
            chat = model.startChat();
            return;
        }

        List<Content> history = new ArrayList<>();
        for (ChatMessage m : messageList) {
            Content.Builder msg_builder = new Content.Builder();
            msg_builder.addText(m.getMessage());
            msg_builder.setRole(m.isFromUser() ? "user" : "model");
            history.add(msg_builder.build());
        }

        Log.d("ChatViewModel", "Rebuilding chat context with " + history.size() + " messages");
        chat = model.startChat(history);
    }

    private final Observer<Boolean> resetObs = reset -> {
        if (Boolean.TRUE.equals(reset)) {
            if (shared.getSelectedSession().getValue() == null) {
                createNewSessionWithWelcome();
            }
        }
    };

    private final Observer<Boolean> modelChangedObs = changed -> {
        if (Boolean.TRUE.equals(changed)) {
            Log.d("ChatViewModel", "Model changed, reinitializing");
            initializeModel();

            rebuildChatContext();
            if (shared != null) {
                shared.setModelChanged(false);
            }
        }
    };

    public ChatFutures getChat() {
        return chat;
    }

    public void setChat(ChatFutures chat) {
        this.chat = chat;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (shared != null) {
            shared.isSessionReset().removeObserver(resetObs);
            shared.isModelChanged().removeObserver(modelChangedObs);
            shared.getSelectedSession().removeObserver(this::switchToSession);
        }

        messages.removeObserver(messageList -> {
            if (!isInitializing && messageList != null && !messageList.isEmpty()) {
                rebuildChatContext();
            }
        });
    }
}
