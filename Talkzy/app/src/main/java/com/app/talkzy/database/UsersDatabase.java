package com.app.talkzy.database;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.talkzy.Fragment.ProfileFragment;
import com.app.talkzy.OtherClasses.IntentChanger;
import com.app.talkzy.MainActivity;
import com.app.talkzy.UserInfo.User;
import com.app.talkzy.loginRegistrationClasses.GoogleSignInInterface;
import com.app.talkzy.loginRegistrationClasses.LogIn;
import com.app.talkzy.loginRegistrationClasses.Registration;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;

public class UsersDatabase implements UsersDatabaseInterface, GoogleSignInInterface {

    public static FirebaseDatabase database;
    public static FirebaseAuth auth; //must change
    public static GoogleSignInClient googleSignInClient;

    private DatabaseReference reference;
    static int count;
    private boolean isRegisteredSuccessfully = false;

    public static void tryToGetFromDB() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(auth.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                String name;
                try {
                    name = (String) snapshot.child("country").getValue();
                } catch(Exception e) {
                    e.printStackTrace();
                    name = "Not defined";
                }
                if (name == null || name == "null") {
                    name = "Not defined";
                }
                ProfileFragment.setCountryFinally(name);
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    public static void setCountryToDB(String countryName) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(UsersDatabase.auth.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("country", countryName);

        reference.updateChildren(hashMap);
    }

    public static void setContinentToDB(String continent) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(UsersDatabase.auth.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("continent", continent);

        reference.updateChildren(hashMap);
    }

    public static void clearDangerData() {
        UsersDatabase.database.getReference("Users")
                .child(auth.getUid())
                .child("sendingMeetRequest").setValue(null);
        System.out.println("anasun");
        UsersDatabase.database.getReference("Users")
                .child(auth.getUid())
                .child("gettingMeetRequest").setValue(null);
        UsersDatabase.database.getReference("Users")
                .child(auth.getUid())
                .child("calling").setValue(null);
        UsersDatabase.database.getReference("Users")
                .child(auth.getUid())
                .child("incomingCall").setValue(null);
    }

    public static void sendMeetRequest(String myID, String otherId) {
        DatabaseReference ref = UsersDatabase.database.getReference("Users")
                .child(myID)
                .child("sendingMeetRequest");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(otherId, "waiting");

        ref.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                DatabaseReference ref = UsersDatabase.database.getReference("Users")
                        .child(otherId)
                        .child("gettingMeetRequest");
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(myID, "waiting");
                ref.updateChildren(hashMap);
            }
        });
    }

    @Override
    public void connect() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser firebaseUser = UsersDatabase.auth.getCurrentUser();
                    if(firebaseUser != null) {
                        if(firebaseUser.isEmailVerified()) {
                            DatabaseReference localReference = database.getReference("Users").child(firebaseUser.getUid());
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isVerified", true);
                            localReference.updateChildren(hashMap);
                            IntentChanger.change(LogIn.LogInActivity, MainActivity.class);
                        } else {
                            auth.signOut();
                            Toast.makeText(LogIn.LogInActivity, "Please verify email!", Toast.LENGTH_SHORT).show();
                            LogIn.loginButton.setClickable(true);

                        }
                    }
                    else{
                        LogIn.loginButton.setClickable(true);
                    }
                } else {
                    LogIn.loginButton.setClickable(true);
                    Toast.makeText(LogIn.LogInActivity, "Check email or password!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void signUpWithGoogle() {
        Intent intent = googleSignInClient.getSignInIntent();
        LogIn.LogInActivity.startActivityForResult(intent, 100);
    }

    @Override
    public void connectToGoogle() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken("440406508048-go403nlo8k5hmdutk84cgfecamko9isj.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(LogIn.LogInActivity , googleSignInOptions);
    }

    boolean isComplete = false;
    @Override
    public void googleSignInGiveResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 100) {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn
                    .getSignedInAccountFromIntent(data);

            if(signInAccountTask.isSuccessful()) {
                String s = "Google sign in successful!!";
                Toast.makeText(LogIn.LogInActivity, s , Toast.LENGTH_SHORT).show();
                try {
                    GoogleSignInAccount googleSignInAccount = signInAccountTask
                            .getResult(ApiException.class);
                    if(googleSignInAccount != null) {
                        AuthCredential authCredential = GoogleAuthProvider
                                .getCredential(googleSignInAccount.getIdToken(), null);
                        UsersDatabase.auth.signInWithCredential(authCredential)
                            .addOnCompleteListener(LogIn.LogInActivity, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        final GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(LogIn.LogInActivity);
                                        User user = new User();
                                        FirebaseUser firebaseUser = UsersDatabase.auth.getCurrentUser();
                                        System.out.println(firebaseUser.getUid());

                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference reference = database.getReference("Users").child(firebaseUser.getUid());
                                        reference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                // checkingIf User exist In database
                                                try {
                                                    if (!snapshot.getValue(User.class).equals(null)) {
                                                        if(isComplete == false) {
                                                            IntentChanger.change(LogIn.LogInActivity, MainActivity.class);
                                                            isComplete = true;
                                                        }
                                                    }
                                                }
                                                // Else Adding value to DB
                                                catch (NullPointerException e){
                                                    user.setFullName(googleSignInAccount.getDisplayName());
                                                    user.setEmail(googleSignInAccount.getEmail());
                                                    user.setUserName(googleSignInAccount.getEmail().substring(0, googleSignInAccount.getEmail().indexOf("@")));
                                                    user.setId(firebaseUser.getUid());
                                                    user.setImageUri(String.valueOf(googleSignInAccount.getPhotoUrl()));
                                                    user.setStatus("online");
                                                    user.setTypingTo("noOne");
                                                    user.setBirthday("null");
                                                    user.setGender("null");
                                                    user.setIsVerified(true);
                                                    user.setFriendsCount(0);
                                                    addUserToDB(LogIn.LogInActivity, user);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                }
                            });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void sendEmail() {
        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(Registration.RegistrationActivity, "Verify your email", Toast.LENGTH_SHORT).show();
                } else {}
            }
        });
    }

    @Override
    public void registerUser(User user) {

        auth.createUserWithEmailAndPassword(user.getEmail(), user.tGetPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            sendEmail();
                            user.setFriendsCount(0);
                            user.setIsVerified(false);
                            addUserToDB(Registration.RegistrationActivity,user);
                            Registration.RegistrationActivity.finish();
                            Registration.registrationButton.setClickable(true);

                        }
                    }
                });
    }

    @Override
    public void updateUser(@NotNull User user) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("fullName", user.getFullName());
        hashMap.put("userName", user.getUserName());
        hashMap.put("birthday", user.getBirthday());
        hashMap.put("bioText", user.getBioText());
        hashMap.put("gender", user.getGender());

        database.getReference("Users").child(user.getId()).updateChildren(hashMap);

    }

    @Override
    public void addUserToDB(Activity currentActivity, User user) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        String userId = firebaseUser.getUid();
        user.setId(userId);
        reference = database.getReference("Users").child(userId);
        reference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if(firebaseUser.isEmailVerified()){
                    }
                    else{
                        auth.signOut();
                    }
                }
                else {
                    Registration.registrationButton.setClickable(true);
                }
            }
        });

    }

}
