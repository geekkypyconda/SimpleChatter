package com.simplechatter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetUpUserActivity extends AppCompatActivity {

    public static final int WRITE_EXT_STORAGE_REQUEST_CODE = 2,READ_EXT_STORAGE_REQUEST_CODE = 1;
    private SharedPreferences sharedPreferences;
    private boolean doubleBackToExitPressedOnce = false,setUp = false,userHasMovenToHomeScreen = false;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootRef;
    public int IMAGE_REQUEST_CODE = 1;
    private String userId,userName,userAbout,userImageUrl = "",backendUserEmail = "",backendUserPhoneNumber = "",deviceToken;
    private CircleImageView circleImageView;
    protected EditText userName_editText,userAbout_editText;
    protected Button rocknRollButton;
    private TextView textView,logOutTextView;
    private FirebaseAuth mAuth;
    private StorageReference userProfileImageRefrences;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_user);

        //User credentials
        Intent userIntent = getIntent();
        backendUserPhoneNumber = userIntent.getStringExtra("user_number");
        Log.d("rock","backedn Phone numebr sis " + backendUserPhoneNumber);
        backendUserEmail = userIntent.getStringExtra("user_email");
        Log.d("rock","backend user Email is " + backendUserEmail);


        //Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        userProfileImageRefrences = FirebaseStorage.getInstance().getReference().child("Profile Images");
        deviceToken = FirebaseInstanceId.getInstance().getToken();

                sharedPreferences = this.getSharedPreferences("com.simplechatter_user",Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
        circleImageView = (CircleImageView)findViewById(R.id.SetUpUserActivity_imageView);
        textView = (TextView)findViewById(R.id.SetUpUserActivity_clickToAddImage_textView);
        userName_editText = (EditText)findViewById(R.id.SetUpUserActivity_userName_eidtText);
        userAbout_editText = (EditText)findViewById(R.id.SetUpUserActivity_userAbout_eidtText);
        rocknRollButton = (Button)findViewById(R.id.SetUpUserActivity_rockAndRollButton);
        logOutTextView = (TextView)findViewById(R.id.SetUpUserActivity_logOut_textView);
        logOutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableEveryThing();showProgressDialog("Logging out");
                AlertDialog builder = new AlertDialog.Builder(SetUpUserActivity.this)
                        .setTitle("Are you sure")
                        .setMessage("Do you Really want to Log out")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                mAuth.signOut();
                                moveToMainActivity();
                                Toast.makeText(SetUpUserActivity.this, "Logging out", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("Never", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SetUpUserActivity.this, "Never say Never", Toast.LENGTH_SHORT).show();
                                enableEveryThing();progressDialog.dismiss();
                            }
                        }).show();

            }
        });

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else
            Log.d("rock","Read External Storage Permission Granted!");
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }else
            Log.d("rock","Write External Storage Permission Granted!");

        showProgressDialog("Retrieving Data");disableEveryThing();
        wasUserAlreadySetUp();

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
                Intent photoIntent = new Intent();
                photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                photoIntent.setType("image/*");
                startActivityForResult(photoIntent,IMAGE_REQUEST_CODE);
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
                Intent photoIntent = new Intent();
                photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                photoIntent.setType("image/*");
                startActivityForResult(photoIntent,IMAGE_REQUEST_CODE);
            }
        });

    }

    private void moveToMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        progressDialog.dismiss();
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("rock","Requestcode " + requestCode + ", ResultCode " + resultCode + " CropImage Code " + CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);

        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setAutoZoomEnabled(true)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                disableEveryThing();showProgressDialog("Uploading Image");
                final Uri croppedImage = result.getUri();
                final StorageReference filePath = userProfileImageRefrences.child(userId + ".jpg");
                filePath.putFile(croppedImage).addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetUpUserActivity.this, "Profile Image updated", Toast.LENGTH_SHORT).show();
                            Log.d("rock","Profile Image updated");

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    userImageUrl = uri.toString();
                                }
                            });
                            Log.d("rock","Download usl is " + userImageUrl);
                            rootRef.child("Users").child(userId).child("image").setValue(userImageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(SetUpUserActivity.this, "Profile Image updated", Toast.LENGTH_SHORT).show();
                                        Log.d("rock","Profile Image updated");
                                        //userImageUrl = downloadUrl;
                                        circleImageView.setImageURI(croppedImage);
                                        enableEveryThing();progressDialog.dismiss();
                                    }else{
                                        Toast.makeText(SetUpUserActivity.this, "Image Uploadinf Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.d("rock","Image Uploadinf Failed! " + task.getException().getMessage());
                                        enableEveryThing();progressDialog.dismiss();
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(SetUpUserActivity.this, "Image Uploadinf Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("rock","Image Uploadinf Failed! " + task.getException().getMessage());
                            enableEveryThing();progressDialog.dismiss();
                        }
                    }
                });
            }
        }


    }

    public void showProgressDialog(String s)
    {
        progressDialog.setTitle(s);
        progressDialog.setMessage("Please Wait.....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        Log.d("rock","inside progeess Dialog");

    }
    public void disableEveryThing()
    {
        userAbout_editText.setEnabled(false);
        userName_editText.setEnabled(false);
        rocknRollButton.setEnabled(false);
        Log.d("rock","disabe All");
    }
    public void enableEveryThing()
    {
        userAbout_editText.setEnabled(true);
        userName_editText.setEnabled(true);
        rocknRollButton.setEnabled(true);
        Log.d("rock","Enalble all");
    }

    public boolean wasUserAlreadySetUp()
    {
        //showProgressDialog("Retrieving data");disableEveryThing();
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("Users/" + "name");
        boolean b = sharedPreferences.getBoolean("set",false);
        if(dbr != null && false){
            Log.d("rock","user Perfectly Set Up! Now Moving To Home Screen");
            moveToHomeScreenActivity();
        }

        else if(dbr != null){
            Log.d("rock","User Was Not Perfectly Set up!");
            retrieveUserDetails();
        }
        else{
            enableEveryThing();
            progressDialog.dismiss();
            Log.d("rock","Was user aleady setup in ELSE");
            return false;
        }
        return false;
    }
    private void retrieveUserDetails() {
        //disableEveryThing();
        //showProgressDialog("Retrieving Data");
        rootRef.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userImage = "";
                if((dataSnapshot.exists())){
                    if(dataSnapshot.hasChild("name")){
                        userName_editText.setText(dataSnapshot.child("name").getValue().toString());
                        Log.d("rock","name Exist with value " + dataSnapshot.child("name").getValue().toString());
                    }

                    if(dataSnapshot.hasChild("about")){
                        userAbout_editText.setText(dataSnapshot.child("about").getValue().toString());
                        Log.d("rock","About Exist with value " + dataSnapshot.child("about").getValue().toString());
                    }

                    if(dataSnapshot.hasChild("phone")){
                        backendUserPhoneNumber = dataSnapshot.child("phone").getValue().toString();
                        Log.d("rock","Phone Exist with value " + dataSnapshot.child("phone").getValue().toString());
                    }

                    if(dataSnapshot.hasChild("email")){
                        backendUserEmail = dataSnapshot.child("email").getValue().toString();
                        Log.d("rock","Email Exist with value " + dataSnapshot.child("email").getValue().toString());
                    }


                    if(dataSnapshot.hasChild("image")){
                        userImage = (String) dataSnapshot.child("image").getValue();
                        Log.d("rock","user Image is " + userImage);
                        if(!userImage.equalsIgnoreCase("")){
                            Picasso.get().load(userImage).into(circleImageView);
                            userImageUrl = userImage;
                        }

                        else
                            Log.d("rock","User image Not Set!");
                    }


                    enableEveryThing();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SetUpUserActivity.this, "Some Error Occured! " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                enableEveryThing();
                progressDialog.dismiss();
                userName_editText.setText("");
                userAbout_editText.setText("Hi! now I am also on Simple Chatter");
            }
        });
        //enableEveryThing();
        //progressDialog.dismiss();
    }

    public void moveToHomeScreenActivity()
    {
        Intent intent = new Intent(this,HomeScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        progressDialog.dismiss();
        updateUserOnlineStatus("online");
        userHasMovenToHomeScreen = true;
        finish();
    }

    public void RockNRoll(View view)
    {
        disableEveryThing();showProgressDialog("Rock'n Roll");

        userName = userName_editText.getText().toString();
        userAbout = userAbout_editText.getText().toString();
        if(userName.equalsIgnoreCase("") || userName.equalsIgnoreCase(" ")){
            Toast.makeText(this, "UserName cannot be Null! or Space!", Toast.LENGTH_LONG).show();
            rocknRollButton.setEnabled(true);
            enableEveryThing();progressDialog.dismiss();
            return;
        }
        if(userAbout.equalsIgnoreCase("") || userAbout.equalsIgnoreCase(" ")){
            Toast.makeText(this, "Your Staus Cannot be Null! or Space!", Toast.LENGTH_SHORT).show();
            rocknRollButton.setEnabled(true);
            enableEveryThing();progressDialog.dismiss();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Map<String,String> map = new HashMap<>();
        map.put("name",userName);
        map.put("uid",userId);
        map.put("about",userAbout);
        map.put("image",userImageUrl);
        map.put("device_token",deviceToken);
        if(backendUserPhoneNumber == null){
            backendUserPhoneNumber = "";
            Log.d("rock","backend phone Number is null!");
        }
        map.put("phone",backendUserPhoneNumber);
        if(backendUserEmail == null){
            backendUserEmail = "";
            Log.d("rock","Backend Email is null");
        }
        map.put("email",backendUserEmail);
        rootRef.child("Users").child(userId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    setUp = true;
                    Toast.makeText(SetUpUserActivity.this, "Rock'n Roll!", Toast.LENGTH_SHORT).show();
                    Log.d("rock","User info Setup Successfully");
                    sharedPreferences.edit().putBoolean("set",true);

                    moveToHomeScreenActivity();
                }else{
                    Toast.makeText(SetUpUserActivity.this, "Some Error Occured Please try Again! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("rock","User info Setup Failed!" + task.getException().getMessage());
                    rocknRollButton.setEnabled(true);
                    enableEveryThing();progressDialog.dismiss();
                }

            }
        });
        setUp = true;
    }



    public void addImage()
    {


        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else {
            //requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        }

        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }else {
            //requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }



    }



    private void updateUserOnlineStatus(String state)
    {
        String saveCurrentTime,saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM,dd,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime= currentTime.format(calendar.getTime());

        HashMap<String,Object> map = new HashMap<>();
        map.put("current_date",saveCurrentDate);
        map.put("current_time",saveCurrentTime);
        map.put("current_status",state);
        String currentUserId = mAuth.getCurrentUser().getUid();
        String disp = "[" + saveCurrentDate + ", " + saveCurrentTime + ", " + state + ", " + currentUserId + "]" + " From SetUpUserActivity" ;
        Log.d("rock",disp);
        rootRef.child("Users").child(currentUserId).child("online_status").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                    Log.d("rock","User online status saved SuccessFully!");
                else
                    Log.d("rock","User online status saving failed!");
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
        }
        else if(requestCode == 2){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            updateUserOnlineStatus("offline");
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_LONG).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser() != null && !userHasMovenToHomeScreen)
            updateUserOnlineStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAuth.getCurrentUser() != null && !userHasMovenToHomeScreen)
            updateUserOnlineStatus("offline");
    }

}

