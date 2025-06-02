package com.homesystem.repositories;

import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.homesystem.helpers.FirebaseHelper;
import com.homesystem.models.User;
import com.homesystem.utils.PasswordUtils;

public class UserRepository {
    private static final String TAG = "UserRepository";


    public static void registerUser(User user, UserRegistrationCallback callback) {
        Log.d(TAG, "Starting user registration for email: " + user.getEmail());
        DatabaseReference usersRef = FirebaseHelper.getUsersReference();

        // Generate a unique key for the user
        String userId = usersRef.push().getKey();
        if (userId == null) {
            Log.e(TAG, "Failed to generate user ID");
            callback.onError("Failed to generate user ID");
            return;
        }

        Log.d(TAG, "Generated user ID: " + userId);

        // Set the user ID
        user.setId(userId);

        // Save user to database
        usersRef.child(userId).setValue(user.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User registered successfully with ID: " + userId);
                    callback.onSuccess(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Registration failed: " + e.getMessage(), e);
                    callback.onError("Registration failed: " + e.getMessage());
                });
    }

    public static void checkUserExists(String email, UserExistsCallback callback) {
        Log.d(TAG, "Checking if user exists with email: " + email);
        FirebaseHelper.getUserByEmail(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.exists();
                Log.d(TAG, "User exists check result: " + exists);
                callback.onResult(exists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error while checking user existence: " + databaseError.getMessage());
                callback.onError("Database error: " + databaseError.getMessage());
            }
        });
    }

    // Adaugă această metodă pentru login
    public static void loginUser(String email, String password, UserLoginCallback callback) {
        Log.d(TAG, "Attempting login for email: " + email);

        // Hash the provided password to compare with stored hash
        String hashedPassword = PasswordUtils.hashPassword(password);

        FirebaseHelper.getUserByEmail(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "User not found with email: " + email);
                    callback.onError("Invalid email or password");
                    return;
                }

                // Get the first (and should be only) user with this email
                DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                User user = userSnapshot.getValue(User.class);

                if (user == null) {
                    Log.e(TAG, "Failed to parse user data");
                    callback.onError("Error retrieving user data");
                    return;
                }

                // Verify password
                if (user.getHashedPassword() != null && user.getHashedPassword().equals(hashedPassword)) {
                    Log.d(TAG, "Login successful for user: " + email);

                    // Update last login time
                    user.setLastUpdatedAt(System.currentTimeMillis());

                    // Update the user in database with new lastUpdatedAt
                    updateLastLoginTime(user.getId(), user.getLastUpdatedAt());

                    callback.onSuccess(user);
                } else {
                    Log.d(TAG, "Invalid password for user: " + email);
                    callback.onError("Invalid email or password");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error during login: " + databaseError.getMessage());
                callback.onError("Login failed: " + databaseError.getMessage());
            }
        });
    }

    // Metodă auxiliară pentru a actualiza timpul ultimei autentificări
    private static void updateLastLoginTime(String userId, long lastUpdatedAt) {
        FirebaseHelper.getUsersReference()
                .child(userId)
                .child("lastUpdatedAt")
                .setValue(lastUpdatedAt)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Last login time updated"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to update last login time: " + e.getMessage()));
    }

    // Metodă pentru a obține utilizatorul curent după ID
    public static void getUserById(String userId, UserLoginCallback callback) {
        Log.d(TAG, "Getting user by ID: " + userId);

        FirebaseHelper.getUsersReference()
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                callback.onSuccess(user);
                            } else {
                                callback.onError("Failed to parse user data");
                            }
                        } else {
                            callback.onError("User not found");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError("Database error: " + databaseError.getMessage());
                    }
                });
    }
}