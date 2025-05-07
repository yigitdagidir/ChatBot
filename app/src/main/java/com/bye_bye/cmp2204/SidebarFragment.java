package com.bye_bye.cmp2204;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

public class SidebarFragment extends Fragment {
    private SidebarViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private RecyclerView sessionsRecyclerView;
    private MaterialButton newChatButton;
    private MaterialButton settingsButton;
    private SessionAdapter sessionAdapter;
    private DrawerLayout drawerLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SidebarViewModel.class);
        
        // Get the shared view model from activity
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
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
        settingsButton = view.findViewById(R.id.settingsButton);
        drawerLayout = requireActivity().findViewById(R.id.main);

        // Setup RecyclerView
        sessionAdapter = new SessionAdapter(session -> {
            // Inform the shared view model about the selected session
            sharedViewModel.selectSession(session);
            closeDrawerAndShowChat();
        });
        sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        sessionsRecyclerView.setAdapter(sessionAdapter);

        // Setup new chat button
        newChatButton.setOnClickListener(v -> {
            viewModel.createNewSession();
            closeDrawerAndShowChat();
        });

        // Setup settings button
        settingsButton.setOnClickListener(v -> {
            closeDrawerAndNavigateTo(R.id.settingsFragment);
        });

        // Observe sessions
        viewModel.getSessions().observe(getViewLifecycleOwner(), sessions -> {
            sessionAdapter.submitList(sessions);
        });
    }
    
    private void closeDrawerAndShowChat() {
        closeDrawerAndNavigateTo(R.id.chatFragment);
    }
    
    private void closeDrawerAndNavigateTo(int destinationId) {
        // Close the drawer first
        drawerLayout.closeDrawers();
        
        try {
            // Try to navigate using the Navigation component
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(destinationId, null, new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .build());
        } catch (Exception e) {
            // Fallback if navigation fails
            e.printStackTrace();
            
            // Try to create the appropriate fragment
            Fragment fragment;
            if (destinationId == R.id.settingsFragment) {
                fragment = new SettingsFragment();
            } else {
                fragment = new ChatFragment();
            }
            
            try {
                // Try another approach using FragmentManager directly
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, fragment)
                        .commitAllowingStateLoss();
            } catch (Exception e2) {
                e2.printStackTrace();
                // If all else fails, log the error but don't crash
            }
        }
    }
}