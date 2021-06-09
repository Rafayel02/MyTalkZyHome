package com.app.talkzy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.talkzy.Adapter.MessageAdapter;
import com.app.talkzy.Fragment.APIService;
import com.app.talkzy.Notifications.Client;
import com.app.talkzy.Notifications.Data;
import com.app.talkzy.Notifications.MyResponse;
import com.app.talkzy.Notifications.Sender;
import com.app.talkzy.Notifications.Token;
import com.app.talkzy.VideoChat.IncomingCallActivity;
import com.app.talkzy.VideoChat.OutgoingCallActivity;
import com.app.talkzy.VideoChat.VideoCallActivity;
import com.app.talkzy.database.UsersDatabase;
import com.app.talkzy.OtherClasses.IntentChanger;
import com.app.talkzy.UserInfo.Chat;
import com.app.talkzy.UserInfo.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private ImageView videoCallButt;
    public static ImageView full_ImageView;
    public static ConstraintLayout messagesLayout;
    public static ConstraintLayout full_ImageViewLayout;
    public static MessageActivity messageActivity;

    CircleImageView profileImage;
    TextView userName;

    ImageButton btnSend;
    Button fileSend;
    EditText textSend;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerView;

    Intent intent;

    String userId;

    ImageView imgOn;
    ImageView imgOff;
    TextView onlineStatus;

    User user;

    APIService apiService;

    boolean notify = false;

    ValueEventListener seenListener;


    private Uri imageUri;
    private StorageTask uploadTask;
    StorageReference storageReference;


    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        messageActivity = this;
        full_ImageViewLayout = findViewById(R.id.fullViewLayout);
        messagesLayout = findViewById(R.id.messagesLayout);
        full_ImageViewLayout = findViewById(R.id.fullViewLayout);
        full_ImageView = findViewById(R.id.full_ImageView);
        try {
            endCall();
        } catch(Exception e) {}

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        videoCallButt = (ImageView) findViewById(R.id.appBarActionButton);
        videoCallButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OutgoingCallActivity.otherUser = user;
                IntentChanger.change(MessageActivity.this, OutgoingCallActivity.class, 0);
            }
        });

        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view_new);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.userName);
        btnSend = findViewById(R.id.btn_send);
        fileSend = findViewById(R.id.attach_file);
        textSend = findViewById(R.id.text_send);
        imgOn = findViewById(R.id.img_on);
        imgOff = findViewById(R.id.img_off);
        onlineStatus = findViewById(R.id.onlineStatus);

        intent = getIntent();

        userId = intent.getStringExtra("userId");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    user = snapshot.getValue(User.class);
                    userName.setText(user.getUserName());
                    if (user.getImageUri().equals("default")) {
                        profileImage.setImageResource(R.drawable.ic_person);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(user.getImageUri())
                                .into(profileImage);
                    }

                    profileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MessageActivity.this, ProfileActivity.class);
                            intent.putExtra("userId", user.getId());
                            startActivity(intent);
                        }
                    });

                    if (user.getStatus().equals("online")) {
                        imgOn.setVisibility(View.VISIBLE);
                        imgOff.setVisibility(View.GONE);
                        onlineStatus.setText("Online");
                        if(user.getTypingTo().equals(firebaseUser.getUid())){

                            onlineStatus.setText("Typing...");

                        }
                    } else {
                        imgOn.setVisibility(View.GONE);
                        imgOff.setVisibility(View.VISIBLE);
                        Date date = new Date();
                        int time = 0;
                        String timeString = "";
                        long differenceInMilliseconds = Long.valueOf(date.getTime()) - Long.valueOf(user.getLastSeen());

                        if((differenceInMilliseconds / 1000) < 60){
                            time = (int)(differenceInMilliseconds / 1000);
                            if(time > 1) {
                                timeString = "seconds";
                            }
                            else{
                                timeString = "second";
                            }
                        }
                        else if((differenceInMilliseconds / 1000 / 60) < 60){

                            time = (int)(differenceInMilliseconds / 1000 / 60);
                            if(time > 1) {
                                timeString = "minutes";
                            }
                            else{
                                timeString = "minute";
                            }

                        }
                        else if((differenceInMilliseconds / 1000  / 60 / 60) < 60){
                            time = (int)(differenceInMilliseconds / 1000  / 60 / 60);
                            if(time > 1) {
                                timeString = "hours";
                            }
                            else{
                                timeString = "hour";
                            }
                        }
                        else if((differenceInMilliseconds / 1000 / 60 / 60 / 24 )< 31){
                            time = (int)(differenceInMilliseconds / 1000  / 60 / 60 / 24);
                            if(time > 1) {
                                timeString = "days";
                            }
                            else{
                                timeString = "day";
                            }
                        }
                        else if((differenceInMilliseconds / 1000  / 60 / 60 / 24 / 30)< 12){
                            time = (int)(differenceInMilliseconds / 1000  / 60 / 60 / 24 / 30);
                            if(time > 1) {
                                timeString = "months";
                            }
                            else{
                                timeString = "month";
                            }
                        }
                        else{
                            time = (int)(differenceInMilliseconds / 1000  / 60 / 60 / 24 / 30 / 12);
                            if(time > 1) {
                                timeString = "years";
                            }
                            else{
                                timeString = "year";
                            }
                        }

                        onlineStatus.setText("Last seen " + time + " "+ timeString +" ago");



                    }

                    readMessage(firebaseUser.getUid(), userId, user.getImageUri());

                }
                catch(IllegalArgumentException e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = String.valueOf(textSend.getText());
                if(!msg.equals("")) {
                    sendMessage(firebaseUser.getUid(),userId,msg);
                }
                else {
                    Toast.makeText(MessageActivity.this, "You cant send empty message", Toast.LENGTH_SHORT).show();
                }
                textSend.setText("");
            }
        });


        fileSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(MessageActivity.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else{
                        pickImageFromGallery();
                    }
                }
                else{
                    pickImageFromGallery();
                }

            }
        });


        textSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                }
                else{
                    checkTypingStatus(userId);

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        seenMessage(userId);

        FirebaseUser currentUser = UsersDatabase.auth.getCurrentUser();
        UsersDatabase.database.getReference().child("Users")
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.child("calling").exists()) {
                            if(snapshot.child("accepted").exists()) {
                                VideoCallActivity.otherUserId = userId;
                                IntentChanger.change(MessageActivity.this, VideoCallActivity.class, 0);
                            }
                        }

                        if(snapshot.child("incomingCall").exists()) {
                            if(snapshot.child("accepted").exists()){
                                VideoCallActivity.otherUserId = userId;
                                IntentChanger.change(MessageActivity.this, VideoCallActivity.class, 0);
                            } else {
                                    IncomingCallActivity.otherUser = user;
                                    IntentChanger.change(MessageActivity.this, IncomingCallActivity.class, 0);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    public void pickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE);
    }

    private void endCall() {
        UsersDatabase.database.getReference().child("Users")
                .child(userId).child("accepted").setValue(null);
    }

    private void seenMessage(final String userId){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(firebaseUser.getUid().equals(chat.getReceiver()) && chat.getSender().equals(userId)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen",true);
                        snapshot.getRef().updateChildren(hashMap);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendFile(String sender, String receiver , String type, String link){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", link);
        hashMap.put("type", type);
        hashMap.put("sentTime", System.currentTimeMillis());
        hashMap.put("isSeen", false);

        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(userId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(userId)
                .child(firebaseUser.getUid());

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg = "Send you file";

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(notify == true) {
                    sendNotification(receiver, user.getUserName(), msg);
                }
                notify = false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage(String sender, String receiver , String message){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("type", "msg");
        hashMap.put("sentTime", System.currentTimeMillis());
        hashMap.put("isSeen", false);

        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(userId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(userId)
                .child(firebaseUser.getUid());

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(notify == true) {
                    sendNotification(receiver, user.getUserName(), msg);
                }
                notify = false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        int min = 1;
        int max = 1000000000;
        Random r = new Random();
        int i1 = r.nextInt(max - min + 1) + min;


        Notification notification = new Notification();
        notification.setId(i1);
        notification.setFromUserId(UsersDatabase.auth.getCurrentUser().getUid());
        notification.setNotificationType("message_sent");
        notification.setIsSeen(false);
        notification.setNotificationTime(System.currentTimeMillis());


        UsersDatabase.database.getReference("Notifications").child(userId).child(String.valueOf(i1)).setValue(notification);

    }

    private void sendNotification(String receiver, String username, String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_logo, username+": "+message, "New Message", userId);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(MessageActivity.this, "Failed!" , Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage(String myId, String userId , String imageUri){

        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Chat chat = snapshot.getValue(Chat.class);
                    if(myId.equals(chat.getReceiver()) && userId.equals(chat.getSender())
                            || userId.equals(chat.getReceiver()) && myId.equals(chat.getSender())){
                        mChat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageUri);

                    recyclerView.setAdapter(messageAdapter);
                    recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void checkTypingStatus(String typing){

        if(UsersDatabase.auth == null){
            UsersDatabase usersDatabase = new UsersDatabase();
            usersDatabase.connect();
            // usersDatabase.connectToGoogle();
        }
        if(UsersDatabase.auth.getUid() != null) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(UsersDatabase.auth.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("typingTo", typing);

            reference.updateChildren(hashMap);
        }

    }

    private void status(String status){
        if(UsersDatabase.auth == null){
            UsersDatabase usersDatabase = new UsersDatabase();
            usersDatabase.connect();
            //usersDatabase.connectToGoogle();
        }
        if(UsersDatabase.auth.getUid() != null) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(UsersDatabase.auth.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);

            reference.updateChildren(hashMap);
        }

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if(imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>(){

                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if(!task.isSuccessful()){
                        throw task.getException();
                    }


                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        sendFile(firebaseUser.getUid(),userId,"img",String.valueOf(downloadUri));
                        pd.dismiss();

                    }
                    else {
                        Toast.makeText(MessageActivity.this, "Failed!" , Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
        else {
            Toast.makeText(MessageActivity.this, "No Image Selected!" , Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        reference.removeEventListener(seenListener);
        super.onPause();
        status("offline");
        checkTypingStatus("noOne");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            imageUri = data.getData();
            if(uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getApplicationContext(), "Upload in progress" , Toast.LENGTH_SHORT).show();
            }
            else {
                uploadImage();
            }

            //img.setImageURI(imageUri);
        } else {
            Toast.makeText(MessageActivity.this, "Try Again!!", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        if(!MessageAdapter.zoomOut){
            finish();
        }
        else{
            MessageActivity.full_ImageViewLayout.setVisibility(View.GONE);
            MessageActivity.messagesLayout.setVisibility(View.VISIBLE);
            MessageAdapter.zoomOut = false;
        }
    }

}