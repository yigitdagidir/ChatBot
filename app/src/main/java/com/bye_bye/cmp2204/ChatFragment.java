package com.bye_bye.cmp2204;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

public class ChatFragment extends Fragment {
    private ChatViewModel viewModel;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private MaterialButton micButton;
    private MessageAdapter messageAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        micButton = view.findViewById(R.id.micButton);

        // Setup RecyclerView
        messageAdapter = new MessageAdapter();
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Setup message input
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });

        // Setup mic button
        micButton.setOnClickListener(v -> {
            // TODO: Implement speech-to-text functionality
        });

        // Observe messages
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            messageAdapter.submitList(messages);
            messagesRecyclerView.scrollToPosition(messages.size() - 1);
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            viewModel.sendMessage(message);
            messageInput.setText("");
        }
    }
}