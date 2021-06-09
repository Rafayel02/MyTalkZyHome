package com.app.talkzy;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.app.talkzy.Adapter.ViewPagerAdapter;
import com.app.talkzy.Fragment.ChatsStoriesFragment;
import com.app.talkzy.Fragment.MapFragment;
import com.app.talkzy.Fragment.MeetFragment;
import com.app.talkzy.Fragment.NotificationsFragment;
import com.app.talkzy.Fragment.ProfileFragment;
import com.app.talkzy.database.UsersDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    private ViewPager2 viewPager2;

    public static MainActivity mainActivity;

    DatabaseReference reference;

    MapFragment mapFragment;
    ChatsStoriesFragment chatsStoriesFragment;
    MeetFragment meetFragment;
    NotificationsFragment notificationsFragment;
    ProfileFragment profileFragment;

    //public static FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        viewPager2 = findViewById(R.id.viewpager2);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        viewPager2.setOffscreenPageLimit(5);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_map:
                                bottomNavigationView.getMenu().findItem(R.id.menu_map).setChecked(true);
                                viewPager2.setCurrentItem(0,false);
                                break;
                            case R.id.menu_stories_chats:
                                bottomNavigationView.getMenu().findItem(R.id.menu_stories_chats).setChecked(true);
                                viewPager2.setCurrentItem(1,false);
                                break;
                            case R.id.menu_meet:
                                bottomNavigationView.getMenu().findItem(R.id.menu_meet).setChecked(true);
                                viewPager2.setCurrentItem(2,false);
                                break;
                            case R.id.menu_notifications:
                                bottomNavigationView.getMenu().findItem(R.id.menu_notifications).setChecked(true);
                                viewPager2.setCurrentItem(3,false);
                                break;
                            case R.id.menu_profile:
                                bottomNavigationView.getMenu().findItem(R.id.menu_profile).setChecked(true);
                                viewPager2.setCurrentItem(4,false);
                                break;
                        }
                        return false;
                    }
                });



        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                switch (position) {
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.menu_map).setChecked(true);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.menu_stories_chats).setChecked(true);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.menu_meet).setChecked(true);
                        break;
                    case 3:
                        bottomNavigationView.getMenu().findItem(R.id.menu_notifications).setChecked(true);
                        break;
                    case 4:
                        bottomNavigationView.getMenu().findItem(R.id.menu_profile).setChecked(true);
                        break;
                }
            }
        });

        // for disabling swiping
        viewPager2.setUserInputEnabled(false);

        setupViewPager(viewPager2);
        bottomNavigationView.setSelectedItemId(R.id.menu_profile);

    }

    private void setupViewPager(ViewPager2 viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());

        mapFragment = new MapFragment();
        chatsStoriesFragment = new ChatsStoriesFragment();
        meetFragment = new MeetFragment();
        notificationsFragment = new NotificationsFragment();
        profileFragment = new ProfileFragment();

        adapter.addFragment(mapFragment);
        adapter.addFragment(chatsStoriesFragment);
        adapter.addFragment(meetFragment);
        adapter.addFragment(notificationsFragment);
        adapter.addFragment(profileFragment);
        viewPager.setAdapter(adapter);
    }

    private void status(String status){

        Date date = new Date();

        try {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(UsersDatabase.auth.getCurrentUser().getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            if (status.equals("offline")) {
                hashMap.put("lastSeen", date.getTime());
            }
            reference.updateChildren(hashMap);

            if (ProfileFragment.isLogoutPressed == true) {
                UsersDatabase.auth.signOut();
                ProfileFragment.isLogoutPressed = false;
            }
        } catch (Exception e) {

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
        UsersDatabase.database.getReference().child("Users")
                .child(UsersDatabase.auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot response : snapshot.child("sendingMeetRequest").getChildren()) {
                            for(DataSnapshot resp: snapshot.child("sendingMeetRequest").getChildren()) {
                                if(!Objects.equals(resp.getKey(), "tempOtherId")) {
                                    UsersDatabase.database.getReference("Users")
                                            .child(resp.getKey())
                                            .child("gettingMeetRequest")
                                            .child(UsersDatabase.auth.getUid()).setValue(null);
                                }
                            }
                        }
                        UsersDatabase.clearDangerData();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
        UsersDatabase.clearDangerData();
        MeetFragment.closePopUp();
        status("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}