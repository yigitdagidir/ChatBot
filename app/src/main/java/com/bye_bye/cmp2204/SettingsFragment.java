package com.bye_bye.cmp2204;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class SettingsFragment extends Fragment {
    private SettingsViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private MaterialSwitch themeSwitch;
    private MaterialButton clearHistoryButton;
    private MaterialToolbar toolbar;
    private AutoCompleteTextView modelSelector;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
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
        modelSelector = view.findViewById(R.id.model_selector);

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

        // Setup model selection
        viewModel.getSelectedModel().observe(getViewLifecycleOwner(), model -> {
            // Only set text if different to prevent callback loop
            if (!model.equals(modelSelector.getText().toString())) {
                modelSelector.setText(model, false);
            }
        });

        modelSelector.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedModel = parent.getItemAtPosition(position).toString();
            changeModel(selectedModel);
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
                // Show a progress indicator
                Toast.makeText(requireContext(), 
                    "Clearing history...", 
                    Toast.LENGTH_SHORT).show();
                
                // Clear the chat history - this creates a new session
                ChatSession newSession = viewModel.clearChatHistory();
                
                if (newSession != null) {
                    // Show success message
                    Toast.makeText(requireContext(), 
                        R.string.history_cleared, 
                        Toast.LENGTH_SHORT).show();
                    
                    // Navigate to chat fragment with a short delay to ensure UI updates
                    requireView().postDelayed(() -> {
                        try {
                            if (isAdded() && !isRemoving()) {
                                // Ensure the session is selected
                                sharedViewModel.selectSession(newSession);
                                
                                // Navigate to chat fragment
                                Navigation.findNavController(requireView())
                                    .navigate(R.id.action_settingsFragment_to_chatFragment);
                                
                                // Log successful navigation
                                android.util.Log.d("SettingsFragment", 
                                    "Navigated to chat fragment for session: " + newSession.getId());
                            }
                        } catch (Exception e) {
                            android.util.Log.e("SettingsFragment", 
                                "Error navigating after history clear", e);
                        }
                    }, 200); // Short delay for UI to update
                } else {
                    // Show error message if session creation failed
                    Toast.makeText(requireContext(), 
                        "Failed to create new session after clearing history", 
                        Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void changeModel(String model) {
        // Change the model in the ViewModel
        viewModel.setSelectedModel(model);
        
        // Show confirmation toast
        Toast.makeText(requireContext(),
            getString(R.string.model_changed) + model,
            Toast.LENGTH_SHORT).show();
    }
}
