    package com.homesystem.models;

    import java.util.HashMap;
    import java.util.Map;

    public class User {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String hashedPassword;
        private String token;
        private long createdAt;
        private long lastUpdatedAt;

        // Default constructor required for Firebase
        public User() {}

        public User(String firstName, String lastName, String email, String hashedPassword) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.hashedPassword = hashedPassword;
            this.createdAt = System.currentTimeMillis();
            this.lastUpdatedAt = System.currentTimeMillis();
        }

        // Convert to Map for Firebase
        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("firstName", firstName);
            result.put("lastName", lastName);
            result.put("email", email);
            result.put("hashedPassword", hashedPassword);
            result.put("token", token);
            result.put("createdAt", createdAt);
            result.put("lastUpdatedAt", lastUpdatedAt);
            return result;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getHashedPassword() { return hashedPassword; }
        public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

        public long getLastUpdatedAt() { return lastUpdatedAt; }
        public void setLastUpdatedAt(long lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    }