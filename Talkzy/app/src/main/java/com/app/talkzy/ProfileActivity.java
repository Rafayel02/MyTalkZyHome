package com.app.talkzy;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.talkzy.database.UsersDatabase;
import com.app.talkzy.UserInfo.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    ImageView profileImageView;
    TextView friendsCountTextView;
    TextView userNameTextView;
    TextView fullNameTextView;
    TextView countryTextView;
    TextView birthdayTextView;
    Button upButton;
    Button downButton;
    private TextView bioTextView;


    private User mUser;
    private User user;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;

    private String mCurrent_state;

    private boolean zoomOut =  false;
    private ConstraintLayout profileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String userId = getIntent().getStringExtra("userId");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        profileImageView = (ImageView) findViewById(R.id.profileImageView);
        friendsCountTextView = (TextView) findViewById(R.id.friendsCount);
        userNameTextView = (TextView) findViewById(R.id.userName);
        fullNameTextView = (TextView) findViewById(R.id.fullName);
        countryTextView = (TextView) findViewById(R.id.countryTextShow);
        bioTextView = (TextView) findViewById(R.id.bioTextView);
        birthdayTextView = (TextView) findViewById(R.id.birthdayTextShow);
        upButton = (Button) findViewById(R.id.upButton);
        downButton = (Button) findViewById(R.id.downButton);
        profileLayout = (ConstraintLayout) findViewById(R.id.profileLayout);
        profileLayout.setVisibility(View.VISIBLE);

        mCurrent_state = "not_friends";
        ConstraintLayout.LayoutParams imgDefaultPosition = (ConstraintLayout.LayoutParams) profileImageView.getLayoutParams();

        profileImageView .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (zoomOut) {
                        profileLayout.setVisibility(View.VISIBLE);
                        profileImageView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
                        profileImageView.setAdjustViewBounds(true);
                        profileImageView.setLayoutParams(imgDefaultPosition);
                        zoomOut = false;
                    } else {
                        profileLayout.setVisibility(View.INVISIBLE);
                        profileImageView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
                        profileImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        profileImageView.setAdjustViewBounds(true);
                        zoomOut = true;
                    }
                } catch(Exception e) {}
            }
        });

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser = dataSnapshot.getValue(User.class);

                // --------------- Friends List / Request Feature --------------------

                mFriendReqDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        downButton.setVisibility(View.GONE);

                        if(dataSnapshot.hasChild(mUser.getId())){
                            String req_type = dataSnapshot.child(mUser.getId()).child("request_type").getValue().toString();

                            if(req_type.equals("received")){
                                mCurrent_state = "req_received";
                                upButton.setText("Accept Friend Request");
                                downButton.setVisibility(View.VISIBLE);
                            }

                            else if(req_type.equals("sent")){
                                mCurrent_state = "req_sent";
                                upButton.setText("Cancel Friend Request");
                            }
                        }

                        else{

                            mFriendDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(mUser.getId())){

                                        mCurrent_state = "friends";
                                        upButton.setText("UnFriend");

                                    }
                                    else{
                                        mCurrent_state = "not_friends";
                                        upButton.setText("Add Friend");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                makeProfile();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                upButton.setEnabled(false);


                // --- NOT FRIENDS STATE -----------------------

                if(mCurrent_state.equals("not_friends")){

                    mFriendReqDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).child(mUser.getId()).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mFriendReqDatabase.child(mUser.getId()).child(UsersDatabase.auth.getCurrentUser().getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mCurrent_state = "req_sent";
                                        upButton.setText("Cancel Friend Request");


                                        int min = 1;
                                        int max = 1000000000;
                                        Random r = new Random();
                                        int i1 = r.nextInt(max - min + 1) + min;


                                        Notification notification = new Notification();
                                        notification.setId(i1);
                                        notification.setFromUserId(UsersDatabase.auth.getCurrentUser().getUid());
                                        notification.setNotificationType("friend_req");
                                        notification.setIsSeen(false);
                                        notification.setNotificationTime(System.currentTimeMillis());

                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("id", notification.getId());
                                        hashMap.put("fromUserId", notification.getFromUserId());
                                        hashMap.put("notificationType", notification.getNotificationType());
                                        hashMap.put("isSeen", notification.getIsSeen());
                                        hashMap.put("notificationTime", notification.getNotificationTime());



                                        UsersDatabase.database.getReference("Notifications").child(mUser.getId()).child(String.valueOf(i1)).setValue(notification);
                                        Toast.makeText(ProfileActivity.this, "Request Sent Successfully.", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }
                            else{

                                Toast.makeText(ProfileActivity.this, "Failed Sending Request.", Toast.LENGTH_SHORT).show();

                            }

                            upButton.setEnabled(true);

                        }
                    });



                }

                // --- CANCEL REQUEST STATE -----------------------

                if(mCurrent_state.equals("req_sent")){

                    mFriendReqDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).child(mUser.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(mUser.getId()).child(UsersDatabase.auth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(mUser.getId());
                                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            int key = -1;
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                                Notification notification = snapshot.getValue(Notification.class);
                                                if(notification.getFromUserId().equals(UsersDatabase.auth.getCurrentUser().getUid()) && notification.getNotificationType().equals("friend_req")){
                                                    key = Integer.parseInt(snapshot.getKey());
                                                }
                                            }

                                            if(key != -1){
                                                FirebaseDatabase.getInstance().getReference("Notifications").child(mUser.getId()).child(String.valueOf(key)).removeValue();
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    upButton.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    upButton.setText("Add Friend");
                                    Toast.makeText(ProfileActivity.this, "Request Canceled Successfully.", Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    });

                }


                // ------------- REQUEST RECEIVED STATE --------------------

                if(mCurrent_state.equals("req_received")){

                    String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).child(mUser.getId()).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(mUser.getId()).child(UsersDatabase.auth.getCurrentUser().getUid()).setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendReqDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).child(mUser.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            mFriendReqDatabase.child(mUser.getId()).child(UsersDatabase.auth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    UsersDatabase.database.getReference("Users").child(UsersDatabase.auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                            user = snapshot.getValue(User.class);
                                                                            HashMap<String, Object> hashMap = new HashMap<>();
                                                                            hashMap.put("friendsCount", user.getFriendsCount()+1);

                                                                            UsersDatabase.database.getReference("Users").child(UsersDatabase.auth.getUid()).updateChildren(hashMap);

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });

                                                                    UsersDatabase.database.getReference("Users").child(mUser.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                            user = snapshot.getValue(User.class);
                                                                            HashMap<String, Object> hashMap = new HashMap<>();
                                                                            hashMap.put("friendsCount", user.getFriendsCount()+1);

                                                                            UsersDatabase.database.getReference("Users").child(mUser.getId()).updateChildren(hashMap);

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });


                                                                    upButton.setEnabled(true);
                                                                    mCurrent_state = "friends";
                                                                    upButton.setText("UnFriend");
                                                                    Toast.makeText(ProfileActivity.this, "Friend Added Successfully.", Toast.LENGTH_SHORT).show();

                                                                }
                                                            });

                                                        }
                                                    });

                                                }
                                            });
                                }
                            });

                }

                if(mCurrent_state.equals("friends")){


                    mFriendDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).child(mUser.getId()).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(mUser.getId()).child(UsersDatabase.auth.getCurrentUser().getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {



                                                    UsersDatabase.database.getReference("Users").child(UsersDatabase.auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                            user = snapshot.getValue(User.class);
                                                            HashMap<String, Object> hashMap = new HashMap<>();
                                                            hashMap.put("friendsCount", user.getFriendsCount()-1);

                                                            UsersDatabase.database.getReference("Users").child(UsersDatabase.auth.getUid()).updateChildren(hashMap);

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                                    UsersDatabase.database.getReference("Users").child(mUser.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                            user = snapshot.getValue(User.class);
                                                            HashMap<String, Object> hashMap = new HashMap<>();
                                                            hashMap.put("friendsCount", user.getFriendsCount()-1);

                                                            UsersDatabase.database.getReference("Users").child(mUser.getId()).updateChildren(hashMap);

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });



                                                                    upButton.setEnabled(true);
                                                                    mCurrent_state = "not_friends";
                                                                    upButton.setText("Add Friend");
                                                                    Toast.makeText(ProfileActivity.this, "Friend Removed Successfully.", Toast.LENGTH_SHORT).show();


                                                }
                                            });
                                }
                            });

                }

            }
        });

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downButton.setEnabled(false);

                mFriendReqDatabase.child(mUser.getId()).child(UsersDatabase.auth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFriendReqDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).child(mUser.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(UsersDatabase.auth.getCurrentUser().getUid());
                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        int key = -1;
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                            Notification notification = snapshot.getValue(Notification.class);
                                            if(notification.getFromUserId().equals(mUser.getId()) && notification.getNotificationType().equals("friend_req")){
                                                key = Integer.parseInt(snapshot.getKey());
                                            }
                                        }

                                        if(key != -1){
                                            FirebaseDatabase.getInstance().getReference("Notifications").child(UsersDatabase.auth.getCurrentUser().getUid()).child(String.valueOf(key)).removeValue();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                upButton.setEnabled(true);
                                downButton.setVisibility(View.GONE);
                                mCurrent_state = "not_friends";
                                upButton.setText("Add Friend");
                                Toast.makeText(ProfileActivity.this, "Request Canceled Successfully.", Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                });
            }
        });





    }

    public void addFriendRequest(User user){



    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void makeProfile(){
        Glide
                .with(getApplicationContext())
                .load(mUser.getImageUri())
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(profileImageView);

        userNameTextView.setText(mUser.getUserName());
        fullNameTextView.setText(mUser.getFullName());
        bioTextView.setText(mUser.getBioText());
        friendsCountTextView.setText(mUser.getFriendsCount()+"");

        // Setting birthday text
        if(Objects.equals(mUser.getBirthday(), "null")) {
            birthdayTextView.setText("No info");
        } else {
            birthdayTextView.setText(mUser.getBirthday());

        }
    }

    private void status(String status){

        if(UsersDatabase.auth.getUid() != null) {
            UsersDatabase.database.getReference("Users").child(UsersDatabase.auth.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);

            UsersDatabase.database.getReference("Users").child(UsersDatabase.auth.getUid()).updateChildren(hashMap);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}