package com.app.talkzy.loginRegistrationClasses;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.talkzy.R;
import com.app.talkzy.UserInfo.User;
import com.app.talkzy.database.UsersDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Registration extends AppCompatActivity {

    //User params
    private String fullName;
    private String userName;
    private String email;
    private String password;
    private String confirmPassword;

    //Registration layout params
    private EditText regFullNameInputText;
    private EditText regUserNameInputText;
    private EditText regEmailInputText;
    private EditText regPasswordInputText;
    private EditText regPasswordConfirmInputText;
    public static Button registrationButton;
    private Button visUnVisOfPasswordButton;
    private Button visUnVisOfPasswordConfirmButton;

    // Booleans for passwords vis. , unv.
    boolean passVis1 = true;
    boolean passVis2 = true;

    //Activitys
    ConstraintLayout registrationActivity;
    ConstraintLayout verifyActivity;
    public static Activity RegistrationActivity;
    UsersDatabase usersDatabase = new UsersDatabase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        RegistrationActivity = this;

        // Sign up button, texts (id)
        registrationButton = (Button) findViewById(R.id.registrationButton);
        regFullNameInputText = (EditText) findViewById(R.id.regFullNameInputText);
        regUserNameInputText = (EditText) findViewById(R.id.regUserNameInputText);
        regEmailInputText = (EditText) findViewById(R.id.regEmailInputText);
        regPasswordInputText = (EditText) findViewById(R.id.regPasswordInputText);
        regPasswordConfirmInputText = (EditText) findViewById(R.id.regPasswordConfirmInputText);
        visUnVisOfPasswordButton = (Button) findViewById(R.id.visUnVisOfPasswordButton);
        visUnVisOfPasswordConfirmButton = (Button) findViewById(R.id.visUnVisOfPasswordConfirmButton);

        // Activities (Layouts)
        registrationActivity = (ConstraintLayout) findViewById(R.id.registrationActivity);
        verifyActivity = (ConstraintLayout) findViewById(R.id.verifyActivity);

        // Password visibility-unv.
        visUnVisOfPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passVis1) {
                    visUnVisOfPasswordButton.setBackgroundResource(R.drawable.ic_baseline_visibility_24);
                    regPasswordInputText.setTransformationMethod(null);
                    passVis1 = false;
                } else {
                    visUnVisOfPasswordButton.setBackgroundResource(R.drawable.ic_baseline_visibility_off_24);
                    regPasswordInputText.setTransformationMethod(new PasswordTransformationMethod());
                    passVis1 = true;
                }
            }
        });

        // Confirm password visibility-unv.
        visUnVisOfPasswordConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passVis2) {
                    visUnVisOfPasswordConfirmButton.setBackgroundResource(R.drawable.ic_baseline_visibility_24);
                    regPasswordConfirmInputText.setTransformationMethod(null);
                    passVis2 = false;
                } else {
                    visUnVisOfPasswordConfirmButton.setBackgroundResource(R.drawable.ic_baseline_visibility_off_24);
                    regPasswordConfirmInputText.setTransformationMethod(new PasswordTransformationMethod());
                    passVis2 = true;
                }
            }
        });

        // sign up button listener
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                registrationButton.setClickable(false);
                if(validInfo()) {
                    //registrationActivity.setVisibility(View.INVISIBLE);
                    //verifyActivity.setVisibility(View.VISIBLE);
                    User user = new User();
                    user.setFullName(fullName);
                    user.setUserName(userName);
                    user.setStatus("online");
                    user.setEmail(email);
                    user.setPassword(password);
                    user.setImageUri("default");
                    user.setTypingTo("noOne");
                    user.setBirthday("null");
                    user.setGender("null");
                    registerIfUserNameAvailable(user);

                }
                else{
                    registrationButton.setClickable(true);
                }
            }
        });

        // Set back arrow
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    boolean userNameIsAvailable = true;
    private void registerIfUserNameAvailable(User inProcessUser){

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(Objects.equals(inProcessUser.getUserName(), user.getUserName())) {
                        userNameIsAvailable = false;
                        break;
                    }
                }
                if(userNameIsAvailable) {
                    usersDatabase.registerUser(inProcessUser);
                } else {
                    userNameIsAvailable = true;
                    registrationButton.setClickable(true);
                    Toast.makeText(Registration.this, "Try another username!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Checking texts for registration
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean validInfo() {
        //Setting params of user
        fullName = regFullNameInputText.getText().toString();
        userName = regUserNameInputText.getText().toString();
        email = regEmailInputText.getText().toString();
        password = regPasswordInputText.getText().toString();
        confirmPassword = regPasswordConfirmInputText.getText().toString();

        if (fullName.isEmpty()) {
            regFullNameInputText.setError("Input Full Name!");
            regFullNameInputText.requestFocus();
            return false;
        }

        if (userName.isEmpty()) {
            regUserNameInputText.setError("Input Username!");
            regUserNameInputText.requestFocus();
            return false;
        }

        if (userName.contains(" ")) {
            regUserNameInputText.setError("username can`t contain space!");
            regUserNameInputText.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            regEmailInputText.setError("Input email!");
            regEmailInputText.requestFocus();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            regEmailInputText.setError("Please input valid email!");
            regEmailInputText.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            regPasswordInputText.setError("Input password!");
            regPasswordInputText.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            regPasswordConfirmInputText.setError("Input password!");
            regPasswordConfirmInputText.requestFocus();
            return false;
        }

        if(!Objects.equals(password, confirmPassword)) {
            Toast.makeText(getApplicationContext(), "Passwords don't match!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Back arrow event
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Back arrow event
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}