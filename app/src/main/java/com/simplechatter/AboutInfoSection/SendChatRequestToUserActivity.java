package com.simplechatter.AboutInfoSection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.simplechatter.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SendChatRequestToUserActivity extends AppCompatActivity {
    //Components
    private String recieveUserId = "",recieveUserPhoneNumber = "",recieveUserEmailAddress = "",senderUserId,recieveUserName = "";        //Senderuserid = Current user id
    private ProgressDialog progressDialog;
    private CircleImageView circleImageView;
    private Button requestButton,acceptbutton;
    private TextView nameTextView,displayNameTextView,aboutTextView,displayAboutTextView,credentialTextView,displayCredentialTextView;

    //Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseRootRef,receiveUserRef,chatRequestRef,contactsRef,notificationRef;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mAuth;
    private StorageReference storageRootref;

    //Others
    private String current_state;
    private boolean userHasPressedBackButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_chat_request_to_user);
        Intent intent = getIntent();
        recieveUserId = intent.getStringExtra("visit_user_id");Log.d("rock","Visited UserId " + recieveUserId);


        //Initializing Components
        circleImageView = (CircleImageView)findViewById(R.id.SendChatRequestToUserActivity_imageView);
        nameTextView = (TextView)findViewById(R.id.SendChatRequestToUserActivity_name_textView);displayNameTextView = (TextView)findViewById(R.id.SendChatRequestToUserActivity_userName_textView);
        aboutTextView = (TextView)findViewById(R.id.SendChatRequestToUserActivity_about_textView);displayAboutTextView = (TextView)findViewById(R.id.SendChatRequestToUserActivity_userAbout_textView);
        credentialTextView = (TextView)findViewById(R.id.SendChatRequestToUserActivity_credentialType_textView);displayCredentialTextView = (TextView)findViewById(R.id.SendChatRequestToUserActivity_displayCredentialType_textView);
        requestButton = (Button)findViewById(R.id.SendChatRequestToUserActivity_request_button);acceptbutton = (Button)findViewById(R.id.SendChatRequestToUserActivity_accept_button);
        progressDialog = new ProgressDialog(this);

        //Initialiazing Firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseRootRef = firebaseDatabase.getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageRootref = FirebaseStorage.getInstance().getReference();
        receiveUserRef = databaseRootRef.child("Users").child(recieveUserId);
        senderUserId = mAuth.getCurrentUser().getUid();
        chatRequestRef = databaseRootRef.child("ChatRequest");
        contactsRef = databaseRootRef.child("Contacts");
        notificationRef = databaseRootRef.child("Notifications");

        //Others
        current_state = "new";



        showProgressDialog("Retrieving User Details");requestButton.setEnabled(false);
        retrieveUserDetails();
    }
    public void retrieveUserDetails()
    {
        receiveUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();displayNameTextView.setText(name);Log.d("rock","User name is " + name);
                recieveUserName = dataSnapshot.child("name").getValue().toString();
                String about = dataSnapshot.child("about").getValue().toString();displayAboutTextView.setText(about);Log.d("rock","User about is " + about);
                String imageUrl = dataSnapshot.child("image").getValue().toString();Log.d("rock","User image is " + imageUrl);
                if(!imageUrl.equalsIgnoreCase(""))
                    Picasso.get().load(imageUrl).into(circleImageView);
                else{
                    Log.d("rock","Profile Image Not Set");
                    Picasso.get().load(R.drawable.userdefaultprofile).placeholder(R.drawable.userdefaultprofile).into(circleImageView);
                }


                if(dataSnapshot.hasChild("phone") && !dataSnapshot.child("phone").getValue().toString().equalsIgnoreCase("")){
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    credentialTextView.setText("Phone Number");
                    displayCredentialTextView.setText(phone);
                    recieveUserPhoneNumber = phone;
                }else if(dataSnapshot.hasChild("email") && !dataSnapshot.child("email").getValue().toString().equalsIgnoreCase("")){
                    String email = dataSnapshot.child("email").getValue().toString();
                    credentialTextView.setText("Email");
                    displayCredentialTextView.setText(email);
                    recieveUserEmailAddress = email;
                }

                manageChatRequest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void manageChatRequest() {
        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(recieveUserId)){
                    String request_type = dataSnapshot.child(recieveUserId).child("request_type").getValue().toString();
                    Log.d("rock","Request Type is " + request_type);
                    if(request_type.equalsIgnoreCase("send")){
                        current_state = "request_send";
                        requestButton.setText("Cancel Request");
                    }else if(request_type.equalsIgnoreCase("received")){
                        current_state = "request_received";
                        requestButton.setText("Accept Chat Request");
                        acceptbutton.setVisibility(View.VISIBLE);
                        acceptbutton.setEnabled(true);
                        acceptbutton.setText("Decline Chat Request");
                        acceptbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });

                    }
                }else{
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(recieveUserId)){
                                current_state = "friends";
                                requestButton.setText("Remove This Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        progressDialog.dismiss();requestButton.setEnabled(true);
        if(!senderUserId.equalsIgnoreCase(recieveUserId)){
            requestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("rock","Current State is " + current_state);
                    requestButton.setEnabled(false);
                    if(current_state.equalsIgnoreCase("new")){
                        showProgressDialog("Sending Request");
                        sendChatRequest();
                    }else if(current_state.equalsIgnoreCase("request_send")){
                        showProgressDialog("Cancelling Request");
                        cancelChatRequest();
                    }else if(current_state.equalsIgnoreCase("request_received")){
                        showProgressDialog("Accepting Request");
                        acceptChatRequest();
                    }else if(current_state.equalsIgnoreCase("friends")){
                        Log.d("rock","inside alter dialog Builder!");
                        AlertDialog builder = new AlertDialog.Builder(SendChatRequestToUserActivity.this)
                                .setTitle("Are You Sure Want To Unfriend " + recieveUserName)
                                .setMessage("You Will be removed From " + recieveUserName + "'s friend list!")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        showProgressDialog("Removing Contact");
                                        Log.d("rock","Posiive button tapped!");
                                        removeSpecificContact();
                                    }
                                }).setNegativeButton("Never", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(SendChatRequestToUserActivity.this, "Never Say Never ", Toast.LENGTH_SHORT).show();
                                        Log.d("rock","User Not Removed!");
                                        Log.d("rock","Negative button tapped!");
                                        requestButton.setEnabled(true);
                                    }
                                }).show();
                    }
                }
            });
        }else{
            requestButton.setEnabled(false);
            requestButton.setVisibility(View.INVISIBLE);
            acceptbutton.setEnabled(false);
            acceptbutton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact() {

        requestButton.setEnabled(false);
        contactsRef.child(senderUserId).child(recieveUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactsRef.child(recieveUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SendChatRequestToUserActivity.this, "Now You Both Dont Know Each other ", Toast.LENGTH_LONG).show();
                                Log.d("rock","Contact removed unfriended");

                                current_state = "new";
                                acceptbutton.setVisibility(View.INVISIBLE);acceptbutton.setEnabled(false);
                                requestButton.setEnabled(true);progressDialog.dismiss();requestButton.setText("Send Request");
                            }else{
                                Toast.makeText(SendChatRequestToUserActivity.this, "Some Error Occured! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.d("rock","Some Error Occured! " + task.getException().getMessage());
                                requestButton.setEnabled(true);progressDialog.dismiss();
                            }
                        }
                    });
                }else{
                    Toast.makeText(SendChatRequestToUserActivity.this, "Some Error Occured! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("rock","Some Error Occured! " + task.getException().getMessage());
                    requestButton.setEnabled(true);progressDialog.dismiss();
                }
            }
        });

    }

    private void acceptChatRequest() {
        contactsRef.child(senderUserId).child(recieveUserId).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactsRef.child(recieveUserId).child(senderUserId).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                chatRequestRef.child(senderUserId).child(recieveUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        chatRequestRef.child(recieveUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(SendChatRequestToUserActivity.this, "All Set", Toast.LENGTH_SHORT).show();
                                                    Log.d("rock","All set");requestButton.setEnabled(true);requestButton.setText("Remove This Contact");
                                                    acceptbutton.setVisibility(View.INVISIBLE);acceptbutton.setEnabled(false);
                                                    current_state = "friends";
                                                    progressDialog.dismiss();
                                                }else{
                                                    Toast.makeText(SendChatRequestToUserActivity.this, "Some Error Occured! " + task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                                                    Log.d("rock","Some Error Occured! " + task.getException().getMessage());
                                                    progressDialog.dismiss();requestButton.setEnabled(true);
                                                }

                                            }
                                        });
                                    }
                                });
                            }else{
                                Toast.makeText(SendChatRequestToUserActivity.this, "Some Error Occured! " + task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                                Log.d("rock","Some Error Occured! " + task.getException().getMessage());
                                progressDialog.dismiss();requestButton.setEnabled(true);
                            }
                        }
                    });
                }else{
                    Toast.makeText(SendChatRequestToUserActivity.this, "Some Error Occured! " + task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                    Log.d("rock","Some Error Occured! " + task.getException().getMessage());
                    progressDialog.dismiss();requestButton.setEnabled(true);
                }
            }
        });
    }

    private void cancelChatRequest() {
        requestButton.setEnabled(false);showProgressDialog("Cancelling Request");
        chatRequestRef.child(senderUserId).child(recieveUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    chatRequestRef.child(recieveUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SendChatRequestToUserActivity.this, "Request SuccessFully Cancelled ", Toast.LENGTH_LONG).show();
                                Log.d("rock","Request SuccessFully Cancelled ");
                                requestButton.setText("Send Request");
                                current_state = "new";
                                acceptbutton.setVisibility(View.INVISIBLE);acceptbutton.setEnabled(false);
                                requestButton.setEnabled(true);progressDialog.dismiss();
                            }else{
                                Toast.makeText(SendChatRequestToUserActivity.this, "Cancel Request Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.d("rock","Cancel Request failed! " + task.getException().getMessage());
                                requestButton.setEnabled(true);progressDialog.dismiss();
                            }
                        }
                    });
                }else{
                    Toast.makeText(SendChatRequestToUserActivity.this, "Cancel Request Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("rock","Cancel Request failed! " + task.getException().getMessage());
                    requestButton.setEnabled(true);progressDialog.dismiss();
                }
            }
        });
    }

    private void sendChatRequest() {

        chatRequestRef.child(senderUserId).child(recieveUserId).child("request_type").setValue("send").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(recieveUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                HashMap<String,String> map = new HashMap<>();
                                map.put("from",senderUserId);
                                map.put("type","request");
                                notificationRef.child(recieveUserId).push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            requestButton.setEnabled(true);progressDialog.dismiss();
                                            current_state = "request_send";
                                            requestButton.setText("Cancel Request");
                                        }else{
                                            Log.d("rock","Error in Sending Chat Request!: " + task.getException().getMessage());
                                            Toast.makeText(SendChatRequestToUserActivity.this, "Sending Chat Request Failed!: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });


                            }else{
                                Toast.makeText(SendChatRequestToUserActivity.this, "Receiving Request Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.d("rock","Receiving Request failed! " + task.getException().getMessage());
                                progressDialog.dismiss();requestButton.setEnabled(true);
                            }
                        }
                    });
                }else{
                    Toast.makeText(SendChatRequestToUserActivity.this, "Sending Request Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("rock","Sending Request failed! " + task.getException().getMessage());
                    progressDialog.dismiss();requestButton.setEnabled(true);
                }
            }
        });
    }
    
    
    
    public void showProgressDialog(String s)
    {
        progressDialog.setTitle(s);
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
        super.onBackPressed();
        userHasPressedBackButton = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser() != null && !userHasPressedBackButton)
            updateUserOnlineStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAuth.getCurrentUser() != null && !userHasPressedBackButton)
            updateUserOnlineStatus("offline");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null)
            updateUserOnlineStatus("online");
    }
}
