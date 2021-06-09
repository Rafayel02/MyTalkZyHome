package com.app.talkzy.Stories;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;

import com.app.talkzy.Adapter.UserAdapter;
import com.app.talkzy.R;
import com.app.talkzy.UserInfo.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StoryViewersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;
    private List<String> viewersIds;

    String userId;
    String storyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_viewers);

        recyclerView = findViewById(R.id.recycler_view_new);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        userId = getIntent().getStringExtra("userId");
        storyId = getIntent().getStringExtra("storyId");

        mUsers = new ArrayList<>();
        viewersIds = new ArrayList<>();
        getViewersIds();
    }

    private void getViewersIds(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyId).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String id = snapshot.getKey();

                    if (!id.equals(firebaseUser.getUid())) {
                        viewersIds.add(id);
                    }
                }
                readUsers();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void readUsers(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    if (!user.getId().equals(firebaseUser.getUid())) {
                        if(user.getIsVerified()) {
                            for(String id : viewersIds){
                                if(Objects.equals(id,user.getId())){
                                    mUsers.add(user);
                                }
                            }
                        }
                    }
                }
                userAdapter = new UserAdapter(getApplicationContext(), mUsers, false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}