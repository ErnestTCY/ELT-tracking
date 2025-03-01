package com.example.grpasg;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryEntry> historyEntries;

    public HistoryAdapter(List<HistoryEntry> historyEntries) {
        this.historyEntries = historyEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntry entry = historyEntries.get(position);

        holder.dateTextView.setText("Date: " + entry.getTimestamp());
        holder.timeTextView.setText("Time: " + entry.getElapsedTime());
        holder.distanceTextView.setText(String.format(Locale.getDefault(), "KM: %.2f", entry.getDistance()));
        holder.speedTextView.setText(String.format(Locale.getDefault(), "Speed: %.2f km/h",
                entry.getDistance() / (entry.getElapsedTimeInMillis() / 3600000.0)));

        // Set the appropriate icon based on activity type
        String activityType = entry.getActivityType();
        if ("running".equalsIgnoreCase(activityType)) {
            holder.mapImageView.setImageResource(R.drawable.runicon); // Replace with your running icon
        } else if ("cycling".equalsIgnoreCase(activityType)) {
            holder.mapImageView.setImageResource(R.drawable.cycleicon); // Replace with your cycling icon
        } else {
            holder.mapImageView.setImageResource(R.drawable.map_placeholder); // Fallback for invalid types
        }

        holder.itemView.setOnClickListener(v -> {
            // Optional: handle item click
            Toast.makeText(v.getContext(), "Selected: " + activityType, Toast.LENGTH_SHORT).show();
        });
    }



    @Override
    public int getItemCount() {
        return historyEntries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, timeTextView, distanceTextView, speedTextView;
        ImageView mapImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_text);
            timeTextView = itemView.findViewById(R.id.time_text);
            distanceTextView = itemView.findViewById(R.id.distance_text);
            speedTextView = itemView.findViewById(R.id.speed_text);
            mapImageView = itemView.findViewById(R.id.map_image); // Initialize correctly
        }
    }
}
