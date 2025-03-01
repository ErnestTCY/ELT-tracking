package com.example.grpasg;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<String> reminders;

    public ReminderAdapter(List<String> reminders) {
        this.reminders = reminders;
    }


    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        holder.text.setText(reminders.get(position)); // Use the updated TextView ID
    }


    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        private TextView text;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.reminder_text); // Match this with the ID in reminder_item.xml
        }

        public void bind(String reminder) {
            text.setText(reminder); // Bind the reminder text
        }
    }
}


