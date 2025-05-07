package com.bye_bye.cmp2204;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

public class SidebarFragment extends Fragment {
    private SidebarViewModel viewModel;
    private RecyclerView sessionsRecyclerView;
    private MaterialButton newChatButton;
    private SessionAdapter sessionAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SidebarViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sidebar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionsRecyclerView = view.findViewById(R.id.sessionsRecyclerView);
        newChatButton = view.findViewById(R.id.newChatButton);

        // Setup RecyclerView
        sessionAdapter = new SessionAdapter(session -> {
            viewModel.setCurrentSession(session);
            Navigation.findNavController(requireView()).navigate(R.id.action_sidebarFragment_to_chatFragment);
        });
        sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        sessionsRecyclerView.setAdapter(sessionAdapter);

        // Setup new chat button
        newChatButton.setOnClickListener(v -> {
            viewModel.createNewSession();
            Navigation.findNavController(requireView()).navigate(R.id.action_sidebarFragment_to_chatFragment);
        });

        // Observe sessions
        viewModel.getSessions().observe(getViewLifecycleOwner(), sessions -> {
            sessionAdapter.submitList(sessions);
        });
    }
}