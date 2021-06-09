package com.app.talkzy.loginRegistrationClasses;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.app.talkzy.MainActivity;
import com.app.talkzy.OtherClasses.IntentChanger;
import com.app.talkzy.R;
import com.app.talkzy.database.UsersDatabase;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogIn extends AppCompatActivity {

    // Sign up event
    public static Button loginButton;
    private Button toSignUpPageButton;
    private Button forgotPasswordButton;
    private Button visUnVisOfPasswordButton;
    private SignInButton googleSignInButton;
    private UsersDatabase usersDatabase = new UsersDatabase();

    // TextView, login button
    private String email;
    private String password;
    private TextView emailInputText;
    private TextView logInPasswordInputText;
    public static Activity LogInActivity;

    // Boolean for password vis. , unv.
    boolean passVis = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        LogInActivity = this;

        // Email, Password, Google Button, signIn Button, login Button (id)
        emailInputText = (TextView) findViewById(R.id.emailInputText);
        logInPasswordInputText = (TextView) findViewById(R.id.logInPasswordInputText);
        googleSignInButton = (SignInButton) findViewById(R.id.googleSignInButton);
        toSignUpPageButton = (Button) findViewById(R.id.toSignUpPageButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        forgotPasswordButton = (Button) findViewById(R.id.forgotPasswordButton);
        visUnVisOfPasswordButton = (Button) findViewById(R.id.visUnVisOfPasswordButton);

        // Connecting To Google for Google sign in, creating database
        usersDatabase.connect();
        usersDatabase.connectToGoogle();

        // Staying signed in
        FirebaseUser firebaseUser = UsersDatabase.auth.getCurrentUser();
        if(firebaseUser != null) {
            if(firebaseUser.isEmailVerified()) {
                checkIfUserExistsInDB();
            }
            else{
                UsersDatabase.auth.signOut();
                Toast.makeText(getApplicationContext(),"Please Verify Your Email !!!", Toast.LENGTH_SHORT).show();
            }
        }

        // Password vis. , unv.
        visUnVisOfPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passVis) {
                    visUnVisOfPasswordButton.setBackgroundResource(R.drawable.ic_baseline_visibility_24);
                    logInPasswordInputText.setTransformationMethod(null);
                    passVis = false;
                } else {
                    visUnVisOfPasswordButton.setBackgroundResource(R.drawable.ic_baseline_visibility_off_24);
                    logInPasswordInputText.setTransformationMethod(new PasswordTransformationMethod());
                    passVis = true;
                }
            }
        });

        // Google sig in button event
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersDatabase.signUpWithGoogle();
            }
        });



        // Sign up button event, which get activity of registration
        toSignUpPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentChanger.change(LogIn.this, Registration.class, 0);
            }
        });

        // Login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setClickable(false);
                if(validEmailAndPassword()) {
                    usersDatabase.loginUser(email, password);
                }
                else{
                    loginButton.setClickable(true);
                }
            }
        });

        // Forgot password button event
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentChanger.change(LogIn.this, PasswordReset.class, 0);
            }
        });

    }

    public void checkIfUserExistsInDB(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    IntentChanger.change(LogInActivity, MainActivity.class);
                }
                else{
                    UsersDatabase.auth.signOut();
                    Toast.makeText(getApplicationContext(),"There are some problems you are removed from Database",Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    public boolean validEmailAndPassword() {
        email = emailInputText.getText().toString();
        password = logInPasswordInputText.getText().toString();

        if (email.isEmpty()) {
            emailInputText.setError("Input email!");
            emailInputText.requestFocus();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputText.setError("Please input valid email!");
            emailInputText.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            logInPasswordInputText.setError("Input password!");
            logInPasswordInputText.requestFocus();
            return false;

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Get result for signing in with Google
        usersDatabase.googleSignInGiveResult(requestCode, resultCode, data );
    }

}