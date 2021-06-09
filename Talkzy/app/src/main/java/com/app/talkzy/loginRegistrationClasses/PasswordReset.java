package com.app.talkzy.loginRegistrationClasses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.talkzy.R;
import com.app.talkzy.database.UsersDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class PasswordReset extends AppCompatActivity {

    // Button, EditText
    private EditText inputEmailForPasswordReset;
    private Button sendEmailForResetButton;

    // Email string
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        // EditText and button
        inputEmailForPasswordReset = (EditText) findViewById(R.id.inputEmailForPasswordReset);
        sendEmailForResetButton = (Button) findViewById(R.id.sendEmailForResetButton);

        // Send button
        sendEmailForResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    // Reseting password
    public void resetPassword() {
        email = inputEmailForPasswordReset.getText().toString();
        if(email.isEmpty()) {
            inputEmailForPasswordReset.setError("Input email!");
            inputEmailForPasswordReset.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmailForPasswordReset.setError("Please input valid email!");
            inputEmailForPasswordReset.requestFocus();
            return;
        }

        UsersDatabase.auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(PasswordReset.this, "Check your email to reset password!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PasswordReset.this, "Try again! Something wrong happened!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}