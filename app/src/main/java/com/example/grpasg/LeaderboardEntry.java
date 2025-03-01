package com.example.grpasg;

public class LeaderboardEntry {
    private String username;
    private double distance;
    private int rank;

    public LeaderboardEntry(String username, double distance, int rank) {
        this.username = username;
        this.distance = distance;
        this.rank = rank;
    }

    public String getUsername() { return username; }
    public double getDistance() { return distance; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
} 