package com.homesystem.repositories;

public interface UserExistsCallback {
    void onResult(boolean exists);
    void onError(String error);
}