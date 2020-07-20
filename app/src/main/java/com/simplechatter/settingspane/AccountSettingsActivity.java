package com.simplechatter.settingspane;

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

public class AccountSettingsActivity extends AppCompatActivity {

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseRootRef;
    private StorageReference storageRootRef,userProfileImageRefrences;
    private String currentUserId,currentUserName,currentUserImage,currentUserAbout,currentUserNewImageUrl = "";
    private boolean userHasMoven;

    //Activity Components
    public static final int IMAGE_REQUEST_CODE = 1;
    private static final String TAG = "AccountSettingsActivity";
    private TextView clickToAddImageTextView;
    private CircleImageView circleImageView;
    private EditText userNameEditText,userAboutEditText;
    private Button updateButton;
    private boolean editedOnce;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        //Firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseRootRef = firebaseDatabase.getReference();
        storageRootRef = firebaseStorage.getReference();
        userProfileImageRefrences = FirebaseStorage.getInstance().getReference().child("Profile Images");

        Intent intent = getIntent();
        currentUserImage = intent.getStringExtra("userImage");
        currentUserName = intent.getStringExtra("userName");
        currentUserAbout = intent.getStringExtra("userAbout");
        currentUserId = mAuth.getCurrentUser().getUid();

        //Activity Components
        clickToAddImageTextView = (TextView)findViewById(R.id.AccountSettingsActivity_clickToAddImage_textView);
        circleImageView = (CircleImageView)findViewById(R.id.AccountSettingsActivity_imageView);
        userNameEditText = (EditText)findViewById(R.id.AccountSettingsActivity_userName_editText);
        userAboutEditText = (EditText)findViewById(R.id.AccountSettingsActivity_userAbout_editText);
        updateButton = (Button)findViewById(R.id.AccountSettingsActivity_updateButton);
        if(currentUserImage == null || currentUserImage.equalsIgnoreCase(""))
            Picasso.get().load(R.drawable.userdefaultprofile).into(circleImageView);
        else
            Picasso.get().load(currentUserImage).placeholder(R.drawable.userdefaultprofile).into(circleImageView);

        userNameEditText.setText(currentUserName);
        userAboutEditText.setText(currentUserAbout);
        progressDialog = new ProgressDialog(this);


        //OnClicklisteners
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nameEnteredByUser = userNameEditText.getText().toString(),aboutEnteredByUser = userAboutEditText.getText().toString();
                disableEveryThing();showProgressDialog("Updating Profile");
                if(isSafe(nameEnteredByUser,aboutEnteredByUser))
                    databaseRootRef.child("Users").child(currentUserId).child("name").setValue(nameEnteredByUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                databaseRootRef.child("Users").child(currentUserId).child("about").setValue(aboutEnteredByUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d(TAG, "onComplete: User Profile Updated Successfully");
                                            Toast.makeText(AccountSettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();enableEveryThing();
                                        }else{
                                            Log.d(TAG, "onComplete: Updation Failed! : " + task.getException().getMessage());
                                            Toast.makeText(AccountSettingsActivity.this, "Failed to update User Profile! : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();enableEveryThing();
                                        }
                                    }
                                });
                            }else{
                                Log.d(TAG, "onComplete: Updation Failed! : " + task.getException().getMessage());
                                Toast.makeText(AccountSettingsActivity.this, "Failed to update User Credentials! : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();enableEveryThing();
                            }
                        }
                    });
            }
        });

        clickToAddImageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoIntent = new Intent();
                photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                photoIntent.setType("image/*");
                startActivityForResult(photoIntent,IMAGE_REQUEST_CODE);
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoIntent = new Intent();
                photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                photoIntent.setType("image/*");
                startActivityForResult(photoIntent,IMAGE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth != null)
            updateUserOnlineStatus("online");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"Requestcode " + requestCode + ", ResultCode " + resultCode + " CropImage Code " + CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK){
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
                disableEveryThing();showProgressDialog("Uploading Photo");
                final Uri croppedImage = result.getUri();
                final StorageReference filePath = userProfileImageRefrences.child(currentUserId + ".jpg");
                filePath.putFile(croppedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    currentUserNewImageUrl = uri.toString();
                                }
                            });
                            Log.d(TAG, "onComplete: Download URL is "+ currentUserNewImageUrl);
                            databaseRootRef.child("Users").child(currentUserId).child("image").setValue(currentUserNewImageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Log.d(TAG, "onComplete: Profile Image Updated Successfully");
                                        Toast.makeText(AccountSettingsActivity.this, "Profile photo updated successfully", Toast.LENGTH_SHORT).show();
                                        enableEveryThing();progressDialog.dismiss();
                                        circleImageView.setImageURI(croppedImage);
                                    }else{
                                        Log.d(TAG, "onComplete: Profile Updation failed! : "+ task.getException().getMessage());
                                        Toast.makeText(AccountSettingsActivity.this, "Failed to update profile photo! : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        enableEveryThing();progressDialog.dismiss();
                                    }
                                }
                            });
                        }else{
                            Log.d(TAG, "onComplete: Profile Updation failed! : "+ task.getException().getMessage());
                            Toast.makeText(AccountSettingsActivity.this, "Failed to update profile photo! : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            enableEveryThing();progressDialog.dismiss();
                        }
                    }
                });
            }
        }
    }

    private void showProgressDialog(String s)
    {
        progressDialog.setTitle(s);
        progressDialog.setMessage("Please Wait....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void enableEveryThing()
    {
        userAboutEditText.setEnabled(true);
        userNameEditText.setEnabled(true);
        updateButton.setEnabled(true);
    }

    private void disableEveryThing()
    {
        userAboutEditText.setEnabled(false);
        userNameEditText.setEnabled(false);
        updateButton.setEnabled(false);
    }


    private boolean isSafe(String nameEnteredByUser, String aboutEnteredByUser) {
        if(nameEnteredByUser.equalsIgnoreCase("") || nameEnteredByUser.equalsIgnoreCase(" ")){
            Toast.makeText(this, "Username cannot be Null! or Space!", Toast.LENGTH_LONG).show();
            enableEveryThing();progressDialog.dismiss();
            return false;
        }
        if(aboutEnteredByUser.equalsIgnoreCase("") || aboutEnteredByUser.equalsIgnoreCase(" ")){
            Toast.makeText(this, "Your Status Cannot be Null! or Space!", Toast.LENGTH_SHORT).show();
            enableEveryThing();progressDialog.dismiss();
            return false;
        }

        return true;
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
}
