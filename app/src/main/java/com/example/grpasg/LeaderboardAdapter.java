package com.example.grpasg;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.grpasg.R;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    private List<LeaderboardEntry> entries;

    public LeaderboardAdapter(List<LeaderboardEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardEntry entry = entries.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void updateEntries(List<LeaderboardEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        private TextView rankText;
        private TextView usernameText;
        private TextView distanceText;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            rankText = itemView.findViewById(R.id.rankText);
            usernameText = itemView.findViewById(R.id.usernameText);
            distanceText = itemView.findViewById(R.id.distanceText);
        }

        public void bind(LeaderboardEntry entry) {
            // Format rank
            rankText.setText(String.valueOf(entry.getRank()));

            // Format userID to be cleaner
            String userId = entry.getUsername();
            // Extract characters 4-7 (abc1, xyz7, def4)
            String uniquePart = userId.substring(4, 8);
            String displayName = "Player " + uniquePart;
            usernameText.setText(displayName);

            // Format distance to 1 decimal place
            String distanceStr = String.format("%.1f km", entry.getDistance());
            distanceText.setText(distanceStr);

            // Special styling for top 3
            switch (entry.getRank()) {
                case 1:
                    rankText.setTextColor(itemView.getContext().getColor(R.color.gold));
                    break;
                case 2:
                    rankText.setTextColor(itemView.getContext().getColor(R.color.silver));
                    break;
                case 3:
                    rankText.setTextColor(itemView.getContext().getColor(R.color.bronze));
                    break;
                default:
                    rankText.setTextColor(itemView.getContext().getColor(R.color.text_primary));
            }
        }
    }
} 