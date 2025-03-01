package com.example.grpasg;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

import com.example.grpasg.R;

public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder> {
    private List<Challenge> challenges;

    public ChallengesAdapter(List<Challenge> challenges) {
        this.challenges = challenges;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge challenge = challenges.get(position);
        holder.bind(challenge);
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    public void updateChallenges(List<Challenge> newChallenges) {
        this.challenges = newChallenges;
        notifyDataSetChanged();
    }

    static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        private TextView titleView;
        private TextView descriptionView;
        private LinearProgressIndicator progressIndicator;
        private TextView progressText;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.challengeTitle);
            descriptionView = itemView.findViewById(R.id.challengeDescription);
            progressIndicator = itemView.findViewById(R.id.challengeProgress);
            progressText = itemView.findViewById(R.id.challengeProgressText);
        }

        public void bind(Challenge challenge) {
            titleView.setText(challenge.getTitle());
            descriptionView.setText(challenge.getDescription());
            
            // Add click listener to activate challenge
            itemView.setOnClickListener(v -> {
                if (challenge.getProgress() == 0) {
                    // Only allow activation if not started
                    ((ChallengesActivity) itemView.getContext())
                        .activateChallenge(challenge, challenge.getPeriod());
                }
            });

            int progressPercentage = (int) ((float) challenge.getProgress() / challenge.getGoal() * 100);
            progressIndicator.setProgress(progressPercentage);
            progressText.setText(challenge.getProgress() + "/" + challenge.getGoal());
        }
    }
} 