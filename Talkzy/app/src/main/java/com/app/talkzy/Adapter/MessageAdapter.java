package com.app.talkzy.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.app.talkzy.MainActivity;
import com.app.talkzy.MessageActivity;
import com.app.talkzy.R;
import com.app.talkzy.UserInfo.Chat;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_RIGHT_IMG = 2;
    public static final int MSG_TYPE_LEFT_IMG = 3;
    public static boolean zoomOut = false;

    private Context mContext;
    private List<Chat> mChat;
    private String imageUri;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageUri){
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageUri = imageUri;


    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent , false);
            return new MessageAdapter.ViewHolder(view);
        }
        else if(viewType == MSG_TYPE_RIGHT_IMG){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right_img, parent , false);
            return new MessageAdapter.ViewHolder(view);
        }
        else if(viewType == MSG_TYPE_LEFT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent , false);
            return new MessageAdapter.ViewHolder(view);

        }
        else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left_img, parent , false);
            return new MessageAdapter.ViewHolder(view);
        }



    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = mChat.get(position);
        if(chat.getType().equals("img")){
            try{
                Glide
                        .with(MessageActivity.messageActivity)
                        .load(chat.getMessage())
                        .centerCrop()
                        .placeholder(R.drawable.loading_gif)
                        .into(holder.image);

                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("ESHHH");
                        try {
                            if (zoomOut) {
                                MessageActivity.full_ImageViewLayout.setVisibility(View.GONE);
                                MessageActivity.messagesLayout.setVisibility(View.VISIBLE);
                                zoomOut = false;
                            } else {
                                MessageActivity.full_ImageViewLayout.setVisibility(View.VISIBLE);
                                MessageActivity.messagesLayout.setVisibility(View.GONE);
                                Glide
                                        .with(MessageActivity.messageActivity)
                                        .load(chat.getMessage())
                                        .centerCrop()
                                        .placeholder(R.drawable.loading_gif)
                                        .into(MessageActivity.full_ImageView);


                                zoomOut = true;
                            }
                        } catch(Exception e) {
                            e.printStackTrace();

                        }
                    }
                });
            }
            catch (Exception e){
                System.err.println("Image view of chat item right not found");
            }
        }
        else if(chat.getType().equals("msg")){
            holder.show_message.setText(chat.getMessage());
        }


        if(imageUri.equals("default")){
            holder.profile_image.setImageResource(R.drawable.ic_person);
        }
        else{
            Glide.with(mContext)
                    .load(imageUri)
                    .into(holder.profile_image);
        }

        if(position == mChat.size()-1){
            if (chat.getIsSeen()){
                holder.txt_seen.setText("Seen");
            }
            else {
                holder.txt_seen.setText("Delivered");
            }
        }
        else {
            holder.txt_seen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        public ImageView profile_image;
        public ImageView image;
        public ImageView full_ImageView;
        public TextView txt_seen;



        public ViewHolder(View itemView){
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            try{
                image = itemView.findViewById(R.id.image);
            }
            catch (Exception e){
                System.err.println("Image view of chat item right not found");
            }


        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(firebaseUser.getUid())){
            if(mChat.get(position).getType().equals("img")){
                return MSG_TYPE_RIGHT_IMG;
            }
            return MSG_TYPE_RIGHT;
        }
        else{
            if(mChat.get(position).getType().equals("img")){
                return MSG_TYPE_LEFT_IMG;
            }
            return MSG_TYPE_LEFT;
        }
    }
}
