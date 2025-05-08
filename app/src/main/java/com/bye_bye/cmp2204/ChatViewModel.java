package com.bye_bye.cmp2204;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.*;
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
    private final SharedViewModel shared;

    private final GenerativeModel _gm = new GenerativeModel(
            "gemini-2.0-flash",
            BuildConfig.apiKey
    );
    private final GenerativeModelFutures model = GenerativeModelFutures.from(_gm);

    private ChatFutures chat = model.startChat();

    public ChatViewModel(@NonNull Application app) {
        super(app);
        repo   = new ChatRepository(app);
        shared = new ViewModelProvider((ViewModelStoreOwner) app)
                .get(SharedViewModel.class);

        messages.setValue(new ArrayList<>());

        /* whenever the sidebar (or anyone) selects a session → swap the message source */
        shared.getSelectedSession().observeForever(this::switchToSession);

        /* first run: create an initial session if none exists */
        if (shared.getSelectedSession().getValue() == null) {
            createNewSession();                 // this will broadcast itself via shared
        }
    }

    /* ---------------- public for UI ---------------- */

    public LiveData<List<ChatMessage>> getMessages()            { return messages; }
    public LiveData<String>            getCurrentSessionTitle() { return sessionTitle; }

    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty()) return;
        ensureSession();
        repo.insertMessage(new ChatMessage(text, true, currentSessionId));

        Content.Builder msg_builder = new Content.Builder();
        msg_builder.addText(text);
        msg_builder.setRole("user");
        Content msg = msg_builder.build();

        ListenableFuture<GenerateContentResponse> response = getChat().sendMessage(msg);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                repo.insertMessage(
                        new ChatMessage(result.getText(), false, currentSessionId)
                );
            }

            @Override
            public void onFailure(Throwable t) {
                repo.insertMessage(
                        new ChatMessage("Error: " + t.getMessage(), false, currentSessionId)
                );
            }
        }, getApplication().getMainExecutor());
    }

    /** Toolbar / FAB use this to start a fresh conversation */
    public void createNewSession() {
        String title = new SimpleDateFormat("'Chat •' MMM dd HH:mm",
                Locale.getDefault()).format(new Date());

        ChatSession s = new ChatSession(title, "openai");
        long id       = repo.insertSessionSync(s);
        s.setId(id);

        repo.insertMessageSync(new ChatMessage(
                "Hello! How can I assist you today?", false, id));

        shared.selectSession(s);   // <- notifies sidebar and this ViewModel (through observer)
    }

    /* ---------------- internals ---------------- */

    /** Called automatically whenever SharedViewModel changes */
    public void switchToSession(ChatSession s) {
        if (s == null) return;
        currentSessionId = s.getId();
        sessionTitle.setValue(s.getTitle());

        if (roomSource != null) messages.removeSource(roomSource);
        roomSource = repo.getMessagesForSession(currentSessionId);
        messages.addSource(roomSource, messages::setValue);

        List<Content> history = new ArrayList<>();

        for (ChatMessage m : Objects.requireNonNull(messages.getValue())) {
            Content.Builder msg_builder = new Content.Builder();
            msg_builder.addText(m.getMessage());
            msg_builder.setRole(m.isFromUser() ? "user" : "model");
            history.add(msg_builder.build());
        }
        Log.d("ChatViewModel", "History size: " + history.size());
        setChat(model.startChat(history));
    }

    private void ensureSession() {
        if (currentSessionId <= 0) createNewSession();
    }

    public ChatFutures getChat() {
        return chat;
    }

    public void setChat(ChatFutures chat) {
        this.chat = chat;
    }
}
