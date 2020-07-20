package com.simplechatter.settingspane;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.simplechatter.MainActivity;
import com.simplechatter.R;
import com.simplechatter.chatactivity.ImageViewerActivity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private FirebaseAuth mAuth;
    private boolean userHasMoven;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootRef;
    private TextView userNameTextView,accountSettingsTextView,logOutTextView;
    private CircleImageView circleImageView;
    private String currentUserAbout,currentUserName,currentUserImage,currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();

        circleImageView = (CircleImageView)findViewById(R.id.SettingsActivity_circleImageView);
        userNameTextView = (TextView)findViewById(R.id.SettingsActivity_userName_textView);
        accountSettingsTextView = (TextView)findViewById(R.id.SettingsActivity_accountSettingsTab_textView);
        logOutTextView = (TextView)findViewById(R.id.SettingsActivity_logOutTab_textView);

        Intent intent = getIntent();
        currentUserImage = intent.getStringExtra("userImage");
        currentUserName = intent.getStringExtra("userName");
        currentUserAbout = intent.getStringExtra("userAbout");
        currentUserId = mAuth.getCurrentUser().getUid();
        if(currentUserImage == null || currentUserImage.equalsIgnoreCase(""))
            Picasso.get().load(R.drawable.userdefaultprofile).into(circleImageView);
        else
            Picasso.get().load(currentUserImage).placeholder(R.drawable.userdefaultprofile).into(circleImageView);
        userNameTextView.setText(currentUserName);

        //OnClickListeners
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToImageViewerActivity();
            }
        });
        accountSettingsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToAccountSettingsActivity();
            }
        });
        logOutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

    }

    private void moveToImageViewerActivity()
    {
        userHasMoven = true;
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra("image_url",currentUserImage);
        startActivity(intent);
    }

    private void moveToAccountSettingsActivity()
    {
        userHasMoven = true;
        Intent intent = new Intent(this,AccountSettingsActivity.class);
        intent.putExtra("userId",currentUserId);
        intent.putExtra("userImage",currentUserImage);
        intent.putExtra("userAbout",currentUserAbout);
        intent.putExtra("userName",currentUserName);
        startActivity(intent);
    }

    public void logOut()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Are You Sure?");
        alertDialog.setMessage("Do you really want to log out");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rootRef.child("Users").child(currentUserId).child("logged_in_status").setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            updateUserOnlineStatus("offline");
                            Log.d(TAG, "onComplete: Logged Out Successfully");
                            Toast.makeText(SettingsActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                            logOutUtil();
                        }else{
                            Log.d(TAG, "onComplete: Error in Logging you Out! : " + task.getException().getMessage());
                            Toast.makeText(SettingsActivity.this, "Error in Logging you Out! : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        }).setNegativeButton("Never", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();


    }

    private void logOutUtil() {
        updateUserOnlineStatus("offline");
        mAuth.signOut();
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        userHasMoven = false;
        finish();
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
        String disp = "[" + saveCurrentDate + ", " + saveCurrentTime + ", " + state + ", " + currentUserId + "]" + " From HomeScreenActivity" ;
        Log.d("rock",disp);
        if(userHasMoven){
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
    public void onBackPressed() {
        userHasMoven = true;
        super.onBackPressed();
    }
}
