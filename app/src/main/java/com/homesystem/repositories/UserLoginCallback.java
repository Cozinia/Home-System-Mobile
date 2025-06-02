package com.homesystem.repositories;

import com.homesystem.models.User;

public interface UserLoginCallback {
    void onSuccess(User user);
    void onError(String error);
}
