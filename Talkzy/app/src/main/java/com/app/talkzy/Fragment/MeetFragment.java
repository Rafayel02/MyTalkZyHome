package com.app.talkzy.Fragment;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.talkzy.Adapter.UserAdapter;
import com.app.talkzy.MainActivity;
import com.app.talkzy.OtherClasses.IntentChanger;
import com.app.talkzy.R;
import com.app.talkzy.UserInfo.User;
import com.app.talkzy.VideoChat.VideoCallActivity;
import com.app.talkzy.database.UsersDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class MeetFragment extends Fragment {

    private static int counter = 0;

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;

    private TextView genderTextShow;
    private TextView locationTextShow;
    private Button filterButt;
    private static ArrayList<String> lastUsersIDs = new ArrayList<>();

    private Spinner genderSpinner;
    private Spinner continentSpinner;
    private String filterGender;
    private String filterContinent;

    //pop up tools
    private static Dialog myDialog;
    private Button closePopUpButt;
    private TextView popUpText;
    private RelativeLayout loading;

    public MeetFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meet, container, false);

        //Pop up tools
        myDialog = new Dialog(MainActivity.mainActivity);

        genderTextShow = view.findViewById(R.id.genderTextShow);
        locationTextShow = view.findViewById(R.id.countryTextShow);
        filterButt = view.findViewById(R.id.filterButton);

        genderSpinner = view.findViewById(R.id.genderSpinner);
        genderSpinner.setSelection(0);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(MainActivity.mainActivity,
                R.array.gender_array_all,  android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterGender = String.valueOf(genderSpinner.getSelectedItem());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ////// continents ////
        continentSpinner = view.findViewById(R.id.continentSpinner);
        continentSpinner.setSelection(0);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.mainActivity,
                R.array.continents,  android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        continentSpinner.setAdapter(adapter);
        continentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterContinent = String.valueOf(continentSpinner.getSelectedItem());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        filterButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUp(view);
                sendMeetRequest();
            }
        });

        recyclerView = view.findViewById(R.id.recycler_view_new);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        try {
            UsersDatabase.database.getReference().child("Users")
                    .child(UsersDatabase.auth.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.child("gettingMeetRequest").exists()) {
                                if(snapshot.child("sendingMeetRequest").exists()) {
                                    OUTER_LOOP:for (DataSnapshot request : snapshot.child("gettingMeetRequest").getChildren()) {
                                        for (DataSnapshot response : snapshot.child("sendingMeetRequest").getChildren()) {
                                            if(Objects.equals(request.getKey(), response.getKey())) {
                                                String tempOtherId = response.getKey();
                                                lastUsersIDs.add(tempOtherId);
                                                clearRequestDataAndMakeThemMeet(snapshot, tempOtherId);
                                                closePopUp();
                                                openMeetLayout();
                                                break OUTER_LOOP;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void openMeetLayout() {
        IntentChanger.change(MainActivity.mainActivity, VideoCallActivity.class, 0);
    }

    private void showPopUp(View v) {
        myDialog.setContentView(R.layout.activity_meet_pop_up);
        closePopUpButt = myDialog.findViewById(R.id.closePopUpButt);
        popUpText = myDialog.findViewById(R.id.popUpText);
        popUpText.setText("Searching...");
        myDialog.setCanceledOnTouchOutside(false);
        loading  = myDialog.findViewById(R.id.loadingPanel);
        closePopUpButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersDatabase.database.getReference().child("Users")
                        .child(UsersDatabase.auth.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for (DataSnapshot response : snapshot.child("sendingMeetRequest").getChildren()) {
                                    clearRequests(snapshot, "Powered by RafRaz");
                                }
                                UsersDatabase.clearDangerData();
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                myDialog.dismiss();
            }
        });
        myDialog.show();
    }

    public static void closePopUp(){
        myDialog.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void clearRequestDataAndMakeThemMeet(@NotNull DataSnapshot snapshot, String tempOtherId) {
        clearRequests(snapshot, tempOtherId);
        UsersDatabase.database.getReference("Users")
                .child(UsersDatabase.auth.getUid())
                .child("accepted")
                .setValue(tempOtherId);
        UsersDatabase.database.getReference("Users")
                .child(tempOtherId)
                .child("accepted")
                .setValue(UsersDatabase.auth.getUid());
        UsersDatabase.database.getReference("Users")
                .child(UsersDatabase.auth.getUid())
                .child("sendingMeetRequest").setValue(null);
        UsersDatabase.database.getReference("Users")
                .child(UsersDatabase.auth.getUid())
                .child("gettingMeetRequest").setValue(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void clearRequests(@NotNull DataSnapshot snapshot, String tempOtherId) {
        for(DataSnapshot resp: snapshot.child("sendingMeetRequest").getChildren()) {
            if(!Objects.equals(resp.getKey(), tempOtherId)) {
                UsersDatabase.database.getReference("Users")
                        .child(resp.getKey())
                        .child("gettingMeetRequest")
                        .child(UsersDatabase.auth.getUid()).setValue(null);
            }
        }
    }


    public void sendMeetRequest() {
        readUsersByFilter(filterGender, filterContinent);
    }

    private void sendMeetRequestByList(ArrayList<User> filteredUsers) {

        FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
        int counter = 0;
        int filteredUsersSize = filteredUsers.size();
        filteredUsersSize = filteredUsersSize>20? 20: filteredUsersSize;

        for(int i=0; i < filteredUsers.size(); i++) {
            if(filteredUsers.get(i).getId().equals(lastUsersIDs)) {
                counter++;
                filteredUsers.remove(i);
            }
        }
        if(filteredUsersSize-counter <= 3) {
            for(int i=0; i < 3-(filteredUsersSize-counter); i++) {
                lastUsersIDs.remove(i);
            }
        }

        for(int i=0; i < filteredUsers.size(); i++) {
            if(!filteredUsers.get(i).getId().equals(lastUsersIDs)) {
                UsersDatabase.sendMeetRequest(myUser.getUid(), filteredUsers.get(i).getId());
            }
        }

//        if(counter == filteredUsers.size()) {
//            counter = 0;
//        }
//        try {
//            FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
//
//            for(int i=counter; i <counter+3; i++) {
//                if(filteredUsers.get(counter) == null) {
//                    counter=0;
//                    break;
//                }
//                VideoCallActivity.otherUserId = filteredUsers.get(counter).getId();
//                UsersDatabase.sendMeetRequest(myUser.getUid(), filteredUsers.get(counter).getId());
//                counter = i+1;
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//        }

    }

    private void readUsersByFilter(String gender, String continent){
        ArrayList<User> mUsers = new ArrayList<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if (!user.getId().equals(firebaseUser.getUid())) {
                        if(user.getIsVerified()) {
                            if("ALL".equals(gender)) {
                                if(continent.equals(user.getContinent())) {
                                    mUsers.add(user);
                                }
                            } else {
                                if (gender.equals(user.getGender())) {
                                    if (continent.equals(user.getContinent())) {
                                        mUsers.add(user);
                                    }
                                }
                            }
                        }
                    }
                }
//                userAdapter = new UserAdapter(getContext(), mUsers, false);
//                recyclerView.setAdapter(userAdapter);
                if(mUsers.size() == 0) {
                    popUpText.setText("No users in current continent\n Try another");
                    loading.setVisibility(View.INVISIBLE);
                } else {
                    sendMeetRequestByList(mUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}