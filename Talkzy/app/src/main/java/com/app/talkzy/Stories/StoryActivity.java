package com.app.talkzy.Stories;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.app.talkzy.R;
import com.app.talkzy.UserInfo.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    StoriesProgressView storiesProgressView;
    ImageView image, story_photo;
    TextView story_username;
    TextView story_time;

    LinearLayout r_seen;
    TextView seen_number;
    ImageView story_delete;

    List<String> images;
    List<String> storyIds;
    String userId;


    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        r_seen = findViewById(R.id.r_seen);
        seen_number = findViewById(R.id.seen_number);
        story_delete = findViewById(R.id.story_delete);


        storiesProgressView = findViewById(R.id.stories);

        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.story_image);
        story_photo = findViewById(R.id.profile_photo);
        story_username = findViewById(R.id.profile_username);
        story_time = findViewById(R.id.story_time);

        r_seen.setVisibility(View.GONE);
        story_delete.setVisibility(View.GONE);

        userId = getIntent().getStringExtra("userId");

        if(Objects.equals(userId, FirebaseAuth.getInstance().getCurrentUser().getUid())){
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }

        getStories(userId);
        userInfo(userId);

        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });

        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        story_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                        .child(userId).child(storyIds.get(counter));
                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(StoryActivity.this,"Deleted!",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });

        r_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoryActivity.this, StoryViewersActivity.class);
                intent.putExtra("userId",userId);
                intent.putExtra("storyId",storyIds.get(counter));
                startActivity(intent);
            }
        });



    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNext() {

        Glide.with(getApplicationContext()).load(images.get(++counter))
                .into(image);

        addView(storyIds.get(counter));
        seenNumber(storyIds.get(counter));
        timeAdd(storyIds.get(counter));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onPrev() {

        if((counter - 1) < 0) {
            return;
        }

        Glide.with(getApplicationContext()).load(images.get(--counter))
                .into(image);
        addView(storyIds.get(counter));
        seenNumber(storyIds.get(counter));
        timeAdd(storyIds.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }


    private void getStories(String userId){

        images = new ArrayList<>();
        storyIds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                images.clear();
                storyIds.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    long timeCurrent = System.currentTimeMillis();
                    if(timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd()){
                        images.add(story.getImageURL());
                        storyIds.add(story.getStoryId());
                        timeAdd(storyIds.get(counter));
                    }

                }

                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);

                Glide.with(getApplicationContext()).load(images.get(counter))
                        .into(image);


                addView(storyIds.get(counter));
                seenNumber(storyIds.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void userInfo(String userId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);

                    Glide.with(getApplicationContext()).load(user.getImageUri())
                        .into(story_photo);
                    story_username.setText(user.getUserName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void addView(String storyId){
        if(!Objects.equals(userId,FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            FirebaseDatabase.getInstance().getReference("Story").child(userId)
                    .child(storyId).child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
        }
    }

    private void seenNumber(String storyId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").
                child(userId).child(storyId).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seen_number.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void timeAdd(String storyId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").
                child(userId).child(storyId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Story story = snapshot.getValue(Story.class);
                long timeInSeconds = (System.currentTimeMillis() - story.getTimeStart()) / 1000;
                String time = timeInSeconds + "s";
                if(timeInSeconds > 60 && timeInSeconds / 60 < 60){
                    time = timeInSeconds / 60 + "m";
                }
                else if(timeInSeconds / 60 > 60 && timeInSeconds / 60 / 60 < 60){
                    time = timeInSeconds / 60 / 60 + "h";
                }
                story_time.setText(time);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}