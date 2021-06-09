package com.app.talkzy.Stories;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.talkzy.MainActivity;
import com.app.talkzy.Notification;
import com.app.talkzy.R;
import com.app.talkzy.database.UsersDatabase;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Random;

public class AddStoryActivity extends AppCompatActivity {

    private Uri mImageUri;
    String myUrl = "";
    private StorageTask storageTask;
    StorageReference storageReference;
    private Uri imageUri;
    private StorageTask uploadTask;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storageReference = FirebaseStorage.getInstance().getReference("story");
        pickImageFromGallery();


    }

    public void pickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery();
                }
                else{
                    Toast.makeText(MainActivity.mainActivity, "Permission denied...." , Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            mImageUri = data.getData();

            if(uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getApplicationContext(), "Upload in progress" , Toast.LENGTH_SHORT).show();

            }
            else {
                publishStory();

            }

        } else {
            Toast.makeText(MainActivity.mainActivity, "Try Again!!", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void publishStory(){
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();

        if(mImageUri != null){
            StorageReference imageReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));

            storageTask = imageReference.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }

                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                                .child(myId);
                        String storyId = reference.push().getKey();
                        long timeEnd = System.currentTimeMillis()+86400000; // 1 day

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageURL", myUrl);
                        hashMap.put("timeStart", ServerValue.TIMESTAMP);
                        hashMap.put("timeEnd", timeEnd);
                        hashMap.put("storyId", storyId);
                        hashMap.put("userId", myId);

                        reference.child(storyId).setValue(hashMap);
                        pd.dismiss();


                        int min = 1;
                        int max = 1000000000;
                        Random r = new Random();
                        int i1 = r.nextInt(max - min + 1) + min;


                        Notification notification = new Notification();
                        notification.setId(i1);
                        notification.setFromUserId(UsersDatabase.auth.getCurrentUser().getUid());
                        notification.setNotificationType("story_added");
                        notification.setIsSeen(false);
                        notification.setNotificationTime(System.currentTimeMillis());


                        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    UsersDatabase.database.getReference("Notifications").child(snapshot.getKey()).child(String.valueOf(i1)).setValue(notification);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        finish();
                    }
                    else{
                        Toast.makeText(AddStoryActivity.this,"Faild to add story", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
        else {
            Toast.makeText(this,"No image selected", Toast.LENGTH_SHORT).show();
        }
    }


}