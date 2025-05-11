package com.bye_bye.cmp2204;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Typeface;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    public MessageAdapter() {
        super(new DiffUtil.ItemCallback<ChatMessage>() {
            @Override
            public boolean areItemsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                return oldItem.getMessage().equals(newItem.getMessage());
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = getItem(position);
        return message.isFromUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_bot, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = getItem(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).bind(message);
        }
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
        }
    }

    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;

        BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }

        void bind(ChatMessage message) {
            messageText.setText(parseMarkdown(message.getMessage()));
        }
        
        private Spannable parseMarkdown(String markdown) {
            // First, handle bullet points for lines starting with *
            StringBuilder processedText = new StringBuilder();
            String[] lines = markdown.split("\n");
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                
                // Track indentation level for nested bullets
                int indentLevel = 0;
                while (line.startsWith("  ")) {
                    line = line.substring(2);
                    indentLevel++;
                }
                
                // Check if line starts with bullet point pattern
                if (line.matches("^\\s*\\*\\s+.*")) {
                    // Replace the "* " with "• " to convert to bullet point
                    line = line.replaceFirst("\\*\\s+", "• ");
                    
                    // Add indent based on nesting level
                    for (int j = 0; j < indentLevel; j++) {
                        processedText.append("  ");
                    }
                    
                    processedText.append(line);
                } else {
                    processedText.append(lines[i]);
                }
                
                // Add newline except for the last line
                if (i < lines.length - 1) {
                    processedText.append("\n");
                }
            }
            
            // Now handle the normal formatting (bold, italic)
            SpannableString spannableString = new SpannableString(processedText.toString());
            
            // Parse bold formatting (double asterisks)
            Pattern boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
            Matcher boldMatcher = boldPattern.matcher(spannableString.toString());
            
            // Track offset to handle multiple replacements
            int offset = 0;
            
            while (boldMatcher.find()) {
                int start = boldMatcher.start() - offset;
                int end = boldMatcher.end() - offset;
                String boldText = boldMatcher.group(1);
                
                // Remove the asterisks and apply bold style
                spannableString = new SpannableString(
                    spannableString.toString().replace(boldMatcher.group(0), boldText)
                );
                
                // Apply bold style
                spannableString.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    start,
                    start + boldText.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                
                // Adjust offset for removed asterisks (4 characters: **_**)
                offset += 4;
            }
            
            // Parse single asterisk for italic, but only for inline formatting,
            // not for bullet points at the beginning of lines
            Pattern italicPattern = Pattern.compile("\\*([^\\s*][^*]*?[^\\s*])\\*");
            Matcher italicMatcher = italicPattern.matcher(spannableString.toString());
            
            // Reset offset for italics
            offset = 0;
            
            while (italicMatcher.find()) {
                int start = italicMatcher.start() - offset;
                String italicText = italicMatcher.group(1);
                
                // Remove the asterisks and apply italic style
                spannableString = new SpannableString(
                    spannableString.toString().replace(italicMatcher.group(0), italicText)
                );
                
                // Apply italic style
                spannableString.setSpan(
                    new StyleSpan(Typeface.ITALIC),
                    start,
                    start + italicText.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                
                // Adjust offset for removed asterisks (2 characters: *_*)
                offset += 2;
            }
            
            return spannableString;
        }
    }
} 