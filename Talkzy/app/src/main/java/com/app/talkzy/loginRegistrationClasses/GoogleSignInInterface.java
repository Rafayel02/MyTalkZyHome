package com.app.talkzy.loginRegistrationClasses;

import android.content.Intent;

import androidx.annotation.Nullable;

public interface GoogleSignInInterface {
    void connectToGoogle();
    void signUpWithGoogle();
    void googleSignInGiveResult(int requestCode, int resultCode, @Nullable Intent data);

}
