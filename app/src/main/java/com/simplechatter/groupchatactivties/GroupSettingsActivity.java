package com.simplechatter.groupchatactivties;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.simplechatter.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupSettingsActivity extends AppCompatActivity {

    //Firebase
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseRootRef;
    private StorageReference storageRootRef;
    private FirebaseAuth mAuth;
    private boolean userHasMoven;

    //Activty
    CircleImageView circleImageView;
    EditText groupNameEditText,groupMottoEditText;
    Button resetButton,updateButton;
    ProgressDialog progressDialog;
    TextView updateGroupProfilePhotoTextView;

    //Group
    private String uniqueGroupId,uniqueGroupName,uniqueGroupImage,uniqueGroupMotto,newGroupProfileImage;
    private int IMAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);

        //Firebase
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseRootRef = firebaseDatabase.getReference();
        storageRootRef = firebaseStorage.getReference();
        mAuth = FirebaseAuth.getInstance();

        //Group
        Intent intent = getIntent();
        uniqueGroupName = intent.getStringExtra("grpName");
        uniqueGroupId = intent.getStringExtra("grpId");
        uniqueGroupImage = intent.getStringExtra("grpImage");
        uniqueGroupMotto = intent.getStringExtra("grpMotto");
        logGroupDetails();

        //Activity
        circleImageView = (CircleImageView)findViewById(R.id.GroupSettingsActivity_imageView);
        if(uniqueGroupImage != null && !uniqueGroupImage.equalsIgnoreCase(""))
            Picasso.get().load(uniqueGroupImage).placeholder(R.drawable.defaultgroupicon).into(circleImageView);
        else
            Picasso.get().load(R.drawable.defaultgroupicon).placeholder(R.drawable.defaultgroupicon).into(circleImageView);

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfilePhoto();
            }
        });
        updateGroupProfilePhotoTextView = (TextView)findViewById(R.id.GroupSettingsActivity_clickToAddImage_textView);
        updateGroupProfilePhotoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfilePhoto();
            }
        });
        groupNameEditText = (EditText)findViewById(R.id.GroupSettingsActivity_userName_editText);groupNameEditText.setText(uniqueGroupName);
        groupMottoEditText = (EditText)findViewById(R.id.GroupSettingsActivity_userAbout_editText);groupMottoEditText.setText(uniqueGroupMotto);
        resetButton = (Button)findViewById(R.id.GroupSettingsActivity_resetButton);
        updateButton = (Button)findViewById(R.id.GroupSettingsActivity_updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = groupNameEditText.getText().toString(),motto = groupMottoEditText.getText().toString();
                if(name == null || name.equalsIgnoreCase("") || motto == null || motto.equalsIgnoreCase("")){
                    Log.d("rock","Group Name or Motto Cannot Be null");
                    Toast.makeText(GroupSettingsActivity.this, "Group Name or Motto Cannot Be null", Toast.LENGTH_SHORT).show();
                }

                updateProfile(name,motto);
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetProfile();
            }
        });
        progressDialog = new ProgressDialog(this);
    }

    private void logGroupDetails()
    {
        String msg = "Name : " + uniqueGroupName + " -- Motto -- " + uniqueGroupMotto + " -- ID -- "  + uniqueGroupId + " -- Image -- " + uniqueGroupImage;
        Log.d("rock",msg);
    }

    private void updateProfilePhoto()
    {
        Intent photoIntent = new Intent();
        photoIntent.setAction(Intent.ACTION_GET_CONTENT);
        photoIntent.setType("image/*");
        startActivityForResult(photoIntent,IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("rock","Request Code is " + requestCode + " Result Code is " + resultCode);
        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setAutoZoomEnabled(true)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this);

        }
        else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                disableEverything();showProgressDialog("Uploading Image");
                final Uri croppedImage = result.getUri();
                final StorageReference filePath = storageRootRef.child("Groups").child("Profile Images").child(uniqueGroupId  + ".jpg");
                filePath.putFile(croppedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    newGroupProfileImage = uri.toString();
                                }
                            });
                            databaseRootRef.child("Groups").child(uniqueGroupId).child("image").setValue(newGroupProfileImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(GroupSettingsActivity.this, "Profile Image updated", Toast.LENGTH_SHORT).show();
                                        Log.d("rock","Profile Image updated");
                                        //userImageUrl = downloadUrl;
                                        circleImageView.setImageURI(croppedImage);
                                        enableEveryThing();progressDialog.dismiss();
                                    }else{
                                        Toast.makeText(GroupSettingsActivity.this, "Image Uploadinf Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.d("rock","Image Uploadinf Failed! " + task.getException().getMessage());
                                        enableEveryThing();progressDialog.dismiss();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(GroupSettingsActivity.this, "Image Uploadinf Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("rock","Image Uploadinf Failed! " + task.getException().getMessage());
                            enableEveryThing();progressDialog.dismiss();
                        }
                    }
                });
            }
        }
        else{
            Log.d("rock","Group Profile Updation failed! ");
            Toast.makeText(GroupSettingsActivity.this, "Group Profile Updation failed! ", Toast.LENGTH_SHORT).show();
            enableEveryThing();progressDialog.dismiss();
        }
    }

    private void updateProfile(String name, String motto) {
        databaseRootRef.child("Groups").child(uniqueGroupId).child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("rock","Group Profile Updated Successfully");
                    Toast.makeText(GroupSettingsActivity.this, "Group Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d("rock","Group Profile Updation failed! " + task.getException().getMessage());
                    Toast.makeText(GroupSettingsActivity.this, "Group Profile Updation failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        databaseRootRef.child("Groups").child(uniqueGroupId).child("motto").setValue(motto).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("rock","Group Profile Updated Successfully");
                    Toast.makeText(GroupSettingsActivity.this, "Group Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d("rock","Group Profile Updation failed! " + task.getException().getMessage());
                    Toast.makeText(GroupSettingsActivity.this, "Group Profile Updation failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void resetProfile() {
        groupNameEditText.setText(uniqueGroupName);
        groupMottoEditText.setText(uniqueGroupMotto);
        if(uniqueGroupImage != null && !uniqueGroupImage.equalsIgnoreCase(""))
            Picasso.get().load(uniqueGroupImage).placeholder(R.drawable.defaultgroupicon).into(circleImageView);
        else
            Picasso.get().load(R.drawable.defaultgroupicon).placeholder(R.drawable.defaultgroupicon).into(circleImageView);
    }

    private void disableEverything()
    {
        groupNameEditText.setEnabled(false);
        groupMottoEditText.setEnabled(false);
        resetButton.setEnabled(false);
        updateButton.setEnabled(false);
        Log.d("rock","disabe All");
    }
    private void enableEveryThing()
    {
        groupNameEditText.setEnabled(true);
        groupMottoEditText.setEnabled(true);
        resetButton.setEnabled(true);
        updateButton.setEnabled(true);
    }
    private void showProgressDialog(String msg)
    {
        progressDialog.setTitle(msg);
        progressDialog.setMessage("Please Wait.....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        Log.d("rock","inside progeess Dialog");
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
        String disp = "[" + saveCurrentDate + ", " + saveCurrentTime + ", " + state + ", " + currentUserId + "]" + " From HomeScreenActivity" ;
        Log.d("rock",disp);
        if(userHasMoven){
            databaseRootRef.child("Users").child(currentUserId).child("online_status").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                        Log.d("rock","User online status saved SuccessFully!");
                    else
                        Log.d("rock","User online status saving failed!");
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        userHasMoven = true;
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        if(!userHasMoven)
            updateUserOnlineStatus("offline");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(!userHasMoven)
            updateUserOnlineStatus("offline");
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        updateUserOnlineStatus("online");
        super.onStart();
    }
}
