package com.app.talkzy.Fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.talkzy.Adapter.NotificationNewAdapter;
import com.app.talkzy.Adapter.NotificationOldAdapter;
import com.app.talkzy.Notification;
import com.app.talkzy.R;
import com.app.talkzy.database.UsersDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private TextView newNotifCountTextView;
    private Button markAllAsReadButton;
    private RecyclerView recyclerViewNew;

    private NotificationNewAdapter notificationNewAdapter;
    private List<Notification> mNewNotifications;

    private RecyclerView recyclerViewOld;

    private NotificationOldAdapter notificationOldAdapter;
    private List<Notification> mOldNotifications;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        markAllAsReadButton = view.findViewById(R.id.markAsRead);
        newNotifCountTextView = view.findViewById(R.id.newNotifCount);

        recyclerViewNew = view.findViewById(R.id.recycler_view_new);
        recyclerViewNew.setHasFixedSize(true);
        recyclerViewNew.setLayoutManager(new LinearLayoutManager(getContext()));


        recyclerViewOld = view.findViewById(R.id.recycler_view_old);
        recyclerViewOld.setHasFixedSize(true);
        recyclerViewOld.setLayoutManager(new LinearLayoutManager(getContext()));


        mNewNotifications = new ArrayList<>();
        mOldNotifications = new ArrayList<>();
        readNotifications();

        markAllAsReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(Notification notification : mNewNotifications) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id", notification.getId());
                    hashMap.put("fromUserId", notification.getFromUserId());
                    hashMap.put("notificationType", notification.getNotificationType());
                    hashMap.put("isSeen", true);
                    hashMap.put("notificationTime", notification.getNotificationTime());

                    UsersDatabase.database.getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(String.valueOf(notification.getId())).updateChildren(hashMap);
                }

                readNotifications();

            }
        });


        return view;
    }

    public void readNotifications(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mNewNotifications.clear();
                mOldNotifications.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Notification notification = snapshot.getValue(Notification.class);
                    if(notification.getIsSeen()){
                        mOldNotifications.add(notification);
                    }
                    else{
                        mNewNotifications.add(notification);
                    }
                }


                newNotifCountTextView.setText("("  + mNewNotifications.size() + ")");
                notificationNewAdapter = new NotificationNewAdapter(getContext(), mNewNotifications);
                notificationOldAdapter = new NotificationOldAdapter(getContext(), mOldNotifications);

                recyclerViewNew.setAdapter(notificationNewAdapter);
                recyclerViewOld.setAdapter(notificationOldAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}