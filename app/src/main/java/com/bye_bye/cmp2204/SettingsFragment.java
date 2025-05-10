package com.bye_bye.cmp2204;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends Fragment {
    private SettingsViewModel viewModel;
    private MaterialSwitch themeSwitch;
    private MaterialButton clearHistoryButton;
    private MaterialToolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.settingsToolbar);
        themeSwitch = view.findViewById(R.id.themeSwitch);
        clearHistoryButton = view.findViewById(R.id.clearHistoryButton);

        // Setup toolbar
        setupToolbar();

        // Setup theme switch
        viewModel.isDarkTheme().observe(getViewLifecycleOwner(), isDark -> {
            themeSwitch.setChecked(isDark);
        });

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setDarkTheme(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });


        // Setup clear history button
        clearHistoryButton.setOnClickListener(v -> showClearHistoryDialog());
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> {
            try {
                // Try to navigate back using Navigation component
                Navigation.findNavController(requireView()).navigateUp();
            } catch (Exception e) {
                // Fallback to fragment manager
                requireActivity().onBackPressed();
            }
        });
    }

    private void showClearHistoryDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_history)
            .setMessage(R.string.clear_history_message)
            .setPositiveButton(R.string.clear, (dialog, which) -> {
                // Clear the chat history
                viewModel.clearChatHistory();
                
                // Show confirmation toast
                Toast.makeText(requireContext(), 
                    R.string.history_cleared, 
                    Toast.LENGTH_SHORT).show();
                
                // Navigate back to chat fragment
                try {
                    Navigation.findNavController(requireView())
                        .navigate(R.id.action_settingsFragment_to_chatFragment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
}