package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.model.InputMatch;
import com.example.myapplication.model.Match;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InputUtils {


    public void checkExistingInputs(Context context, String matchID, String userID, int homeTeamGoals, int awayTeamGoals) {
        DatabaseReference inputMatchesRef = FirebaseDatabase.getInstance().getReference().child("inputMatches");

        //Checking for existing inputs for the same match
        inputMatchesRef.orderByChild("matchID").equalTo(matchID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean inputExists = false;

                for (DataSnapshot inputSnapshot : dataSnapshot.getChildren()) {
                    if (inputSnapshot.child("matchID").getValue(String.class).equals(matchID)) {
                        inputExists = true;

                    }
                }

                if (inputExists) {
                    //Alert the users they cannot make anymore predictions for this specific game
                    Toast.makeText(context, "Deja ati introdus un scor pentru acest meci", Toast.LENGTH_SHORT).show();
                } else {
                    //Save the prediction and proceed to calculate their points
                    InputMatch inputMatch = new InputMatch(userID, matchID, homeTeamGoals, awayTeamGoals);

                    inputMatchesRef.push()
                            .setValue(inputMatch)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firebase", "InputMatch added successfully");
                                // Using the matchID, we get all the data from that specific match
                                DatabaseReference matchesRef = FirebaseDatabase.getInstance().getReference().child("matches");

                                matchesRef.child(matchID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            //The match was found
                                            Match match = snapshot.getValue(Match.class);

                                            if (match != null) {
                                                Log.d("Firebase", "Match details: " + match.toString());
                                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                                Log.d("Firebase", "current User Authenticated: " + firebaseUser.getEmail());
                                                if (firebaseUser != null) {
                                                    String userID = firebaseUser.getUid();
                                                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
                                                    Log.d("Firebase", "User Is not null, user id is: " + userID);

                                                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                                                try {
                                                                    User currentUser = userSnapshot.getValue(User.class);
                                                                    if (currentUser != null) {
                                                                        Log.d("Firebase", "User details: " + currentUser.toString());

                                                                        // Starting the points algorithm
                                                                        int points = 5;

                                                                        // ... rest of the points calculation

                                                                        // Update user points and input match points
                                                                        currentUser.setPoints(currentUser.getPoints() + points);
                                                                        inputMatch.setPointsCollected(points);
                                                                    }
                                                                } catch (Exception e) {
                                                                    Log.e("Firebase", "Error processing user data: " + e.getMessage());
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Log.e("Firebase", "Database error: " + error.getMessage());
                                                        }
                                                    });


//                                                    usersRef.addChildEventListener(new ChildEventListener() {
//                                                        @Override
//                                                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                                                            User currentUser = snapshot.getValue(User.class);
//                                                            if (currentUser != null) {
//                                                                Log.d("Firebase", "User details: " + currentUser.toString());
//
//                                                                // Starting the points algorithm
//                                                                int points = 0;
//
//                                                                //correct result (win, draw, lose)
//                                                                if (match.getHomeTeamGoals() > match.getAwayTeamGoals() && inputMatch.getHomeTeamGoals() > inputMatch.getAwayTeamGoals() ||
//                                                                        match.getHomeTeamGoals() == match.getAwayTeamGoals() && inputMatch.getHomeTeamGoals() == inputMatch.getAwayTeamGoals() ||
//                                                                        match.getHomeTeamGoals() < match.getAwayTeamGoals() && inputMatch.getHomeTeamGoals() < inputMatch.getAwayTeamGoals()) {
//                                                                    points += 3;
//                                                                }
//                                                                //correct # of goals scored by the home team
//                                                                if (match.getHomeTeamGoals() == inputMatch.getHomeTeamGoals()) {
//                                                                    points += 1;
//                                                                }
//                                                                //correct # of goals scored by the away team
//                                                                if (match.getAwayTeamGoals() == inputMatch.getAwayTeamGoals()) {
//                                                                    points += 1;
//                                                                }
//                                                                if (match.getHomeTeamGoals() - match.getAwayTeamGoals() == inputMatch.getHomeTeamGoals() - inputMatch.getAwayTeamGoals()) {
//                                                                    points += 1;
//                                                                }
//                                                                //opposite result
//                                                                if (match.getHomeTeamGoals() > match.getAwayTeamGoals() && inputMatch.getHomeTeamGoals() < inputMatch.getAwayTeamGoals() ||
//                                                                        match.getHomeTeamGoals() < match.getAwayTeamGoals() && inputMatch.getHomeTeamGoals() > inputMatch.getAwayTeamGoals()) {
//                                                                    points -= 2;
//                                                                }
//
//                                                                // Update user points and input match points
//                                                                currentUser.setPoints(currentUser.getPoints() + points);
//                                                                inputMatch.setPointsCollected(points);
//                                                            }
//                                                        }
//
//                                                        @Override
//                                                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                                                        }
//
//                                                        @Override
//                                                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//                                                        }
//
//                                                        @Override
//                                                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                                                        }
//
//                                                        @Override
//                                                        public void onCancelled(@NonNull DatabaseError error) {
//
//                                                        }
//
//                                                    });


                                                }

                                            } else {
                                                Log.e("Firebase", "Match details are null");
                                            }

                                            //Searching for the user associated with Firebase Auth


                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(context, "Failed to retrieve registered inputs", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .addOnFailureListener(e -> Log.e("Firebase", "Error adding input match: " + e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Toast.makeText(context, "Failed to retrieve registered inputs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
