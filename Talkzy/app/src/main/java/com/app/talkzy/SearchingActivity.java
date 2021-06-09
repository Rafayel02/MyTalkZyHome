package com.app.talkzy;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.talkzy.Adapter.UserAdapter;
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

public class SearchingActivity extends AppCompatActivity {

    private ImageView searchInUsers;
    private ImageView closeSearchPage;
    private EditText searchingEditText;
    private String usernameText;
    private boolean foundUserName = false;

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);

        searchingEditText = findViewById(R.id.searchingEditText);
        closeSearchPage = findViewById(R.id.closeSearchPage);
        searchInUsers = findViewById(R.id.searchInUsers);

        recyclerView = findViewById(R.id.recycler_view_new);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mUsers = new ArrayList<>();

        closeSearchPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchInUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameText = searchingEditText.getText().toString();
                if(!usernameText.isEmpty()) {
                    searchUser(usernameText);
                } else {
                    Toast.makeText(SearchingActivity.this, "Input username", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private void searchUser(String userName){
        foundUserName = false;
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
                            if(user.getUserName().contains(userName)) {
                                foundUserName = true;
                                mUsers.add(user);
                            }
                        }
                    }

                }
                if(foundUserName) {
                    userAdapter = new UserAdapter(SearchingActivity.this, mUsers, false);
                    recyclerView.setAdapter(userAdapter);
                } else {
                    Toast.makeText(SearchingActivity.this, "No user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}