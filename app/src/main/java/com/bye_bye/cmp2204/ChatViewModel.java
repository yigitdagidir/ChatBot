package com.bye_bye.cmp2204;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repo;
    private final MediatorLiveData<List<ChatMessage>> messages = new MediatorLiveData<>();
    private LiveData<List<ChatMessage>> roomSource;

    private final MutableLiveData<String> sessionTitle = new MutableLiveData<>("New Chat");
    private long currentSessionId = -1;

    /* ----- shared selection comes from here ----- */
    private final SharedViewModel shared;

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
        repo.insertMessage(new ChatMessage("Echo: " + text, false, currentSessionId));
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
    }

    private void ensureSession() {
        if (currentSessionId <= 0) createNewSession();
    }
}
