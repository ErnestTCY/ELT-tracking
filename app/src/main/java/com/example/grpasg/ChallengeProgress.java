package com.example.grpasg;

public class ChallengeProgress {
    private String challengeId;
    private String userId;
    private double currentProgress;
    private double target;
    private String activityType;
    private long startTimestamp;
    private long endTimestamp;

    // Default constructor for Firebase
    public ChallengeProgress() {}

    // Main constructor
    public ChallengeProgress(String challengeId, String userId, double target, String activityType) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.currentProgress = 0;
        this.target = target;
        this.activityType = activityType;
        this.startTimestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getChallengeId() { return challengeId; }
    public String getUserId() { return userId; }
    public double getCurrentProgress() { return currentProgress; }
    public double getTarget() { return target; }
    public String getActivityType() { return activityType; }
    public long getStartTimestamp() { return startTimestamp; }
    public long getEndTimestamp() { return endTimestamp; }

    public void setCurrentProgress(double progress) {
        this.currentProgress = Math.min(progress, target);
        if (currentProgress >= target) {
            this.endTimestamp = System.currentTimeMillis();
        }
    }
} 