package com.app.talkzy.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.talkzy.R;
import com.app.talkzy.Stories.AddStoryActivity;
import com.app.talkzy.Stories.Story;
import com.app.talkzy.Stories.StoryActivity;
import com.app.talkzy.UserInfo.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserHorizontalAdapter extends RecyclerView.Adapter<UserHorizontalAdapter.ViewHolder>{

    private Context mContext;
    private List<User> mUser;

    public UserHorizontalAdapter(Context mContext, List<User> mUser) {
        this.mContext = mContext;
        this.mUser = mUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {


            View view = LayoutInflater.from(mContext).inflate(R.layout.user_horizontal_item,viewGroup, false);
            return new UserHorizontalAdapter.ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        User user = mUser.get(i);


        userInfo(viewHolder, user.getId(), i);


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on Image Click
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView profile_photo;
        public TextView story_username;

        public ViewHolder(View itemView){
            super(itemView);

            profile_photo = itemView.findViewById(R.id.profile_photo);
            story_username = itemView.findViewById(R.id.profile_username);

        }

    }


    private void userInfo(final ViewHolder viewHolder, String userId, int pos){


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user.getImageUri().equals("default")){
                    try {
                        Glide.with(mContext).load(R.drawable.ic_baseline_person_24).into(viewHolder.profile_photo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Glide.with(mContext).load(user.getImageUri()).into(viewHolder.profile_photo);
                }
                viewHolder.story_username.setText(user.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}
