package com.simplechatter.AboutInfoSection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.simplechatter.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AboutActivity extends AppCompatActivity {

    private String userIdForGroup,userImage,userAbout,userName;
    private boolean userHasMoven;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Intent intent = getIntent();
        userIdForGroup = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");
        userAbout = intent.getStringExtra("userAbout");
        userImage = intent.getStringExtra("userImage");

        mAuth = FirebaseAuth.getInstance();
        databaseRootRef = FirebaseDatabase.getInstance().getReference();

    }
    public void reportBug(View view)
    {
        userHasMoven = true;
        Intent intent2 = new Intent(this,ReportBugActivity.class);
        intent2.putExtra("userId",userIdForGroup);
        intent2.putExtra("userImage",userImage);
        intent2.putExtra("userAbout",userAbout);
        intent2.putExtra("userName",userName);
        startActivity(intent2);
        finish();
    }
    public void sendFeedback(View view)
    {
        userHasMoven = true;
        Intent intent2 = new Intent(this,FeedbackActivity.class);
        intent2.putExtra("userId",userIdForGroup);
        intent2.putExtra("userImage",userImage);
        intent2.putExtra("userAbout",userAbout);
        intent2.putExtra("userName",userName);
        startActivity(intent2);
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
        super.onStart();
        updateUserOnlineStatus("online");
    }
}
