package com.homesystem.helpers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FirebaseHelper {
    private static FirebaseDatabase database;
    private static DatabaseReference databaseReference;

    public static void initializeFirebase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            databaseReference = database.getReference();
        }
    }

    public static DatabaseReference getUsersReference() {
        initializeFirebase();
        return databaseReference.child("users");
    }

    public static DatabaseReference getCentralUnitsReference() {
        initializeFirebase();
        return databaseReference.child("centralUnits");
    }

    public static DatabaseReference getSensorsReference() {
        initializeFirebase();
        return databaseReference.child("sensors");
    }

    public static Query getUserByEmail(String email) {
        return getUsersReference().orderByChild("email").equalTo(email);
    }
}