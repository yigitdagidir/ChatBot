package com.bye_bye.cmp2204;

import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private ChatViewModel     chatVm;
    private SharedViewModel   shared;
    private MessageAdapter    adapter;
    private EditText          messageInput;
    private DrawerLayout      drawer;

    @Override public void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        chatVm  = new ViewModelProvider(this).get(ChatViewModel.class);
        shared  = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_chat, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        /* ---------- UI setup ---------- */
        RecyclerView list = v.findViewById(R.id.messagesRecyclerView);
        adapter      = new MessageAdapter();
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        messageInput = v.findViewById(R.id.messageInput);
        MaterialButton sendBtn = v.findViewById(R.id.micButton);
        sendBtn.setOnClickListener(b -> send());

        MaterialToolbar bar = v.findViewById(R.id.chatToolbar);
        drawer = requireActivity().findViewById(R.id.main);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(bar);
        bar.setNavigationOnClickListener(bt -> drawer.open());

        /* ---------- LiveData ---------- */
        chatVm.getMessages().observe(getViewLifecycleOwner(),
                listData -> adapter.submitList(new ArrayList<>(listData)));

        chatVm.getCurrentSessionTitle().observe(getViewLifecycleOwner(), bar::setTitle);

        /* react to sidebar selection */
        shared.getSelectedSession().observe(getViewLifecycleOwner(), chatVm::switchToSession);
    }

    private void send() {
        String txt = messageInput.getText().toString().trim();
        if (!txt.isEmpty()) {
            chatVm.sendMessage(txt);
            messageInput.setText("");
        }
    }

    /* toolbar menu (new chat) */
    @Override public void onCreateOptionsMenu(@NonNull Menu m,@NonNull MenuInflater inf){
        inf.inflate(R.menu.chat_menu, m);
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem i){
        if(i.getItemId()==R.id.action_new_chat){ chatVm.createNewSession(); return true; }
        if(i.getItemId()==R.id.action_settings){
            Navigation.findNavController(requireView()).navigate(R.id.settingsFragment); return true;
        }
        return super.onOptionsItemSelected(i);
    }
}
