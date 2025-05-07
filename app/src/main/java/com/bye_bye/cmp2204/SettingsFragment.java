package com.bye_bye.cmp2204;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends Fragment {
    private SettingsViewModel viewModel;
    private MaterialSwitch themeSwitch;
    private MaterialButton clearHistoryButton;

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

        themeSwitch = view.findViewById(R.id.themeSwitch);
        clearHistoryButton = view.findViewById(R.id.clearHistoryButton);

        // Setup theme switch
        viewModel.isDarkTheme().observe(getViewLifecycleOwner(), isDark -> {
            themeSwitch.setChecked(isDark);
        });

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setDarkTheme(isChecked);
        });

        // Setup clear history button
        clearHistoryButton.setOnClickListener(v -> showClearHistoryDialog());
    }

    private void showClearHistoryDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear Chat History")
            .setMessage("Are you sure you want to clear all chat history? This action cannot be undone.")
            .setPositiveButton("Clear", (dialog, which) -> {
                viewModel.clearChatHistory();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}