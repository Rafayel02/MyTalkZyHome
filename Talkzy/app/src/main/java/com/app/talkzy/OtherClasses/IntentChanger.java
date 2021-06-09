package com.app.talkzy.OtherClasses;

import android.app.Activity;
import android.content.Intent;

public class IntentChanger {

    public static void change(Activity first, Class second) {
        Intent toMainIntent = new Intent(first, second);
        toMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        first.startActivity(toMainIntent);
        first.finish();
    }

    public static void change(Activity first, Class second, int notFinishableActivity) {
        Intent toMainIntent = new Intent(first, second);
        first.startActivity(toMainIntent);

    }


}
