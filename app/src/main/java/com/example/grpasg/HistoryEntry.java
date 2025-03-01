package com.example.grpasg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class HistoryEntry implements Serializable {
    private String timestamp;
    private String elapsedTime;
    private double distance;
    private double calories;
    private ArrayList<HashMap<String, Double>> routeCoordinates; // Add this field

    // Default constructor for Firebase
    public HistoryEntry() {}
    private long elapsedTimeInMillis; // Add this field


    public void setElapsedTimeInMillis(long elapsedTimeInMillis) {
        this.elapsedTimeInMillis = elapsedTimeInMillis;
    }

    // Constructor with parameters
    // Constructor with all parameters
    public HistoryEntry(String timestamp, String elapsedTime, double distance, double calories, ArrayList<HashMap<String, Double>> routeCoordinates) {
        this.timestamp = timestamp;
        this.elapsedTime = elapsedTime;
        this.distance = distance;
        this.calories = calories;
        this.routeCoordinates = routeCoordinates;
    }

    private String activityType; // Add this field

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }


    // Getters and Setters
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public ArrayList<HashMap<String, Double>> getRouteCoordinates() {
        return routeCoordinates;
    }

    public void setRouteCoordinates(ArrayList<HashMap<String, Double>> routeCoordinates) {
        this.routeCoordinates = routeCoordinates;
    }
    public long getElapsedTimeInMillis() {
        if (elapsedTime == null || elapsedTime.isEmpty()) {
            return 0; // Default to 0 if elapsedTime is null or empty
        }
        try {
            String[] timeParts = elapsedTime.split(":");
            long hours = Long.parseLong(timeParts[0]);
            long minutes = Long.parseLong(timeParts[1]);
            long seconds = Long.parseLong(timeParts[2]);
            return (hours * 3600 + minutes * 60 + seconds) * 1000; // Convert to milliseconds
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Return 0 if parsing fails
        }
    }
}
