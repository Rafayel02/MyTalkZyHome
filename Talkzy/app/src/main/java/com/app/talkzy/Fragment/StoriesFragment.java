package com.app.talkzy.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.talkzy.Adapter.StoryAdapter;
import com.app.talkzy.Adapter.UserAdapter;
import com.app.talkzy.Notifications.Token;
import com.app.talkzy.R;
import com.app.talkzy.Stories.Story;
import com.app.talkzy.UserInfo.ChatList;
import com.app.talkzy.UserInfo.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StoriesFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    private List<ChatList> usersList;

    public StoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stories, container, false);



        recyclerView_story = view.findViewById(R.id.recycler_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext() , LinearLayoutManager.HORIZONTAL, false);
        recyclerView_story.setLayoutManager(linearLayoutManager);

        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(),storyList);
        recyclerView_story.setAdapter(storyAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatList chatList = snapshot.getValue(ChatList.class);
                    usersList.add(chatList);
                }

                chatList();
                readStory();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("convosCount", dataSnapshot.getChildrenCount());
                FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());


        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);

    }

    private void chatList(){
        mUsers = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for(ChatList chatList: usersList){
                        if(user.getId().equals(chatList.getId())){
                            mUsers.add(user);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readStory(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timeCurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("",0,0,"",FirebaseAuth.getInstance().getCurrentUser().getUid()));

                for(User user : mUsers){
                    int countStory = 0;
                    Story story = null;
                    for(DataSnapshot snapshot : dataSnapshot.child(user.getId()).getChildren()){
                        story = snapshot.getValue(Story.class);
                        if(timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd()){
                            countStory++;
                        }
                    }
                    if( countStory > 0 ){
                        storyList.add(story);
                    }
                }

                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onResume() {
        ChatsStoriesFragment.appBarActionTextView.setText("Add to story");
        ChatsStoriesFragment.appBarActionButton.setImageResource(R.drawable.ic_add_photo);
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
