package com.example.grpasg;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;

public class AddDummyData {

    public static void addDummyData() {
        // Get database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("history");

        // Replace "dummyUserId123" with an actual user ID if available
        String userId = "dummyUserId123";

        // Dummy cycling data
        HistoryEntry cyclingEntry = new HistoryEntry();
        cyclingEntry.setActivityType("cycling");
        cyclingEntry.setCalories(150);
        cyclingEntry.setDistance(10.5);
        cyclingEntry.setElapsedTime("00:45:30");
        cyclingEntry.setElapsedTimeInMillis(2730000);
        cyclingEntry.setTimestamp("2024-12-09 14:00:00");

        // Dummy coordinates for cycling
        ArrayList<HashMap<String, Double>> cyclingCoordinates = new ArrayList<>();
        HashMap<String, Double> coord1 = new HashMap<>();
        coord1.put("latitude", 37.7749);
        coord1.put("longitude", -122.4194);
        cyclingCoordinates.add(coord1);

        HashMap<String, Double> coord2 = new HashMap<>();
        coord2.put("latitude", 37.7849);
        coord2.put("longitude", -122.4094);
        cyclingCoordinates.add(coord2);

        cyclingEntry.setRouteCoordinates(cyclingCoordinates);

        // Write cycling data
        databaseReference.child(userId).child("cycling").push().setValue(cyclingEntry);

        // Dummy running data
        HistoryEntry runningEntry = new HistoryEntry();
        runningEntry.setActivityType("running");
        runningEntry.setCalories(200);
        runningEntry.setDistance(8.0);
        runningEntry.setElapsedTime("00:40:00");
        runningEntry.setElapsedTimeInMillis(2400000);
        runningEntry.setTimestamp("2024-12-09 16:00:00");

        // Dummy coordinates for running
        ArrayList<HashMap<String, Double>> runningCoordinates = new ArrayList<>();
        HashMap<String, Double> runCoord1 = new HashMap<>();
        runCoord1.put("latitude", 40.7128);
        runCoord1.put("longitude", -74.0060);
        runningCoordinates.add(runCoord1);

        HashMap<String, Double> runCoord2 = new HashMap<>();
        runCoord2.put("latitude", 40.7228);
        runCoord2.put("longitude", -74.0160);
        runningCoordinates.add(runCoord2);

        runningEntry.setRouteCoordinates(runningCoordinates);

        // Write running data
        databaseReference.child(userId).child("running").push().setValue(runningEntry);
    }
}
