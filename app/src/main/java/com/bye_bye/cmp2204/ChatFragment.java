package com.bye_bye.cmp2204;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import java.util.ArrayList;

public class ChatFragment extends Fragment {
    private ChatViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private MaterialButton micButton;
    private MaterialButton closeButton;
    private MaterialButton backButton;
    private MaterialCardView quickActionMenu;
    private MessageAdapter messageAdapter;
    private MaterialToolbar toolbar;
    private DrawerLayout drawerLayout;
    
    private Observer<List<ChatMessage>> messagesObserver = new Observer<List<ChatMessage>>() {
        @Override
        public void onChanged(List<ChatMessage> messages) {
            if (messages != null) {
                messageAdapter.submitList(new ArrayList<>(messages)); // Create a new list to force refresh
                if (messages.size() > 0) {
                    messagesRecyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        setHasOptionsMenu(true); // Enable options menu for this fragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.chat_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // Navigate to settings fragment
            Navigation.findNavController(requireView()).navigate(R.id.settingsFragment);
            return true;
        } else if (id == R.id.action_new_chat) {
            // Create a new chat session
            viewModel.createNewSession();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        micButton = view.findViewById(R.id.micButton);
        toolbar = view.findViewById(R.id.chatToolbar);
        quickActionMenu = view.findViewById(R.id.quickActionMenu);
        closeButton = view.findViewById(R.id.closeButton);
        backButton = view.findViewById(R.id.backButton);
        
        // Setup drawer toggle
        drawerLayout = requireActivity().findViewById(R.id.main);
        setupToolbar();

        // Setup RecyclerView
        messageAdapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); // Stack from bottom
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Setup message input
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && 
                 event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMessage();
                return true;
            }
            return false;
        });
        
        // Setup send button in the input area
        micButton.setOnClickListener(v -> {
            sendMessage();
        });
        
        // Setup quick action menu buttons
        closeButton.setOnClickListener(v -> {
            quickActionMenu.setVisibility(View.GONE);
        });
        
        backButton.setOnClickListener(v -> {
            drawerLayout.open();
        });

        // Observe messages - using a persistent observer to handle LiveData updates
        viewModel.getMessages().observe(getViewLifecycleOwner(), messagesObserver);
        
        // Observe session title
        viewModel.getCurrentSessionTitle().observe(getViewLifecycleOwner(), title -> {
            toolbar.setTitle(title);
        });
    }
    
    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            requireActivity(), drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        // Use the drawer icon to open the sidebar
        toolbar.setNavigationOnClickListener(v -> {
            drawerLayout.open();
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            try {
                viewModel.sendMessage(message);
                messageInput.setText("");
            } catch (Exception e) {
                e.printStackTrace();
                // Handle exception gracefully - maybe show a toast
            }
        }
    }
}