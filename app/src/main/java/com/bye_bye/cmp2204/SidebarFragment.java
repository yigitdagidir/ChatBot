package com.bye_bye.cmp2204;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
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

    private SidebarViewModel   viewModel;
    private SharedViewModel    sharedViewModel;
    private DrawerLayout       drawerLayout;
    private SessionAdapter     adapter;

    /* ---------------- Android lifecycle ---------------- */

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        viewModel         = new ViewModelProvider(this).get(SidebarViewModel.class);
        sharedViewModel   = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle state) {
        return inf.inflate(R.layout.fragment_sidebar, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        /* --------- DrawerLayout (null-safe) ---------- */
        drawerLayout = requireActivity().findViewById(R.id.main);
        if (drawerLayout == null) {                       // <-- real device edge-case
            drawerLayout = findDrawerInHierarchy(v);
        }

        /* -------- Recycler of sessions -------- */
        RecyclerView list = v.findViewById(R.id.sessionsRecyclerView);
        adapter = new SessionAdapter(session -> {
            sharedViewModel.selectSession(session);
            navigate(R.id.chatFragment);
        });
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        /* -------- Buttons -------- */
        MaterialButton newChat = v.findViewById(R.id.newChatButton);
        newChat.setOnClickListener(b -> {
            viewModel.createNewSession();
            navigate(R.id.chatFragment);
        });

        MaterialButton settings = v.findViewById(R.id.settingsButton);
        settings.setOnClickListener(b -> navigate(R.id.settingsFragment));

        /* -------- LiveData -------- */
        viewModel.getSessions().observe(getViewLifecycleOwner(),
                sessions -> adapter.submitList(sessions));
    }

    /* ---------------- helpers ---------------- */

    /** Close the drawer (if we actually have it) and go to destination */
    private void navigate(int destId) {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        NavController nav = Navigation.findNavController(requireActivity(),
                R.id.nav_host_fragment);
        nav.navigate(destId, null,
                new NavOptions.Builder().setLaunchSingleTop(true).build());
    }

    /** Walk up the view tree until we hit a DrawerLayout. */
    private DrawerLayout findDrawerInHierarchy(View v) {
        View p = v;
        while (p != null && !(p instanceof DrawerLayout)) {
            p = (View) p.getParent();
        }
        return (DrawerLayout) p;
    }
}
