package com.app.talkzy.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.app.talkzy.ContinentData;
import com.app.talkzy.Locator;
import com.app.talkzy.MainActivity;
import com.app.talkzy.R;
import com.app.talkzy.SearchingActivity;
import com.app.talkzy.database.UsersDatabase;
import com.app.talkzy.loginRegistrationClasses.LogIn;
import com.app.talkzy.OtherClasses.IntentChanger;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    public static boolean isLogoutPressed = false;
    public User profileUser;

    // Buttons
    private Button settingsButton;

    // Texts
    static TextView convosCount;
    static TextView friendsCount;

    //Country
    private static String countryName;
    private static String continentName;
    private static TextView countryText;

    //Confirm Buttons
    private Button logOutButton;
    private Button submitButton;

    //Activities
    private ConstraintLayout profileLayout;
    private ConstraintLayout settingsLayout;

    //Gender
    private Spinner genderSpinner;
    private String genderText;

    //Year
    private Spinner yearsSpinner;
    private String yearText;
    //Month
    private Spinner monthsSpinner;
    private String monthText;
    //Day
    private Spinner daysSpinner;
    private String dayText;

    //Profile fields
    private TextView userNameTextView;
    private TextView fullNameTextView;
    private EditText changeFullName;
    private EditText changeUserName;
    private TextView BioText;
    private EditText changeBioText;

    // Profile img
    private ImageView img;

    // Country and birthday texts
    private TextView birthdayTextShow;
    private static TextView countryTextShow;

    StorageReference storageReference;
    private Uri imageUri;
    private StorageTask uploadTask;

    ValueEventListener valueEventListener;
    DatabaseReference reference;
    public static FragmentActivity thisFragment;

    public ProfileFragment() {
        // Required empty public constructor
    }

    //Searching part
    private ImageView toSearchPage;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bio text
        BioText = (TextView) root.findViewById(R.id.BioText);
        changeBioText = (EditText) root.findViewById(R.id.changeBioText);

        //Country name
        countryText = root.findViewById(R.id.countryText);


        // To search page
        toSearchPage = (ImageView) root.findViewById(R.id.toSearchPage);

        // Info id
        userNameTextView = (TextView) root.findViewById(R.id.userName);
        ViewGroup.MarginLayoutParams textViewLayoutParams = new ViewGroup.MarginLayoutParams(userNameTextView.getLayoutParams());

        //Full name text
        fullNameTextView = (TextView) root.findViewById(R.id.fullName);

        //Activities
        profileLayout = root.findViewById(R.id.profileLayout);
        settingsLayout = root.findViewById(R.id.settingsLayout);

        // Texts
        convosCount = root.findViewById(R.id.convosCount);
        friendsCount = root.findViewById(R.id.friendsCount);
        changeFullName = root.findViewById(R.id.changeFullName);
        birthdayTextShow = root.findViewById(R.id.birthdayTextShow);
        countryTextShow = root.findViewById(R.id.countryTextShow);

        // Change username edit text
        changeUserName = root.findViewById(R.id.changeUserName);

        //Buttons
        logOutButton = root.findViewById(R.id.logOutButton);
        settingsButton = root.findViewById(R.id.settingsButton);
        submitButton = root.findViewById(R.id.submitButton);

        // Image
        img = root.findViewById(R.id.profileImageView);
        img.setClipToOutline(true);
        ViewGroup.MarginLayoutParams imgLayoutParams = new ViewGroup.MarginLayoutParams(userNameTextView.getLayoutParams());

        Locator.connectingLocationService(MainActivity.mainActivity);

        // Gender
        genderSpinner = (Spinner) root.findViewById(R.id.genderSpinner);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(MainActivity.mainActivity,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);
        genderSpinner.setSelection(0);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                genderText = String.valueOf(genderSpinner.getSelectedItem());
            }
            public void onNothingSelected(AdapterView<?> parentView) {
                genderText = String.valueOf(genderSpinner.getSelectedItem());
            }
        });

        //////Birthday Spinners//////

        //Years Spinner
        yearsSpinner = (Spinner) root.findViewById(R.id.yearsSpinner);
        ArrayAdapter<CharSequence> yearsAdapter = ArrayAdapter.createFromResource(MainActivity.mainActivity,
                R.array.yearsOfBirth, android.R.layout.simple_spinner_item);
        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearsSpinner.setAdapter(yearsAdapter);
        yearsSpinner.setSelection(0);
        yearsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                yearText = String.valueOf(yearsSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                yearText = String.valueOf(yearsSpinner.getSelectedItem());
            }
        });

        //Months spinner
        monthsSpinner = (Spinner) root.findViewById(R.id.monthsSpinner);
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(MainActivity.mainActivity,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthsSpinner.setAdapter(monthAdapter);
        monthsSpinner.setSelection(0);
        monthsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monthText = String.valueOf(monthsSpinner.getSelectedItemPosition()+1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                monthText = String.valueOf(monthsSpinner.getSelectedItemPosition()+1);
            }
        });

        //Days spinner
        daysSpinner = (Spinner) root.findViewById(R.id.daysSpinner);
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(MainActivity.mainActivity,
                R.array.days_array, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daysSpinner.setAdapter(dayAdapter);
        daysSpinner.setSelection(0);
        daysSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dayText = String.valueOf(daysSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                dayText = String.valueOf(daysSpinner.getSelectedItem());
            }
        });

        //////Birthday Spinners//////

        // To change pic (reference to storage)
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        // Log out buttons
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersDatabase.googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            ProfileFragment.isLogoutPressed = true;
                            IntentChanger.change(MainActivity.mainActivity, LogIn.class);
                            Toast.makeText(getActivity(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // To change picture
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(MainActivity.mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
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

        // Setting Button
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileLayout.setVisibility(View.INVISIBLE);
                settingsLayout.setVisibility(View.VISIBLE);
            }
        });

        // Back from settings
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!changeUserName.getText().toString().isEmpty()) {
                    if (!changeUserName.getText().toString().contains(" ")) {
                        updateIfUserNameNotAvailable(changeUserName.getText().toString());
                    } else {
                        changeUserName.setError("username can`t contain space!");
                        changeUserName.requestFocus();
                    }
                } else {
                    changeUserName.setError("this field can't be empty");
                    changeUserName.requestFocus();
                }

            }
        });

        toSearchPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentChanger.change(getActivity(), SearchingActivity.class, 0);
            }
        });

        try {
            FirebaseUser firebaseUser = UsersDatabase.auth.getCurrentUser();
            String userId = firebaseUser.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            reference = database.getReference("Users").child(userId);

            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    thisFragment = getActivity();
                    profileUser = snapshot.getValue(User.class);
                    makeProfile(thisFragment);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            reference.addValueEventListener(valueEventListener);

        }
        catch (Exception e){
            e.printStackTrace();
        }

        try{
            UsersDatabase.clearDangerData();
        }catch(Exception e){
            e.printStackTrace();
        }

        return root;
    }

    private void updateUserInfo() {
        // Getting text from editTexts
        String newFullName = changeFullName.getText().toString();
        String userName = changeUserName.getText().toString();
        String bioText = changeBioText.getText().toString();
        String birthdayText = getBirthday();

        // Setting texts in user
        profileUser.setFullName(newFullName);
        profileUser.setUserName(userName);
        profileUser.setBioText(bioText);
        profileUser.setGender(genderText);
        profileUser.setBirthday(birthdayText);

        // Setting texts that got above
        fullNameTextView.setText(profileUser.getFullName());
        userNameTextView.setText(profileUser.getUserName());
        BioText.setText(profileUser.getBioText());

        if(!profileUser.getBirthday().isEmpty()) {
            birthdayTextShow.setText(profileUser.getBirthday());
        }

        // Update in database
        UsersDatabase usersDatabase = new UsersDatabase();
        usersDatabase.updateUser(profileUser);

    }

    boolean userNameIsAvailable = true;
    private void updateIfUserNameNotAvailable(String username){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(Objects.equals(username, user.getUserName())) {
                        if(!Objects.equals(firebaseUser.getUid(), user.getId())) {
                            userNameIsAvailable = false;
                            break;
                        }
                    }
                }
                if(userNameIsAvailable) {
                    // Update user info
                    updateUserInfo();

                    // Change view
                    profileLayout.setVisibility(View.VISIBLE);
                    settingsLayout.setVisibility(View.INVISIBLE);
                } else {
                    userNameIsAvailable = true;
                    Toast.makeText(MainActivity.mainActivity, "Try another username!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void setCountry(String name) {
        if("Not defined".equals(name)) {
            try {
                UsersDatabase.tryToGetFromDB();
            } catch (Exception e) {
                e.printStackTrace();
                countryText.setText("Trying to define...");
                countryTextShow.setText("Trying to define...");
            }
        } else {
            setCountryFinally(name);
        }

    }

    public static void setCountryFinally(String name){
        if("Not defined".equals(name)) {
            countryText.setText("Trying to define...");
            countryTextShow.setText("Trying to define...");
        } else {
            countryText.setText(name);
            countryTextShow.setText(name);
        }
        UsersDatabase.setCountryToDB(name);
        UsersDatabase.setContinentToDB(ContinentData.getContinent(name));
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void makeProfile(FragmentActivity fragment) {

        // General view of profile
        userNameTextView.setText(profileUser.getUserName());
        fullNameTextView.setText(profileUser.getFullName());
        BioText.setText(profileUser.getBioText());

        convosCount.setText(profileUser.getConvosCount()+"");
        friendsCount.setText(profileUser.getFriendsCount()+"");

        // For settings (to change)
        changeFullName.setText(profileUser.getFullName());
        changeUserName.setText(profileUser.getUserName());
        changeBioText.setText(profileUser.getBioText());

        // Setting spinners positions
        setGenderSpinnerPosition();
        setBirthdaySpinnersPosition();

        //Setting birthday text
        if(!profileUser.getBirthday().isEmpty()) {
            birthdayTextShow.setText(profileUser.getBirthday());
        }

        // Get pic
        Glide
                .with(getContext())
                .load(profileUser.getImageUri())
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(img);
    }

    public void setGenderSpinnerPosition() {
        String tempGenderText = profileUser.getGender();
        if(!"null".equals(tempGenderText) && tempGenderText != null) {
            int position = 0;
            switch (tempGenderText) {
                case "MALE":
                    position = 1;
                    break;
                case "FEMALE":
                    position = 2;
                    break;
                default:
                    position = 0;
                    break;
            }
            genderSpinner.setSelection(position);
        }
    }

    public void setBirthdaySpinnersPosition() {
        String tempBirthdayText = profileUser.getBirthday();
        if(!"null".equals(tempBirthdayText) && tempBirthdayText != null) {
            String[] itemsOfBirthday = getBirthdayItems(tempBirthdayText);
            daysSpinner.setSelection(Integer.valueOf(itemsOfBirthday[0]) - 1);
            monthsSpinner.setSelection(Integer.valueOf(itemsOfBirthday[1]) - 1);
            yearsSpinner.setSelection(2021 - Integer.valueOf(itemsOfBirthday[2]));
        }
    }

    private String [] getBirthdayItems(String str) {
        return str.split("\\.");
    }

    private String getBirthday() {
        StringBuilder sb = new StringBuilder();
        sb.append(dayText)
                .append(".")
                .append(monthText)
                .append(".")
                .append(yearText);

        return sb.toString();
    }

    public void pickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getContext());
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
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(UsersDatabase.auth.getUid());

                        HashMap<String, Object> map = new HashMap<>();

                        map.put("imageUri",mUri);
                        reference.updateChildren(map);
                        pd.dismiss();

                    }
                    else {
                        Toast.makeText(getContext(), "Failed!" , Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
        else {
            Toast.makeText(getContext(), "No Image Selected!" , Toast.LENGTH_SHORT).show();
        }

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
            imageUri = data.getData();

            if(uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getContext(), "Upload in progress" , Toast.LENGTH_SHORT).show();
            }
            else {
                uploadImage();
            }

            //img.setImageURI(imageUri);
        } else {
            Toast.makeText(MainActivity.mainActivity, "Try Again!!", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        try {
            reference.removeEventListener(valueEventListener);
        }
        catch (Exception e){

        }
    }
}
