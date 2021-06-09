package com.app.talkzy.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.app.talkzy.R;
import com.app.talkzy.loginRegistrationClasses.LogIn;
import com.app.talkzy.OtherClasses.IntentChanger;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                IntentChanger.change(SplashScreenActivity.this, LogIn.class);

            }
        }, 1000);

    }
}