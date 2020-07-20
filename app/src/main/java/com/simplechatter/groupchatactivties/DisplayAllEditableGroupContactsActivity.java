package com.simplechatter.groupchatactivties;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.simplechatter.classes.Contact;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class DisplayAllEditableGroupContactsActivity extends AppCompatActivity {

    //Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseRootRef,groupContactsRef,UsersRef,groupRef,usersContactRef,groupMemberRef;
    private FirebaseStorage firebaseStorage;
    private boolean boolOne = false,userHasMoven;
    private String TAG = "rock";
    private FirebaseAuth mAuth;
    private StorageReference storageRootref;
    private String currentUserId = "",currentGroupId = "",currentGroupName,mode,currentGroupImage,currentGroupMotto;
    private  ArrayList<String> members = new ArrayList<>();
    private  HashSet<String> set = new HashSet<>();
    //Activity Components
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private CircleImageView toolbarCircleImageView;
    private Button cancelButton,addButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_all_editable_group_contacts);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseRootRef = firebaseDatabase.getReference();
        storageRootref = firebaseStorage.getReference();
        currentUserId = mAuth.getCurrentUser().getUid();
        groupContactsRef = databaseRootRef.child("Group_Contacts").child(currentUserId);
        UsersRef = databaseRootRef.child("Users");
        usersContactRef = databaseRootRef.child("Contacts").child(currentUserId);
        groupRef = databaseRootRef.child("Groups");

        Intent intent = getIntent();
        currentGroupId = intent.getStringExtra("grpId");
        currentGroupName = intent.getStringExtra("grpName");
        mode = intent.getStringExtra("mode");
        currentGroupImage = intent.getStringExtra("grpImage");
        currentGroupMotto = intent.getStringExtra("grpMotto");
        Log.d(TAG,"Some Group Info " + currentGroupId + " " + mode);
        groupMemberRef = databaseRootRef.child("Groups").child(currentGroupId).child("Members");

        //Activity Components
        recyclerView = (RecyclerView)findViewById(R.id.DisplayAllEditableGroupContactsActivity_RecyclerView);recyclerView.setLayoutManager(new LinearLayoutManager(this));
        toolbar = (Toolbar)findViewById(R.id.DisplayAllEditableGroupContactsActivity_appBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle("");
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);
        toolbarCircleImageView = (CircleImageView)findViewById(R.id.CustomChatBar_circleImageView);
        progressDialog = new ProgressDialog(this);

        if(currentGroupImage != null &!currentGroupImage.equalsIgnoreCase(""))
            Picasso.get().load(currentGroupImage).placeholder(R.drawable.defaultgroupicon).into(toolbarCircleImageView);
        else
            Picasso.get().load(R.drawable.defaultgroupicon).placeholder(R.drawable.defaultgroupicon).into(toolbarCircleImageView);

        TextView groupNameTextView = (TextView)findViewById(R.id.CustomChatBar_displayUserName_textView);groupNameTextView.setText("Add member to : " + currentGroupName);
        groupNameTextView.setTextSize(18);
        TextView groupAboutTextView = (TextView)findViewById(R.id.CustomChatBar_lastSeen_textView);groupAboutTextView.setText(currentGroupMotto);

        addButton = (Button)findViewById(R.id.DisplayAllEditableGroupContactsActivity_AddButton);
        cancelButton = (Button)findViewById(R.id.DisplayAllEditableGroupContactsActivity_cancelButton);
        if(mode.equalsIgnoreCase("remove"))
            addButton.setText("Remove");

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode.equalsIgnoreCase("add")){
                    if(members.size() == 0 || members.isEmpty()){
                        Log.d(TAG,"You haven't selected Any member");
                        Toast.makeText(DisplayAllEditableGroupContactsActivity.this, "You haven't selected Any member", Toast.LENGTH_SHORT).show();
                    }
                    else
                        confirmSubmitAndAdd("Are you sure want to add these members?","No! Let me check again",1);
                }else if(mode.equalsIgnoreCase("remove")){
                    if(members.size() == 0 || members.isEmpty()){
                        Log.d(TAG,"You haven't selected Any member");
                        Toast.makeText(DisplayAllEditableGroupContactsActivity.this, "You haven't selected Any member", Toast.LENGTH_SHORT).show();
                    }
                    else
                        confirmSubmitAndAdd("Are you sure want to remove these members?","No! Let me check again",0);
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSubmitAndAdd("Are you sure want to exit without adding members?","Never",0);

            }
        });

    }

    public void confirmSubmitAndAdd(String s, String ne, final int mode)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(s);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {                                                      //Mode 1 : Add Button, Mode 2 : Cancel Button
                if(mode == 1){
                    addMembersInGroup();
                    moveToGroupChatActivity(currentGroupId,currentGroupName,currentGroupMotto,currentGroupImage);
                }else{
                    removeMembersFromGroup();
                    moveToGroupChatActivity(currentGroupId,currentGroupName,currentGroupMotto,currentGroupImage);
                }
            }
        });
        alertDialog.setNegativeButton(ne, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }

    private void removeMembersFromGroup() {
        showProgressDialog("Removing Members");disableEveryThing();
        for(int x = 0;x < members.size();x++)
            removeMembersFromGroupUtil(members.get(x));
        progressDialog.dismiss();enableEveryThing();
    }

    public void addMembersInGroup()
    {
        showProgressDialog("Adding Members");disableEveryThing();
        for(int x = 0;x < members.size();x++)
            addMemberInGroupUtil(members.get(x));
        progressDialog.dismiss();enableEveryThing();
    }

    public void removeMembersFromGroupUtil(final String memberId)
    {
        showProgressDialog("Adding Members");disableEveryThing();
        databaseRootRef.child("Groups").child(currentGroupId).child("Members").child(memberId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    databaseRootRef.child("Group_Contacts").child(memberId).child(currentGroupId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG,"Members Removed Successfully");
                                Toast.makeText(DisplayAllEditableGroupContactsActivity.this, "Members Removed Successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                Log.d(TAG,"Error in Removing member! : " + task.getException().getMessage());
                                Toast.makeText(DisplayAllEditableGroupContactsActivity.this, "Error in Removing member! : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    Log.d(TAG,"Members Removed Successfully");
                    Toast.makeText(DisplayAllEditableGroupContactsActivity.this, "Members Removed Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void addMemberInGroupUtil(final String memberId)
    {
        showProgressDialog("Adding Members");disableEveryThing();
        databaseRootRef.child("Groups").child(currentGroupId).child("Members").child(memberId).child("status").setValue("member").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    databaseRootRef.child("Group_Contacts").child(memberId).child(currentGroupId).child("Contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()){
                                Log.d(TAG,"Error in Adding member! : " + task.getException().getMessage());
                                Toast.makeText(DisplayAllEditableGroupContactsActivity.this, "Error in Adding member! : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }else{
                                Log.d(TAG,"Members Added Successfully");
                                Toast.makeText(DisplayAllEditableGroupContactsActivity.this, "Members Added Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    Log.d(TAG,"Members Adding Failed! " + task.getException().getMessage());
                    Toast.makeText(DisplayAllEditableGroupContactsActivity.this, "Members Adding Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserOnlineStatus("online");
        FirebaseRecyclerOptions<Contact> options = null;
        if(mode.equalsIgnoreCase("add"))
            options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(usersContactRef, Contact.class).build();
        else if(mode.equalsIgnoreCase("remove"))
            options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(groupMemberRef, Contact.class).build();

        FirebaseRecyclerAdapter<Contact, EditabeContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, EditabeContactsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final EditabeContactsViewHolder holder, int position, @NonNull Contact model) {
                Log.d("rock","insdie Adapter in Display acivity");
                final String userId = getRef(position).getKey();

                if(mode.equalsIgnoreCase("add")){
                    databaseRootRef.child("Groups").child(currentGroupId).child("Members").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(userId) || dataSnapshot.child(userId).getValue() == null)
                                return;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    Log.d(TAG,"BoolOne is " + boolOne);
                    if(!boolOne && !userId.equalsIgnoreCase(currentUserId)){
                        UsersRef.child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild("image") && !dataSnapshot.child("image").getValue().toString().equalsIgnoreCase("")){
                                    String imageUrl = dataSnapshot.child("image").getValue().toString();
                                    Picasso.get().load(imageUrl).placeholder(R.drawable.userdefaultprofile).into(holder.circleImageView);
                                }else
                                    Picasso.get().load(R.drawable.userdefaultprofile).placeholder(R.drawable.userdefaultprofile).into(holder.circleImageView);

                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileAbout = dataSnapshot.child("about").getValue().toString();
                                holder.userName.setText(profileName);holder.userAbout.setText(profileAbout);
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(members.contains(userId) && set.contains(userId)){
                                            holder.userDisplayRelativeLayout.setBackgroundColor(getResources().getColor(R.color.white));
                                            members.remove(members.indexOf(userId));
                                            set.remove(userId);
                                        }else{
                                            holder.userDisplayRelativeLayout.setBackgroundColor(getResources().getColor(R.color.greenSelectColor));
                                            members.add(userId);
                                            set.add(userId);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }else if(mode.equalsIgnoreCase("remove")){
                    UsersRef.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("image") && !dataSnapshot.child("image").getValue().toString().equalsIgnoreCase("")){
                                String imageUrl = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(imageUrl).placeholder(R.drawable.userdefaultprofile).into(holder.circleImageView);
                            }else
                                Picasso.get().load(R.drawable.userdefaultprofile).placeholder(R.drawable.userdefaultprofile).into(holder.circleImageView);
                            String profileName = dataSnapshot.child("name").getValue().toString();
                            String profileAbout = dataSnapshot.child("about").getValue().toString();
                            holder.userName.setText(profileName);holder.userAbout.setText(profileAbout);
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(members.contains(userId) && set.contains(userId)){
                                        holder.userDisplayRelativeLayout.setBackgroundColor(getResources().getColor(R.color.white));
                                        members.remove(members.indexOf(userId));
                                        set.remove(userId);
                                    }else{
                                        holder.userDisplayRelativeLayout.setBackgroundColor(getResources().getColor(R.color.greenSelectColor));
                                        members.add(userId);
                                        set.add(userId);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @NonNull
            @Override
            public EditabeContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                EditabeContactsViewHolder viewHolder = new EditabeContactsViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private boolean exitsInGroup(final String userId) {
        final boolean[] res = {false};
        databaseRootRef.child("Groups").child(currentGroupId).child("Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userId) && !dataSnapshot.child(userId).getValue().toString().equalsIgnoreCase(""))
                    res[0] = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d(TAG,"returning " + res[0] + " To exits group method");
        return res[0];
    }

    public class EditabeContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userAbout;
        String uid = "";
        CircleImageView circleImageView;
        RelativeLayout userDisplayRelativeLayout;
        public EditabeContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = (TextView)itemView.findViewById(R.id.UserDisplayLayout_userName_textView);              //Using User name for GroupName
            userAbout = (TextView)itemView.findViewById(R.id.UserDisplayLayout_userAbout_textView);            //Using User About for Group Motto
            circleImageView = (CircleImageView)itemView.findViewById(R.id.UserDisplayLayout_circleImageView);
            userDisplayRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.UserDisplayLayout_Main_relativeLayout);
        }
    }

    public void moveToGroupChatActivity(String grpId,String grpName,String grpMotto,String grpImage)
    {
        userHasMoven = true;
        Intent intent = new Intent(this,GroupChatActivity.class);
        intent.putExtra("grpId",grpId);intent.putExtra("grpName",grpName);
        intent.putExtra("grpMotto",grpMotto);intent.putExtra("grpImage",grpImage);
        startActivity(intent);
        finish();
    }


    public void showProgressDialog(String s)
    {
        progressDialog.setTitle(s);
        progressDialog.setMessage("Please Wait.....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void disableEveryThing()
    {
        cancelButton.setEnabled(false);
        addButton.setEnabled(false);
    }

    public void enableEveryThing()
    {
        cancelButton.setEnabled(true);
        addButton.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if(mode.equalsIgnoreCase("add"))
            confirmSubmitAndAdd("Are you sure want to exit without adding members?","Never",0);
        else if(mode.equalsIgnoreCase("remove"))
            confirmSubmitAndAdd("Are you sure want to exit without removing members?","Never",1);

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