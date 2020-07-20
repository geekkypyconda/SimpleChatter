package com.simplechatter.groupchatactivties;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
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
import com.simplechatter.chatactivity.Message;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private ImageView sendButton;
    private ScrollView scrollView;
    private Toolbar toolbar;
    private String groupName,currentUserName,currentDate,currentTime,uniqueGrpId,checker,uniqueGroupMotto,uniqueGroupImage;
    private boolean backPressed = false;
    private EditText editText;
    private ImageButton sendFilesButton;
    private Uri imageUri;
    private StorageTask storageTask;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private CircleImageView toolbarCircleImageView;
    private List<Message> messageList;
    private ArrayList<String> groupIdArrayList = new ArrayList<>(),messageIdArrayList = new ArrayList<>();
    private int IMAGE_REQUEST_CODE = 1,PDF_REQUEST_CODE = 2,DOCUMENT_REQUEST_CODE = 3;
    private GroupMessageAdapter groupMessageAdapter;
    private TextView textView;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private String currentUserid;
    private StorageReference storageRootRef;
    private DatabaseReference userRef,groupRef,groupMessageKeyref,databaseRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        sendFilesButton = (ImageButton) findViewById(R.id.GroupChatActivity_filesIcon_imageButton);
        sendButton = (ImageView) findViewById(R.id.GroupChatActivity_imageView);checker = "none";
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChat();
            }
        });
        progressDialog = new ProgressDialog(this);
        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{"image","pdf","document"};
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Select");builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            checker = "image";
                            Intent photoIntent = new Intent();
                            photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                            photoIntent.setType("image/*");
                            startActivityForResult(photoIntent.createChooser(photoIntent,"Select Image"),IMAGE_REQUEST_CODE);
                        }else if(which == 1){
                            checker = "pdf";
                            Intent photoIntent = new Intent();
                            photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                            photoIntent.setType("application/pdf");
                            startActivityForResult(photoIntent.createChooser(photoIntent,"Select Image"),PDF_REQUEST_CODE);
                        }else if(which == 2){
                            checker = "document";
                            Intent photoIntent = new Intent();
                            photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                            photoIntent.setType("application/*");
                            startActivityForResult(photoIntent.createChooser(photoIntent,"Select Image"),DOCUMENT_REQUEST_CODE);
                        }
                    }
                });builder.show();
            }
        });

        //scrollView = (ScrollView)findViewById(R.id.GroupChatActivity_scrollView);
        toolbar = (Toolbar)findViewById(R.id.GroupChatActivity_appBar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        //groupName = intent.getStringExtra("grp_name");
        uniqueGrpId = intent.getStringExtra("grpId");
        groupName = intent.getStringExtra("grpName");
        String groupMotto = intent.getStringExtra("grpMotto");uniqueGroupMotto = groupMotto;
        String receiverImageUrl = intent.getStringExtra("grpImage");uniqueGroupImage = receiverImageUrl;


        Log.d("rock","Name : " + groupName + " Motto : " + groupMotto + " ID " + uniqueGrpId + " Image " + receiverImageUrl);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);
        toolbarCircleImageView = (CircleImageView)findViewById(R.id.CustomChatBar_circleImageView);

        if(uniqueGroupImage != null &!uniqueGroupImage.equalsIgnoreCase(""))
            Picasso.get().load(uniqueGroupImage).placeholder(R.drawable.defaultgroupicon).into(toolbarCircleImageView);
        else
            Picasso.get().load(R.drawable.defaultgroupicon).placeholder(R.drawable.defaultgroupicon).into(toolbarCircleImageView);

        TextView displayGrpName = (TextView)findViewById(R.id.CustomChatBar_displayUserName_textView);
        displayGrpName.setText(groupName);
        TextView displayGrpMotto = (TextView)findViewById(R.id.CustomChatBar_lastSeen_textView);
        displayGrpMotto.setText(groupMotto);
        messageList = new ArrayList<>();


        editText = (EditText)findViewById(R.id.GroupChatActivity_editText);
        //textView = (TextView)findViewById(R.id.GroupChatActivity_textView);editText = (EditText)findViewById(R.id.GroupChatActivity_editText);
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();currentUserid = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Group_Messages").child(uniqueGrpId);
        databaseRootRef = FirebaseDatabase.getInstance().getReference();
        storageRootRef = FirebaseStorage.getInstance().getReference();
        currentUserName = mAuth.getCurrentUser().getDisplayName();
        recyclerView = (RecyclerView)findViewById(R.id.GroupChatActivity_recyclerView);
        currentUserid = mAuth.getCurrentUser().getUid();

        groupMessageAdapter = new GroupMessageAdapter(messageList);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(groupMessageAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");progressDialog.setCanceledOnTouchOutside(false);progressDialog.setMessage("0 % Uploaded");

        getUserinfo();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = this.getMenuInflater();
        mi.inflate(R.menu.group_chat_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.group_chat_menu_group_add_member:
                moveToDisplayAllEditableGroupContactsActivity(groupName,uniqueGrpId,"add",uniqueGroupImage,uniqueGroupMotto);
                break;
            case R.id.group_chat_menu_group_settings:
                moveToGroupSettingsActivity(groupName,uniqueGrpId,uniqueGroupImage,uniqueGroupMotto);
                break;
            case R.id.group_chat_menu_group_remove_member:
                moveToDisplayAllEditableGroupContactsActivity(groupName,uniqueGrpId,"remove",uniqueGroupImage,uniqueGroupMotto);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void moveToGroupSettingsActivity(String grpName,String grpId,String grpImage,String grpMotto)
    {
        backPressed = true;
        Intent intent = new Intent(GroupChatActivity.this,GroupSettingsActivity.class);
        intent.putExtra("grpName",grpName);intent.putExtra("grpId",grpId);
        intent.putExtra("grpImage",grpImage);intent.putExtra("grpMotto",grpMotto);
        startActivity(intent);
    }
    public void moveToDisplayAllEditableGroupContactsActivity(String grpName,String grpId,String mode,String grpImage,String grpMotto)
    {
        backPressed = true;
        Intent intent = new Intent(GroupChatActivity.this,DisplayAllEditableGroupContactsActivity.class);
        intent.putExtra("grpName",grpName);intent.putExtra("grpId",grpId);
        intent.putExtra("mode",mode);intent.putExtra("grpImage",grpImage);
        intent.putExtra("grpMotto",grpMotto);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("rock","Request Code " + requestCode + ", Result Code is " + resultCode);
        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            if(checker.equalsIgnoreCase("image")){
                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("Groups").child("Image Messages");
                progressDialog.setMessage("Uploading");progressDialog.show();
                final String senderMessageRef = "Group_Messages/" + uniqueGrpId;
                final DatabaseReference messageKeyRef = databaseRootRef.child("Group_Messages").child(uniqueGrpId).child(currentUserid).push();
                final String messageKeyId = messageKeyRef.getKey();
                final StorageReference filePath = imageRef.child(messageKeyId + ".jpg");

                imageUri = data.getData();storageTask = filePath.putFile(imageUri);
                storageTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            Toast.makeText(GroupChatActivity.this, "Image Uploading Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("rock","Image Uploading Failed! " + task.getException().getMessage());
                            progressDialog.dismiss();
                            return null;
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            String myUrl = task.getResult().toString();
                            String saveCurrentTime = "",saveCurrentDate = "";
                            Calendar calendar = Calendar.getInstance();

                            SimpleDateFormat currentDate = new SimpleDateFormat("MMM,dd,yyyy");
                            saveCurrentDate = currentDate.format(calendar.getTime());

                            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                            saveCurrentTime= currentTime.format(calendar.getTime());

                            Map messageTextBody = new HashMap();

                            messageTextBody.put("date",saveCurrentDate);
                            messageTextBody.put("groupId",uniqueGrpId);
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("messageId",messageKeyId);

                            messageTextBody.put("name",currentUserName);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("type","image");
                            messageTextBody.put("from",currentUserid);

                            Map messageDetails = new HashMap();
                            messageDetails.put(senderMessageRef + "/" + messageKeyId,messageTextBody);
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
                                        Toast.makeText(GroupChatActivity.this, "Message Sending Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        editText.setEnabled(true);
                                        sendButton.setEnabled(true);
                                        progressDialog.dismiss();
                                    }
                                }
                            });


                        }else{
                            Toast.makeText(GroupChatActivity.this, "Image Uploading Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("rock","Image Uploading Failed! " + task.getException().getMessage());
                            progressDialog.dismiss();
                        }
                    }
                });



            }
        }else if(requestCode == PDF_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            if(checker.equalsIgnoreCase("pdf")){
                StorageReference pdfRef = FirebaseStorage.getInstance().getReference().child("Groups").child("Pdf Files");
                progressDialog.setMessage("Uploading");progressDialog.show();
                final String senderMessageRef = "Group_Messages/" + uniqueGrpId;
                final DatabaseReference messageKeyRef = databaseRootRef.child("Group_Messages").child(uniqueGrpId).child(currentUserid).push();
                final String messageKeyId = messageKeyRef.getKey();
                final StorageReference filePath = pdfRef.child(messageKeyId + ".pdf");
                Uri fileUri = data.getData();
                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String myUrl = uri.toString();

                                    String saveCurrentTime = "",saveCurrentDate = "";
                                    Calendar calendar = Calendar.getInstance();

                                    SimpleDateFormat currentDate = new SimpleDateFormat("MMM,dd,yyyy");
                                    saveCurrentDate = currentDate.format(calendar.getTime());

                                    SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                                    saveCurrentTime= currentTime.format(calendar.getTime());

                                    Map messageTextBody = new HashMap();

                                    messageTextBody.put("date",saveCurrentDate);
                                    messageTextBody.put("groupId",uniqueGrpId);
                                    messageTextBody.put("message",myUrl);
                                    messageTextBody.put("messageId",messageKeyId);

                                    messageTextBody.put("name",currentUserName);
                                    messageTextBody.put("time",saveCurrentTime);
                                    messageTextBody.put("type","pdf");
                                    messageTextBody.put("from",currentUserid);

                                    Map messageDetails = new HashMap();
                                    messageDetails.put(senderMessageRef + "/" + messageKeyId,messageTextBody);

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
                                                Toast.makeText(GroupChatActivity.this, "Message Sending Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                editText.setEnabled(true);
                                                sendButton.setEnabled(true);
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }else {
                            Toast.makeText(GroupChatActivity.this, "PDF Uploading Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("rock","PDF Uploading Failed! " + task.getException().getMessage());
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupChatActivity.this, "Something went wrong: " + e.getMessage() + "!", Toast.LENGTH_SHORT).show();
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
        }else if(requestCode == DOCUMENT_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            if(checker.equalsIgnoreCase("document")){
                StorageReference pdfRef = FirebaseStorage.getInstance().getReference().child("Groups").child("Document Files");
                progressDialog.setMessage("Uploading");progressDialog.show();
                final String senderMessageRef = "Group_Messages/" + uniqueGrpId;
                final DatabaseReference messageKeyRef = databaseRootRef.child("Group_Messages").child(uniqueGrpId).child(currentUserid).push();
                final String messageKeyId = messageKeyRef.getKey();
                final StorageReference filePath = pdfRef.child(messageKeyId + ".docs");
                Uri fileUri = data.getData();
                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String myUrl = uri.toString();

                                    String saveCurrentTime = "",saveCurrentDate = "";
                                    Calendar calendar = Calendar.getInstance();

                                    SimpleDateFormat currentDate = new SimpleDateFormat("MMM,dd,yyyy");
                                    saveCurrentDate = currentDate.format(calendar.getTime());

                                    SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                                    saveCurrentTime= currentTime.format(calendar.getTime());

                                    Map messageTextBody = new HashMap();

                                    messageTextBody.put("date",saveCurrentDate);
                                    messageTextBody.put("groupId",uniqueGrpId);
                                    messageTextBody.put("message",myUrl);
                                    messageTextBody.put("messageId",messageKeyId);

                                    messageTextBody.put("name",currentUserName);
                                    messageTextBody.put("time",saveCurrentTime);
                                    messageTextBody.put("type","document");
                                    messageTextBody.put("from",currentUserid);

                                    Map messageDetails = new HashMap();
                                    messageDetails.put(senderMessageRef + "/" + messageKeyId,messageTextBody);

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
                                                Toast.makeText(GroupChatActivity.this, "Message Sending Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                editText.setEnabled(true);
                                                sendButton.setEnabled(true);
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }else {
                            Toast.makeText(GroupChatActivity.this, "Document Uploading Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("rock","Document Uploading Failed! " + task.getException().getMessage());
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupChatActivity.this, "Something went wrong: " + e.getMessage() + "!", Toast.LENGTH_SHORT).show();
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
        }else{
            Log.d("rock","Document Uploading failed!");
            Toast.makeText(this,"Document Uploading failed!",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            updateUserOnlineStatus("online");
            messageList.clear();
            groupIdArrayList.clear();
            messageIdArrayList.clear();
        }

        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                if(!messageList.contains(message) && !messageIdArrayList.contains(message.getMessageId())) {
                    messageList.add(message);messageIdArrayList.add(message.getMessageId());
                    groupMessageAdapter.notifyDataSetChanged();
                }

                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
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

    }
    public boolean onlySpaces(String text)
    {
        for(int x = 0;x < text.length();x++)
            if(text.charAt(x) != ' ')
                return false;
        return true;
    }

    public void sendChat()
    {
        String text = editText.getText().toString();
        if(text.isEmpty() || onlySpaces(text))
            return;
        else
            saveMessageToFireBase(text);

        //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
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
        map.put("type","text");
        map.put("messageId",messageKey);
        map.put("groupId",uniqueGrpId);
        map.put("from",currentUserid);
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

        //scrollView.fullScroll(ScrollView.FOCUS_DOWN);


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
            backPressed = true;
            super.onBackPressed();

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
