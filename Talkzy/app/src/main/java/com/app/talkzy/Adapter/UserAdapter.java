package com.app.talkzy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.app.talkzy.MessageActivity;
import com.app.talkzy.ProfileActivity;
import com.app.talkzy.R;
import com.app.talkzy.UserInfo.Chat;
import com.app.talkzy.UserInfo.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;
    private String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat){
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent , false);

        return new UserAdapter.ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        User user = mUsers.get(position);
        holder.username.setText(user.getUserName());
        if (Objects.equals(user.getImageUri(),"default")){
            holder.profile_image.setImageResource(R.drawable.ic_person);
        }
        else{
            Glide
                    .with(mContext)
                    .load(user.getImageUri())
                    .into(holder.profile_image)
            ;
        }

        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra("userId", user.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(intent);
            }
        });

        if(isChat){

            lastMessage(user.getId(), holder.last_msg);

            if(user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
            }
            else{
                holder.img_on.setVisibility(View.GONE);
            }
        }
        else {

            holder.last_msg.setVisibility(View.GONE);
            holder.img_on.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId", user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        private ImageView img_on;
        private TextView last_msg;

        public ViewHolder(View itemView){
            super(itemView);

            username = itemView.findViewById(R.id.userName);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            last_msg = itemView.findViewById(R.id.notificationBody);

        }
    }

    private void lastMessage(final  String userId, final TextView last_msg){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    try {
                        if ((chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)) && chat.getType().equals("msg")) {
                            theLastMessage = chat.getMessage();
                        } else if (chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid()) && chat.getType().equals("msg")) {
                            theLastMessage = "You: " + chat.getMessage();
                        } else if ((chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)) && chat.getType().equals("img")) {
                            theLastMessage = "Sent image";
                        } else if ((chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) && chat.getType().equals("img")) {
                            theLastMessage = "You: Sent image";
                        }
                    }
                    catch (Exception e){

                    }
                }


                switch (theLastMessage){
                    case "default":
                        last_msg.setText("No Message");
                        break;
                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
