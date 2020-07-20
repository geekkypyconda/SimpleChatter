package com.simplechatter.chatactivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.simplechatter.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class OneToOneChatActivity extends AppCompatActivity {


    //Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseRootRef,userContactsRef,UsersRef,ContactRef,ChatRef;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mAuth;
    private StorageReference storageRootref;
    private StorageTask storageTask;
    private String currentUserId = "";

    //Activity Components
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private String receiverImageUrl;
    private String checker;
    private String myUrl;
    private RecyclerView recyclerView;
    private boolean userHasPressedBackButton = false;
    private CircleImageView sendButton,circleImageView;
    private Toolbar chatToolbar;
    private ImageButton sendFilesButton;
    private EditText editText;
    private TextView displayReceiverNameTextView,displayReceiverLastSeenTextView;
    private List<Message> messageList = new ArrayList<>();
    private ArrayList<String> messageIdArrayList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private int IMAGE_REQUEST_CODE = 1,PDF_REQUEST_CODE = 2,DOCUMENT_REQUEST_CODE = 3;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_to_one_chat);

        //Initializing FireBase Components
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseRootRef = firebaseDatabase.getReference();
        storageRootref = firebaseStorage.getReference();
        currentUserId = mAuth.getCurrentUser().getUid();
        userContactsRef = databaseRootRef.child("Contacts").child(currentUserId);
        ContactRef = databaseRootRef.child("Contacts");
        UsersRef = databaseRootRef.child("Users");
        ChatRef = ContactRef.child(currentUserId);

        //Initializing sender and receiver
        Intent intent = getIntent();
        receiverId = intent.getStringExtra("visit_user_id");
        Log.d("rock","Received user Id is " + receiverId);
        receiverName = intent.getStringExtra("visit_user_name");Log.d("rock","Received user Name is " + receiverName);
        receiverImageUrl = intent.getStringExtra("image_url");Log.d("rock","Received user Name is " + receiverImageUrl);
        senderId = mAuth.getCurrentUser().getUid();
        senderName = mAuth.getCurrentUser().getDisplayName();


        //Initializing Activity Components
        recyclerView = (RecyclerView)findViewById(R.id.OneToOneChatActivity_recyclerView);
        chatToolbar = (Toolbar)findViewById(R.id.OneToOneChatActivity_customChatBarLayout);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        sendButton = (CircleImageView)findViewById(R.id.OneToOneChatActivity_imageView);
        editText = (EditText)findViewById(R.id.OneToOneChatActivity_editText);
        displayReceiverNameTextView = (TextView)findViewById(R.id.CustomChatBar_displayUserName_textView);displayReceiverNameTextView.setText(receiverName);
        displayReceiverLastSeenTextView = (TextView)findViewById(R.id.CustomChatBar_lastSeen_textView);
        circleImageView = (CircleImageView)findViewById(R.id.CustomChatBar_circleImageView);
        Picasso.get().load(receiverImageUrl).placeholder(R.drawable.userdefaultprofile).into(circleImageView);

        messagesAdapter = new MessagesAdapter(messageList);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messagesAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        sendFilesButton = (ImageButton)findViewById(R.id.OneToOneChatActivity_filesIcon_imageButton);
        checker = "none";myUrl = "none";
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");progressDialog.setCanceledOnTouchOutside(false);progressDialog.setMessage("0 % Uploaded");


        //Onclick Listeners
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString();
                saveMessageInFirebase(msg);
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }
        });

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{"Image","Pdf","Document"};
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(OneToOneChatActivity.this);
                alertDialog.setTitle("Select");alertDialog.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            checker = "Image";

                            Intent photoIntent = new Intent();
                            photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                            photoIntent.setType("image/*");
                            startActivityForResult(photoIntent.createChooser(photoIntent,"Select Image"),IMAGE_REQUEST_CODE);

                        }else if(which == 1){
                            checker = "Pdf";
                            Intent photoIntent = new Intent();
                            photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                            photoIntent.setType("application/pdf");
                            startActivityForResult(photoIntent.createChooser(photoIntent,"Select Pdf"),PDF_REQUEST_CODE);
                        }else if(which == 2){
                            checker = "Document";
                            Intent photoIntent = new Intent();
                            photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                            photoIntent.setType("application/*");
                            startActivityForResult(photoIntent.createChooser(photoIntent,"Select Document"),DOCUMENT_REQUEST_CODE);
                        }
                    }
                });alertDialog.show();
            }
        });

        displayLastSeen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("rock","Request Code " + requestCode + ", Result Code is " + resultCode);
        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            if(checker.equalsIgnoreCase("Image")){
                StorageReference imageMessagesRef = FirebaseStorage.getInstance().getReference().child("Image Messages");
                progressDialog.setMessage("");progressDialog.show();
                final String messageSenderRef = "Messages/" + senderId + "/" + receiverId;
                final String messageRecieverRef = "Messages/" + receiverId + "/" + senderId;

                DatabaseReference messageKeyRef = databaseRootRef.child("Messages").child(senderId).child(receiverId).push();
                final String messageKeyId = messageKeyRef.getKey();

                final StorageReference filePath = imageMessagesRef.child(messageKeyId + ".jpg");
                imageUri = data.getData();
                storageTask = filePath.putFile(imageUri);

                storageTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            Toast.makeText(OneToOneChatActivity.this, "Image Uploading Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("rock","Image Uploading Failed! " + task.getException().getMessage());
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            myUrl = task.getResult().toString();

                            String saveCurrentTime,saveCurrentDate;
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("MMM,dd,yyyy");
                            saveCurrentDate = currentDate.format(calendar.getTime());

                            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                            saveCurrentTime= currentTime.format(calendar.getTime());

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("type","image");
                            messageTextBody.put("from",senderId);
                            messageTextBody.put("to",receiverId);
                            messageTextBody.put("messageId",messageKeyId);
                            messageTextBody.put("date",saveCurrentDate);
                            messageTextBody.put("time",saveCurrentTime);

                            Map messageDetails = new HashMap();
                            messageDetails.put(messageSenderRef + "/" + messageKeyId,messageTextBody);
                            messageDetails.put(messageRecieverRef + "/" + messageKeyId,messageTextBody);

                            databaseRootRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        Log.d("rock","Message Sucessfully Saved in Firebase");
                                        editText.setEnabled(true);editText.setText("");
                                        sendButton.setEnabled(true);
                                        progressDialog.dismiss();
                                    }

                                    else{
                                        Log.d("rock","Message Saving Failed! " + task.getException().getMessage());
                                        Toast.makeText(OneToOneChatActivity.this, "Message Sending Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        editText.setEnabled(true);
                                        sendButton.setEnabled(true);
                                        progressDialog.dismiss();
                                    }
                                }
                            });


                        }else{
                            Toast.makeText(OneToOneChatActivity.this, "Image Uploading Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("rock","Image Uploading Failed! " + task.getException().getMessage());
                            progressDialog.dismiss();
                        }
                    }
                });

            }
        }else if(requestCode == PDF_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            StorageReference imageMessagesRef = FirebaseStorage.getInstance().getReference().child("Pdf Files");
            progressDialog.setMessage("");progressDialog.show();
            final String messageSenderRef = "Messages/" + senderId + "/" + receiverId;
            final String messageRecieverRef = "Messages/" + receiverId + "/" + senderId;

            DatabaseReference messageKeyRef = databaseRootRef.child("Messages").child(senderId).child(receiverId).push();
            final String messageKeyId = messageKeyRef.getKey();
            Uri fileUri = data.getData();
            final StorageReference filePath = imageMessagesRef.child(messageKeyId + ".pdf");
            filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                       filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                myUrl = uri.toString();

                                String saveCurrentTime,saveCurrentDate;
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat currentDate = new SimpleDateFormat("MMM,dd,yyyy");
                                saveCurrentDate = currentDate.format(calendar.getTime());

                                SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                                saveCurrentTime= currentTime.format(calendar.getTime());

                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message",myUrl);
                                messageTextBody.put("type","pdf");
                                messageTextBody.put("from",senderId);
                                messageTextBody.put("to",receiverId);
                                messageTextBody.put("messageId",messageKeyId);
                                messageTextBody.put("date",saveCurrentDate);
                                messageTextBody.put("time",saveCurrentTime);

                                Map messageDetails = new HashMap();
                                messageDetails.put(messageSenderRef + "/" + messageKeyId,messageTextBody);
                                messageDetails.put(messageRecieverRef + "/" + messageKeyId,messageTextBody);

                                databaseRootRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if(task.isSuccessful()){
                                            Log.d("rock","PDF Uploaded Successfully!");
                                            progressDialog.dismiss();
                                        }

                                        else{
                                            Log.d("rock","PDF Uploaded Failed!: " + task.getException().getMessage());
                                            Toast.makeText(OneToOneChatActivity.this, "PDF Uploaded Failed!: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }else{
                        Toast.makeText(OneToOneChatActivity.this, "Pdf Uploading Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("rock","Pdf Uploading Failed! " + task.getException().getMessage());
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(OneToOneChatActivity.this, "Something went wrong: " + e.getMessage() + "!", Toast.LENGTH_SHORT).show();
                    Log.d("rock","Something went wrong: " + e.getMessage() + "!");
                    progressDialog.dismiss();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double p = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage(p + " % Uploaded");
                }
            });

        }else if(requestCode == DOCUMENT_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            StorageReference imageMessagesRef = FirebaseStorage.getInstance().getReference().child("Document Files");
            progressDialog.setMessage("");progressDialog.show();
            final String messageSenderRef = "Messages/" + senderId + "/" + receiverId;
            final String messageRecieverRef = "Messages/" + receiverId + "/" + senderId;

            DatabaseReference messageKeyRef = databaseRootRef.child("Messages").child(senderId).child(receiverId).push();
            final String messageKeyId = messageKeyRef.getKey();
            Uri fileUri = data.getData();
            final StorageReference filePath = imageMessagesRef.child(messageKeyId + ".docx");
            filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                       filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                           @Override
                           public void onSuccess(Uri uri) {
                               myUrl = uri.toString();

                               String saveCurrentTime,saveCurrentDate;
                               Calendar calendar = Calendar.getInstance();
                               SimpleDateFormat currentDate = new SimpleDateFormat("MMM,dd,yyyy");
                               saveCurrentDate = currentDate.format(calendar.getTime());

                               SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                               saveCurrentTime= currentTime.format(calendar.getTime());

                               Map messageTextBody = new HashMap();
                               messageTextBody.put("message",myUrl);
                               messageTextBody.put("type","document");
                               messageTextBody.put("from",senderId);
                               messageTextBody.put("to",receiverId);
                               messageTextBody.put("messageId",messageKeyId);
                               messageTextBody.put("date",saveCurrentDate);
                               messageTextBody.put("time",saveCurrentTime);

                               Map messageDetails = new HashMap();
                               messageDetails.put(messageSenderRef + "/" + messageKeyId,messageTextBody);
                               messageDetails.put(messageRecieverRef + "/" + messageKeyId,messageTextBody);

                               databaseRootRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener() {
                                   @Override
                                   public void onComplete(@NonNull Task task) {
                                       if(task.isSuccessful()){
                                           Log.d("rock","Document Uploaded Successfully!");
                                           progressDialog.dismiss();
                                       }

                                       else{
                                           Log.d("rock","Document Uploaded Failed!: " + task.getException().getMessage());
                                           Toast.makeText(OneToOneChatActivity.this, "Document Uploaded Failed!: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                           progressDialog.dismiss();
                                       }
                                   }
                               });
                           }
                       });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(OneToOneChatActivity.this, "Something went wrong: " + e.getMessage() + "!", Toast.LENGTH_SHORT).show();
                    Log.d("rock","Something went wrong: " + e.getMessage() + "!");
                    progressDialog.dismiss();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double p = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage(p + " % Uploaded");
                }
            });
        }
        else{
            Log.d("rock","Some Problem Occured in Parsing The Document!");
            Toast.makeText(this, "Some Problem Occured in Parsing The Document!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            updateUserOnlineStatus("online");
            Log.d("rock","Making User Online from OneToOneChatActivity in onStart() current backPressed State is " + userHasPressedBackButton);
            messageList.clear();
            messageIdArrayList.clear();
            displayLastSeen();
        }

        databaseRootRef.child("Messages").child(senderId).child(receiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Message message = dataSnapshot.getValue(Message.class);
                if(!messageList.contains(message) && !messageIdArrayList.contains(message.getMessageId())) {
                    messageList.add(message);messageIdArrayList.add(message.getMessageId());
                    messagesAdapter.notifyDataSetChanged();
                }

                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                displayLastSeen();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
        displayLastSeen();
    }

    private void saveMessageInFirebase(String msg)
    {
        if(msg.equals("") || msg.isEmpty())
            return;
        else if(msg.length() > 10000000){
            Log.d("rock","Message Too Long!");
            Toast.makeText(this, "Message Too Long", Toast.LENGTH_SHORT).show();
            return;
        }else{
            displayLastSeen();
            editText.setEnabled(false);
            sendButton.setEnabled(false);
            String messageSenderRef = "Messages/" + senderId + "/" + receiverId;
            String messageRecieverRef = "Messages/" + receiverId + "/" + senderId;

            DatabaseReference messageKeyRef = databaseRootRef.child("Messages").child(senderId).child(receiverId).push();
            String messageKeyId = messageKeyRef.getKey();

            String saveCurrentTime,saveCurrentDate;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM,dd,yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            saveCurrentTime= currentTime.format(calendar.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",msg);
            messageTextBody.put("type","text");
            messageTextBody.put("from",senderId);
            messageTextBody.put("to",receiverId);
            messageTextBody.put("messageId",messageKeyId);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("time",saveCurrentTime);

            Map messageDetails = new HashMap();
            messageDetails.put(messageSenderRef + "/" + messageKeyId,messageTextBody);
            messageDetails.put(messageRecieverRef + "/" + messageKeyId,messageTextBody);

            databaseRootRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Log.d("rock","Message Sucessfully Saved in Firebase");
                        editText.setEnabled(true);editText.setText("");
                        sendButton.setEnabled(true);
                    }


                    else{
                        Log.d("rock","Message Saving Failed! " + task.getException().getMessage());
                        Toast.makeText(OneToOneChatActivity.this, "Message Sending Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        editText.setEnabled(true);
                        sendButton.setEnabled(true);
                    }
                }
            });

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

    private void displayLastSeen()
    {
        databaseRootRef.child("Users").child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("online_status")){
                    String state = dataSnapshot.child("online_status").child("current_status").getValue().toString();
                    String date = dataSnapshot.child("online_status").child("current_date").getValue().toString();
                    String time = dataSnapshot.child("online_status").child("current_time").getValue().toString();

                    String userSendAbout = "Last Seen: " + date + "\n" + time;
                    if(state.equalsIgnoreCase("online")){

                        displayReceiverLastSeenTextView.setText("online");
                        boolean isOnline = true;
                    }else
                        displayReceiverLastSeenTextView.setText(userSendAbout);
                }else
                    displayReceiverLastSeenTextView.setText("offline");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        userHasPressedBackButton = true;
        super.onBackPressed();


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



}
