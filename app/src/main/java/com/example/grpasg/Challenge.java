package com.example.grpasg;

public class Challenge {
    private String title;
    private String description;
    private int iconResId;
    private int progress;
    private int goal;
    private String period;

    // Required empty constructor for Firebase
    public Challenge() {}

    public Challenge(String title, String description, int iconResId, int progress, int goal) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.progress = progress;
        this.goal = goal;
    }

    public Challenge(String title, String description, int iconResId, int progress, int goal, String period) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.progress = progress;
        this.goal = goal;
        this.period = period;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    
    public int getGoal() { return goal; }
    public void setGoal(int goal) { this.goal = goal; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
} 