package com.app.talkzy.VideoChat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class OutgoingCallActivity extends AppCompatActivity {

    private ImageView cancelCallButt;
    private TextView receiverNameText;
    private CircleImageView receiverImage;
    public static User otherUser;
    private TextView callingInProcess;
    private FirebaseUser currentUser = UsersDatabase.auth.getCurrentUser();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call_activity);

        // Views
        receiverImage = findViewById(R.id.senderImage);
        receiverNameText = (TextView) findViewById(R.id.senderNameText);
        callingInProcess = (TextView) findViewById(R.id.callingInProcess);
        cancelCallButt = (ImageView) findViewById(R.id.cancelCallButt);

        // StartCall
        startCall();

        // On end
        cancelCallButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelCall();
                finish();
            }
        });

        UsersDatabase.database.getReference().child("Users")
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.child("calling").exists()) {
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        UsersDatabase.database.getReference().child("Users")
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("calling").exists()) {
                            if(snapshot.child("accepted").exists()) {
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void cancelCall() {
        UsersDatabase.database.getReference("Users")
                .child(currentUser.getUid())
                .child("calling")
                .setValue(null);

        UsersDatabase.database.getReference("Users")
                .child(otherUser.getId())
                .child("incomingCall")
                .setValue(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startCall() {

        UsersDatabase.database.getReference("Users")
                .child(currentUser.getUid())
                .child("calling")
                .setValue(otherUser.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                UsersDatabase.database.getReference("Users")
                        .child(otherUser.getId())
                        .child("incomingCall")
                        .setValue(currentUser.getUid());
            }
        });



        setParams();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setParams() {
        Glide
                .with(getApplicationContext())
                .load(otherUser.getImageUri())
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(receiverImage);

        receiverNameText.setText(otherUser.getUserName());
    }

}