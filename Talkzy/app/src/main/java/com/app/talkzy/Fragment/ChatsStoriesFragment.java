package com.app.talkzy.Fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.talkzy.Adapter.StoriesChatsFragmentAdapter;
import com.app.talkzy.MainActivity;
import com.app.talkzy.R;
import com.app.talkzy.UserInfo.User;
import com.app.talkzy.database.UsersDatabase;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsStoriesFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager2 pager2;
    CircleImageView profileImage;
    public static ImageView appBarActionButton;
    TextView userNameTextView;
    public static TextView appBarActionTextView;
    StoriesChatsFragmentAdapter adapter;

    public ChatsStoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stories_chats, container, false);



        profileImage = view.findViewById(R.id.profile_image);
        userNameTextView = view.findViewById(R.id.userName);
        appBarActionTextView = view.findViewById(R.id.appBarActionTextView);
        appBarActionButton = view.findViewById(R.id.appBarActionButton);
        tabLayout = view.findViewById(R.id.tab_layout);
        pager2 = view.findViewById(R.id.view_pager2);

        FragmentManager fm = getFragmentManager();
        adapter = new StoriesChatsFragmentAdapter(fm, getLifecycle());
        pager2.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Chats"));
        tabLayout.addTab(tabLayout.newTab().setText("Stories"));
        // for disabling swiping
        pager2.setUserInputEnabled(false);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        FirebaseDatabase.getInstance().getReference("Users").child(UsersDatabase.auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    User user = snapshot.getValue(User.class);
                    userNameTextView.setText(user.getUserName());
                    if (user.getImageUri().equals("default")) {
                        profileImage.setImageResource(R.drawable.ic_person);
                    } else {
                        Glide.with(MainActivity.mainActivity)
                                .load(user.getImageUri())
                                .into(profileImage);
                    }

                    profileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            Intent intent = new Intent(MessageActivity.this, ProfileActivity.class);
//                            intent.putExtra("userId", user.getId());
//                            startActivity(intent);
                        }
                    });

                }
                catch(IllegalArgumentException e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }


    @Override
    public void onResume() {
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