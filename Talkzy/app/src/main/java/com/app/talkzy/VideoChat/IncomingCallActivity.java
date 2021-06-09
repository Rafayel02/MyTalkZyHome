package com.app.talkzy.VideoChat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.talkzy.R;
import com.app.talkzy.database.UsersDatabase;
import com.app.talkzy.UserInfo.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class IncomingCallActivity extends AppCompatActivity {

    private CircleImageView senderImage;
    private ImageView acceptVideoCallButt;
    private TextView senderNameText;
    private ImageView declineVideoCalButt;

    private static FirebaseUser currentUser;
    public static User otherUser;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_activity);

        try {
            currentUser = UsersDatabase.auth.getCurrentUser();
        } catch(Exception e) {}
        mediaPlayer = MediaPlayer.create(this, R.raw.calling);
        mediaPlayer.start();
        senderImage = findViewById(R.id.senderImage);
        senderNameText = (TextView) findViewById(R.id.senderNameText);
        acceptVideoCallButt = (ImageView) findViewById(R.id.acceptVideoCallButt);
        declineVideoCalButt = (ImageView) findViewById(R.id.declineVideoCalButt);
        setParams();

        acceptVideoCallButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptCall();
            }
        });

        declineVideoCalButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                declineCall();
            }
        });

        UsersDatabase.database.getReference().child("Users")
                .child(otherUser.getId())
                .addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.child("calling").exists()) {
                            mediaPlayer.stop();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void acceptCall() {
            UsersDatabase.database.getReference("Users")
                    .child(currentUser.getUid())
                    .child("accepted")
                    .setValue(otherUser.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    UsersDatabase.database.getReference("Users")
                            .child(otherUser.getId())
                            .child("accepted")
                            .setValue(currentUser.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            UsersDatabase.database.getReference("Users")
                                    .child(currentUser.getUid())
                                    .child("incomingCall")
                                    .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    UsersDatabase.database.getReference("Users")
                                            .child(otherUser.getId())
                                            .child("calling")
                                            .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            finish();
                                        }
                                    });

                                }
                            });

                        }
                    });

                }
            });


    }

    private void declineCall() {
        mediaPlayer.stop();
        UsersDatabase.database.getReference("Users")
                .child(otherUser.getId())
                .child("calling")
                .setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                UsersDatabase.database.getReference("Users")
                        .child(currentUser.getUid())
                        .child("incomingCall")
                        .setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                });
            }
        });
    }

    private void setParams() {
        Glide
                .with(getApplicationContext())
                .load(otherUser.getImageUri())
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(senderImage);

        senderNameText.setText(otherUser.getUserName());

    }


}