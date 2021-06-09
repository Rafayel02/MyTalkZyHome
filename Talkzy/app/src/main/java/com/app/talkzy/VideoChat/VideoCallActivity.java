package com.app.talkzy.VideoChat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.talkzy.R;
import com.app.talkzy.database.UsersDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import org.jetbrains.annotations.NotNull;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoCallActivity extends AppCompatActivity
        implements Session.SessionListener,
        PublisherKit.PublisherListener
{

    //6f2df82dbac030713389b460afae4a57bc21177a
    private static String API_KEY = "47247434";
    private static String SESSION_ID = "2_MX40NzI0NzQzNH5-MTYyMzAxMTcxNDQ4NX5DNVZVbUp4STlsNUE3eU9qOC82aHNhK0R-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NzI0NzQzNCZzaWc9NGY0MWQ3NDZmZTc3Y2U5MDBiMDU0ZGM0ZGZmZDYyOGY2MmY3NGE1NDpzZXNzaW9uX2lkPTJfTVg0ME56STBOelF6Tkg1LU1UWXlNekF4TVRjeE5EUTROWDVETlZaVmJVcDRTVGxzTlVFM2VVOXFPQzgyYUhOaEswUi1mZyZjcmVhdGVfdGltZT0xNjIzMDExNzcyJm5vbmNlPTAuNjE2NTQ5MDExMjQyNDA2MSZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjI1NjAzNzcxJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoCallActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private ImageView endCalButt;
    private ImageView switchButt;
    private DatabaseReference userRef;

    public static String otherUserId;
    public FirebaseUser currentUser;

    private FrameLayout mPubViewController;
    private FrameLayout mSubViewController;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscribe;

    private ImageView muteUnMuteButt;
    private boolean mute = false;

    private AudioManager audioManager;

    private CameraManager mManager;
    private String[] mCameraIds;

    private int camBackId;
    private int camFrontId;

    private Camera camera;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        muteUnMuteButt = (ImageView) findViewById(R.id.mute_unMute_butt);
        switchButt = (ImageView) findViewById(R.id.switch_butt);

        try {
            currentUser = UsersDatabase.auth.getCurrentUser();
        } catch(Exception e) {}
        muteUnMuteButt = (ImageView) findViewById(R.id.mute_unMute_butt);

        audioManager = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setMicrophoneMute(false);

        mManager = (CameraManager)getSystemService(getApplicationContext().CAMERA_SERVICE);
        try {
            mCameraIds = mManager.getCameraIdList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        camBackId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
        camFrontId = Camera.CameraInfo.CAMERA_FACING_FRONT;

        switchButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(VideoCallActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        });

        muteUnMuteButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mute) {
                    try {
                        muteUnMuteButt.setImageResource(R.drawable.ic_baseline_mic_24);
                        try {
                            audioManager.setMicrophoneMute(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch(Exception e) {}
                    mute = false;
                } else {
                    try {
                        muteUnMuteButt.setImageResource(R.drawable.ic_baseline_mic_off_24);
                        try {
                            audioManager.setMicrophoneMute(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch(Exception e) {}
                    mute = true;
                }
            }
        });

        UsersDatabase.database.getReference().child("Users")
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.child("accepted").exists()) {
                            if(mPublisher != null) {
                                mPublisher.destroy();
                            }

                            if(mSubscribe != null) {
                                mSubscribe.destroy();
                            }
                            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaa");
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        userRef = UsersDatabase.database.getReference();

        endCalButt = (ImageView) findViewById(R.id.close_video_chat_butt);
        endCalButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersDatabase.database.getReference().child("Users")
                        .child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        clearDataOnEndCall("accepted");
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });
        requestPermissions();
    }

    private void clearDataOnEndCall(String str) {
        System.out.println(otherUserId);
        userRef.child("Users")
                .child(currentUser.getUid())
                .child(str)
                .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                userRef.child("Users")
                        .child(otherUserId)
                        .child(str)
                        .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(mPublisher != null) {
                            mPublisher.destroy();
                        }
                        if(mSubscribe != null) {
                            mSubscribe.destroy();
                        }
                        System.out.println("kjhkjhjkhkjhkj");
                        finish();
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoCallActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {

        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if(EasyPermissions.hasPermissions(this, perms)) {
            mSubViewController = findViewById(R.id.sub_container);
            mPubViewController = findViewById(R.id.pub_container);

            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();

            mSession.setSessionListener(VideoCallActivity.this);

            mSession.connect(TOKEN);

        } else {
            EasyPermissions.requestPermissions(this, "Need Camera and Mic Permissions... ", RC_VIDEO_APP_PERM, perms);
        }

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {

        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoCallActivity.this);

        mPubViewController.addView(mPublisher.getView());

        if(mPublisher.getView() instanceof GLSurfaceView) {

            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);

        }

        mSession.publish(mPublisher);

    }

    @Override
    public void onDisconnected(Session session) {

        Log.i(LOG_TAG, "Stream Disconnected");

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

        Log.i(LOG_TAG, "Stream Received");
        if(mSubscribe == null) {
            mSubscribe = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscribe);
            mSubViewController.addView(mSubscribe.getView());
        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        Log.i(LOG_TAG, "Stream Dropped");

        if(mSubscribe != null) {

            mSubscribe = null;

            mSubViewController.removeAllViews();

        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
