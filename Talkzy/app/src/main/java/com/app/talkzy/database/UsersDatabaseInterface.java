package com.app.talkzy.database;

import android.app.Activity;

import com.app.talkzy.UserInfo.User;

public interface UsersDatabaseInterface {

    void connect();
    void loginUser(String email, String password);
    void registerUser(User user);
    void addUserToDB(Activity currentActivity, User user);
    void updateUser(User user);
    void sendEmail();
}
