package com.bye_bye.cmp2204;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionAdapter extends ListAdapter<ChatSession, SessionAdapter.SessionViewHolder> {
    private final OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(ChatSession session);
    }

    public SessionAdapter(OnSessionClickListener listener) {
        super(new DiffUtil.ItemCallback<ChatSession>() {
            @Override
            public boolean areItemsTheSame(@NonNull ChatSession oldItem, @NonNull ChatSession newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull ChatSession oldItem, @NonNull ChatSession newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                       oldItem.getLastMessageTime() == newItem.getLastMessageTime();
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ChatSession session = getItem(position);
        holder.bind(session, listener);
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView timeText;
        private final SimpleDateFormat dateFormat;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(android.R.id.text1);
            timeText = itemView.findViewById(android.R.id.text2);
            dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        }

        void bind(ChatSession session, OnSessionClickListener listener) {
            titleText.setText(session.getTitle());
            timeText.setText(dateFormat.format(new Date(session.getLastMessageTime())));
            itemView.setOnClickListener(v -> listener.onSessionClick(session));
        }
    }
} 