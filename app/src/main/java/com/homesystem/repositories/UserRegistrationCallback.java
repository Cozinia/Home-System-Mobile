package com.homesystem.repositories;

public interface UserRegistrationCallback {
    void onSuccess(String userId);
    void onError(String error);
}