package com.simplechatter.AboutInfoSection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    //Firebase
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRootRef;
    private boolean userHasMoven;

    //Activity Components
    private String currentUserId = "",currentUserName = "";
    private EditText editText;
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        //Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        databaseRootRef = firebaseDatabase.getReference();
        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("userId");
        currentUserName = intent.getStringExtra("userName");

        //Activity Components
        editText = (EditText) findViewById(R.id.FeedbackActivity_editText);
        button = (Button) findViewById(R.id.FeedbackActivity_sendButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if(text == null || text.equalsIgnoreCase("")){
                    Toast.makeText(FeedbackActivity.this, "Feedback cannot be Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendFeedback(text);
            }
        });
    }

    private void sendFeedback(String text) {
        String feedBackKeyId = databaseRootRef.child("Feedbacks").child(currentUserId).push().getKey();
        String saveCurrentTime = "",saveCurrentDate = "";
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM,dd,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime= currentTime.format(calendar.getTime());

        Map messageTextBody = new HashMap();
        messageTextBody.put("userId",currentUserId);
        messageTextBody.put("date",saveCurrentDate);
        messageTextBody.put("feedBackId",feedBackKeyId);
        messageTextBody.put("name",currentUserName);
        messageTextBody.put("time",saveCurrentTime);
        messageTextBody.put("Feedback",text);

        Map finalDetails = new HashMap();
        String refPath = "Feedbacks" + "/" + feedBackKeyId;
        finalDetails.put(refPath,messageTextBody);
        databaseRootRef.updateChildren(finalDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Log.d("rock", "onComplete: Thanks For Your Feedback");
                    Toast.makeText(FeedbackActivity.this, "Thanks For Your Feedback", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d("rock", "onComplete: Error in sending feedback! : " + task.getException().getMessage());
                    Toast.makeText(FeedbackActivity.this, "Error in sending feedback! : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

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
