package com.app.talkzy.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.talkzy.Adapter.ChatUserAdapter;
import com.app.talkzy.Adapter.StoryAdapter;
import com.app.talkzy.Adapter.UserAdapter;
import com.app.talkzy.Adapter.UserHorizontalAdapter;
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

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView searchedChatRecyclerView;

    private ChatUserAdapter chatUserAdapter;
    private UserHorizontalAdapter userHorizontalAdapter;
    private List<User> mUsers;
    private List<User> mSearchedUsers;
    private List<User> mUsersOnline;

    private LinearLayout linearLayout;
    private EditText searchForChatEditText;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    DatabaseReference reference2;

    private RecyclerView recyclerView_userHorizontal;

    private List<ChatList> usersList;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_new);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchedChatRecyclerView = view.findViewById(R.id.searchedChatRecyclerView);
        searchedChatRecyclerView.setHasFixedSize(true);
        searchedChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView_userHorizontal = view.findViewById(R.id.recycler_view_story);
        recyclerView_userHorizontal.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext() , LinearLayoutManager.HORIZONTAL, false);
        recyclerView_userHorizontal.setLayoutManager(linearLayoutManager);

        linearLayout = view.findViewById(R.id.linearLayout);
        searchForChatEditText = view.findViewById(R.id.searchForChatEditText);


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
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("convosCount", dataSnapshot.getChildrenCount());
                FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        searchForChatEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if(String.valueOf(s).equals("")){
                    linearLayout.setVisibility(View.VISIBLE);
                    searchedChatRecyclerView.setVisibility(View.GONE);

                    chatList();

                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                System.out.println(s);
                linearLayout.setVisibility(View.GONE);
                searchedChatRecyclerView.setVisibility(View.VISIBLE);

                mSearchedUsers = new ArrayList<>();
                reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mSearchedUsers.clear();
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            User user = snapshot.getValue(User.class);
                            for(ChatList chatList: usersList){
                                if(user.getId().equals(chatList.getId())){
                                    if(user.getUserName().toLowerCase().contains(String.valueOf(s).toLowerCase())) {
                                        mSearchedUsers.add(user);
                                    }
                                }

                            }
                        }

                        chatUserAdapter = new ChatUserAdapter(getContext(), mSearchedUsers, true);
                        searchedChatRecyclerView.setAdapter(chatUserAdapter);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);

    }

    private void chatList(){
        mUsers = new ArrayList<>();
        mUsersOnline = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                mUsersOnline.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for(ChatList chatList: usersList){
                        if(user.getId().equals(chatList.getId())){
                            mUsers.add(user);
                            if(user.getStatus().equals("online")){
                                mUsersOnline.add(user);
                            }
                        }

                    }
                }

                chatUserAdapter = new ChatUserAdapter(getContext(), mUsers, true);
                userHorizontalAdapter = new UserHorizontalAdapter(getContext(), mUsersOnline);
                recyclerView.setAdapter(chatUserAdapter);
                recyclerView_userHorizontal.setAdapter(userHorizontalAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onResume() {
        ChatsStoriesFragment.appBarActionTextView.setText("Create a Group");
        ChatsStoriesFragment.appBarActionButton.setImageResource(R.drawable.ic_group_add);
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
