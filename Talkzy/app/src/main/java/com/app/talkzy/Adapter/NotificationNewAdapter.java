package com.app.talkzy.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.app.talkzy.MessageActivity;
import com.app.talkzy.Notification;
import com.app.talkzy.R;
import com.app.talkzy.Stories.StoryActivity;
import com.app.talkzy.database.UsersDatabase;
import com.app.talkzy.UserInfo.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NotificationNewAdapter extends RecyclerView.Adapter<NotificationNewAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> mNotifications;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;

    public NotificationNewAdapter(Context mContext, List<Notification> mNotifications){
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @SuppressLint("ResourceAsColor")
    @NonNull
    @Override
    public NotificationNewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent , false);
        view.setBackgroundColor(Color.parseColor("#51F92B52"));
        return new NotificationNewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationNewAdapter.ViewHolder holder, int position) {



        Notification notification = mNotifications.get(position);


        if(notification.getFromUserId() != null) {
            if(notification.getNotificationType().equals("friend_req")) {

                onFriendReq(notification,holder);

            }
            else if(notification.getNotificationType().equals("story_added")){

                onStoryAdded(notification,holder);

            }
            else if(notification.getNotificationType().equals("message_sent")){
                onMessageSent(notification,holder);
            }

        }

    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView notificationBody;
        public TextView notificationTime;
        public ImageView profile_image;
        public Button acceptButton;
        public Button deleteButton;
        public Button actionButton;

        public ViewHolder(View itemView){
            super(itemView);

            notificationBody = itemView.findViewById(R.id.notificationBody);
            notificationTime = itemView.findViewById(R.id.notificationTime);
            profile_image = itemView.findViewById(R.id.profile_image);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            actionButton = itemView.findViewById(R.id.actionButton);


        }
    }

    public void onMessageSent(Notification notification, NotificationNewAdapter.ViewHolder holder){

        holder.acceptButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        holder.notificationBody.setVisibility(View.VISIBLE);
        holder.actionButton.setVisibility(View.VISIBLE);


        long timeInSeconds = (System.currentTimeMillis() - notification.getNotificationTime()) / 1000;
        String time = timeInSeconds + "s";
        if(timeInSeconds > 60 && timeInSeconds / 60 < 60){
            time = timeInSeconds / 60 + "m";
        }
        else if(timeInSeconds / 60 > 60 && timeInSeconds / 60 / 60 < 60){
            time = timeInSeconds / 60 / 60 + "h";
        }
        else if(timeInSeconds / 60 / 60 > 60 && timeInSeconds / 60 / 60 / 60 < 60){
            time = timeInSeconds / 60 / 60 / 60 + " day(s)";

        }

        holder.notificationTime.setText(time);
        holder.actionButton.setText("Reply");


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(notification.getFromUserId());
        final User[] user1 = {new User()};

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User[] user = {dataSnapshot.getValue(User.class)};
                user1[0] = user[0];
                mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user[0].getId());
                String userFirstName = "";
                if(user[0].getFullName().contains(" ")){
                    userFirstName = user[0].getFullName().substring(0, user[0].getFullName().indexOf(" "));
                }
                else{
                    userFirstName = user[0].getFullName();
                }
                holder.notificationBody.setText(userFirstName + " sent you a message");
                if (user[0].getImageUri().equals("default")) {
                    holder.profile_image.setImageResource(R.drawable.ic_person);
                } else {
                    Glide
                            .with(mContext)
                            .load(user[0].getImageUri())
                            .into(holder.profile_image)
                    ;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId", user1[0].getId());
                mContext.startActivity(intent);


            }
        });

    }

    public void onStoryAdded(Notification notification, NotificationNewAdapter.ViewHolder holder){

        holder.acceptButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        holder.notificationBody.setVisibility(View.VISIBLE);
        holder.actionButton.setVisibility(View.VISIBLE);


        long timeInSeconds = (System.currentTimeMillis() - notification.getNotificationTime()) / 1000;
        String time = timeInSeconds + "s";
        if(timeInSeconds > 60 && timeInSeconds / 60 < 60){
            time = timeInSeconds / 60 + "m";
        }
        else if(timeInSeconds / 60 > 60 && timeInSeconds / 60 / 60 < 60){
            time = timeInSeconds / 60 / 60 + "h";
        }
        else if(timeInSeconds / 60 / 60 > 60 && timeInSeconds / 60 / 60 / 60 < 60){
            time = timeInSeconds / 60 / 60 / 60 + " day(s)";

        }

        final String finalTime = time;
        holder.notificationTime.setText(time);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(notification.getFromUserId());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User[] user = {dataSnapshot.getValue(User.class)};
                mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user[0].getId());
                mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
                mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
                String userFirstName = "";
                if(user[0].getFullName().contains(" ")){
                    userFirstName = user[0].getFullName().substring(0, user[0].getFullName().indexOf(" "));
                }
                else{
                    userFirstName = user[0].getFullName();
                }
                holder.notificationBody.setText(userFirstName + " added story");
                if (user[0].getImageUri().equals("default")) {
                    holder.profile_image.setImageResource(R.drawable.ic_person);
                } else {
                    Glide
                            .with(mContext)
                            .load(user[0].getImageUri())
                            .into(holder.profile_image)
                    ;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(!finalTime.contains("d")) {
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userId", notification.getFromUserId());
                    mContext.startActivity(intent);
                }
                else{
                    Toast.makeText(mContext,"This Stoy is no longer availabale Sorry",Toast.LENGTH_SHORT).show();
                }

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", notification.getId());
                hashMap.put("fromUserId", notification.getFromUserId());
                hashMap.put("notificationType", notification.getNotificationType());
                hashMap.put("isSeen", true);
                hashMap.put("notificationTime", notification.getNotificationTime());

                UsersDatabase.database.getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(String.valueOf(notification.getId())).updateChildren(hashMap);



            }
        });

    }

    public void onFriendReq(Notification notification, NotificationNewAdapter.ViewHolder holder){


        holder.actionButton.setVisibility(View.GONE);

        long timeInSeconds = (System.currentTimeMillis() - notification.getNotificationTime()) / 1000;
        String time = timeInSeconds + "s";
        if(timeInSeconds > 60 && timeInSeconds / 60 < 60){
            time = timeInSeconds / 60 + "m";
        }
        else if(timeInSeconds / 60 > 60 && timeInSeconds / 60 / 60 < 60){
            time = timeInSeconds / 60 / 60 + "h";
        }
        else if(timeInSeconds / 60 / 60 > 60 && timeInSeconds / 60 / 60 / 60 < 60){
            time = timeInSeconds / 60 / 60 / 60 + " day(s)";

        }

        holder.notificationTime.setText(time);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(notification.getFromUserId());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                final User[] user = {dataSnapshot.getValue(User.class)};
                mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user[0].getId());
                mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
                mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

                String userFirstName = "";
                if(user[0].getFullName().contains(" ")){
                    userFirstName = user[0].getFullName().substring(0, user[0].getFullName().indexOf(" "));
                }
                else{
                    userFirstName = user[0].getFullName();
                }
                holder.notificationBody.setText(userFirstName + " sent you friend request");
                if (user[0].getImageUri().equals("default")) {
                    holder.profile_image.setImageResource(R.drawable.ic_person);
                } else {
                    Glide
                            .with(mContext)
                            .load(user[0].getImageUri())
                            .into(holder.profile_image)
                    ;
                }
                if (notification.getNotificationType().equals("friend_req")) {

                    holder.acceptButton.setVisibility(View.VISIBLE);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                    holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                            mFriendDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).child(user[0].getId()).setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendDatabase.child(user[0].getId()).child(UsersDatabase.auth.getCurrentUser().getUid()).setValue(currentDate)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            mFriendReqDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).child(user[0].getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    mFriendReqDatabase.child(user[0].getId()).child(UsersDatabase.auth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {

                                                                            UsersDatabase.database.getReference("Users").child(UsersDatabase.auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                                    user[0] = snapshot.getValue(User.class);
                                                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                                                    hashMap.put("friendsCount", user[0].getFriendsCount() + 1);

                                                                                    UsersDatabase.database.getReference("Users").child(UsersDatabase.auth.getUid()).updateChildren(hashMap);

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                }
                                                                            });

                                                                            UsersDatabase.database.getReference("Users").child(user[0].getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                                    user[0] = snapshot.getValue(User.class);
                                                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                                                    hashMap.put("friendsCount", user[0].getFriendsCount() + 1);

                                                                                    UsersDatabase.database.getReference("Users").child(user[0].getId()).updateChildren(hashMap);

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                }
                                                                            });

                                                                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(UsersDatabase.auth.getCurrentUser().getUid());
                                                                            final User userInNotification = user[0];

                                                                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                    int key = -1;
                                                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                                        Notification notification = snapshot.getValue(Notification.class);
                                                                                        if (notification.getFromUserId().equals(userInNotification.getId()) && notification.getNotificationType().equals("friend_req")) {
                                                                                            key = Integer.parseInt(snapshot.getKey());
                                                                                        }
                                                                                    }

                                                                                    if (key != -1) {
                                                                                        FirebaseDatabase.getInstance().getReference("Notifications").child(UsersDatabase.auth.getCurrentUser().getUid()).child(String.valueOf(key)).removeValue();
                                                                                    }

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                }
                                                                            });


                                                                        }
                                                                    });

                                                                }
                                                            });

                                                        }
                                                    });
                                        }
                                    });

                        }
                    });
                    holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mFriendReqDatabase.child(UsersDatabase.auth.getCurrentUser().getUid()).child(user[0].getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendReqDatabase.child(user[0].getId()).child(UsersDatabase.auth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(UsersDatabase.auth.getCurrentUser().getUid());
                                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    int key = -1;
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        Notification notification = snapshot.getValue(Notification.class);

                                                        if (notification.getFromUserId().equals(user[0].getId()) && notification.getNotificationType().equals("friend_req")) {
                                                            key = Integer.parseInt(snapshot.getKey());
                                                        }
                                                    }

                                                    if (key != -1) {
                                                        FirebaseDatabase.getInstance().getReference("Notifications").child(UsersDatabase.auth.getCurrentUser().getUid()).child(String.valueOf(key)).removeValue();
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });


                                        }
                                    });

                                }
                            });

                        }
                    });

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}