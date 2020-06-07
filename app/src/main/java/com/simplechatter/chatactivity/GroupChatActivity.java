package com.simplechatter.chatactivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simplechatter.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {

    private ImageView sendButton;
    private ScrollView scrollView;
    private Toolbar toolbar;
    private String groupName,currentUserName,currentDate,currentTime,uniqueGrpId;
    private boolean backPressed = false;
    private EditText editText;
    private TextView textView;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private String currentUserid;
    private DatabaseReference userRef,groupRef,groupMessageKeyref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        sendButton = (ImageView) findViewById(R.id.SetUpUserActivity_imageView);
        scrollView = (ScrollView)findViewById(R.id.GroupChatActivity_scrollView);
        toolbar = (Toolbar)findViewById(R.id.GroupChatActivity_appBar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        groupName = intent.getStringExtra("grp_name");
        uniqueGrpId = intent.getStringExtra("grp_id");

        getSupportActionBar().setTitle(groupName);
        textView = (TextView)findViewById(R.id.GroupChatActivity_textView);editText = (EditText)findViewById(R.id.GroupChatActivity_editText);
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();currentUserid = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(uniqueGrpId);
        currentUserName = mAuth.getCurrentUser().getDisplayName();
        getUserinfo();
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null)
            updateUserOnlineStatus("online");
        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if(dataSnapshot.exists())
                        displayMessages(dataSnapshot);

                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }



            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        displayMessages(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    public void getUserinfo()
    {
        userRef.child(currentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                else
                    Toast.makeText(GroupChatActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }
    public boolean onlySpaces(String text)
    {
        for(int x = 0;x < text.length();x++)
            if(text.charAt(x) != ' ')
                return false;
        return true;
    }

    public void sendChat(View View)
    {
        String text = editText.getText().toString();
        if(text.isEmpty() || onlySpaces(text))
            return;
        else
            saveMessageToFireBase(text);

        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }
    public void saveMessageToFireBase(String msg)
    {
        String messageKey = groupRef.push().getKey();

        Calendar calender = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = simpleDateFormat.format(calender.getTime());

        calender = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm:ss a");
        currentTime = simpleDateFormat.format(calender.getTime());

        Map<String,Object> tempMap = new HashMap<>();
        groupRef.updateChildren(tempMap);
        groupMessageKeyref = groupRef.child(messageKey);

        Map<String,Object> map = new HashMap<>();
        map.put("name",currentUserName);
        map.put("message",msg);
        map.put("date",currentDate);
        map.put("time",currentTime);
        map.put("userId",currentUserid);
        groupMessageKeyref.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(GroupChatActivity.this, "Error In Sending Message! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("rock","Error In Sending Message! " + task.getException().getMessage());
                }else
                    editText.setText("");
            }
        });

        scrollView.fullScroll(ScrollView.FOCUS_DOWN);


    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext())
        {
            String msgUserName = (String)((DataSnapshot)(iterator.next())).getValue().toString();
            String msgContent = (String)((DataSnapshot)(iterator.next())).getValue().toString();
            String msgDate = (String)((DataSnapshot)(iterator.next())).getValue().toString();
            String msgTime = (String)((DataSnapshot)(iterator.next())).getValue().toString();
            String msgUserId = (String)((DataSnapshot)(iterator.next())).getValue().toString();

            textView.append(msgUserName + ":\n" + msgContent + "\n" + msgTime + "     " + msgDate + "\n\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
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
        String disp = "[" + saveCurrentDate + ", " + saveCurrentTime + ", " + state + ", " + currentUserId + "]" + " From HomeScreenActivity" ;
        Log.d("rock",disp);
        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("online_status").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        if(!backPressed){
            backPressed = true;
            super.onBackPressed();

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser() != null && !backPressed)
            updateUserOnlineStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAuth.getCurrentUser() != null && !backPressed)
            updateUserOnlineStatus("offline");
    }



}
