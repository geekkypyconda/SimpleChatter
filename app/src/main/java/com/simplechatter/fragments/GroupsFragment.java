package com.simplechatter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import com.simplechatter.classes.GroupContacts;
import com.simplechatter.groupchatactivties.GroupChatActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View view;
    private View groupFragmentView;
    private String temp = "";
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> arrayList,grpIDS;
    private String currentUserId;
    private ListView listView;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseRootRef,groupContactRef;
    private StorageReference storageRootRef;
    private boolean userHasMovenToGroupChatActivity = false;
    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        recyclerView = (RecyclerView)groupFragmentView.findViewById(R.id.GroupsFragment_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseRootRef = FirebaseDatabase.getInstance().getReference();
        storageRootRef = FirebaseStorage.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();
        groupContactRef = databaseRootRef.child("Group_Contacts").child(currentUserId);

        return groupFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("rock","Inside On Start");
        FirebaseRecyclerOptions<GroupContacts> options = new FirebaseRecyclerOptions.Builder<GroupContacts>().setQuery(groupContactRef, GroupContacts.class).build();
        Log.d("rock","Inside On Start 2");
        FirebaseRecyclerAdapter<GroupContacts,GroupChatViewHolder> adapter = new FirebaseRecyclerAdapter<GroupContacts, GroupChatViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final GroupChatViewHolder holder, int position, @NonNull GroupContacts model) {

                final String grpIds = getRef(position).getKey();Log.d("rock","Group id is " + grpIds);
                final String imageUri[] = {""};
                databaseRootRef.child("Groups").child(grpIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("image") && !dataSnapshot.child("image").getValue().toString().equalsIgnoreCase("")){
                            imageUri[0] = dataSnapshot.child("image").getValue().toString();
                            Log.d("rock","Insidde On data View Hldr Image Uri is " + imageUri[0]);

                            Picasso.get().load(imageUri[0]).placeholder(R.drawable.defaultgroupicon).into(holder.circleImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.d("rock","Success in loading the image");
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.d("rock","Failed in loading th eimage " + e.getMessage());
                                }
                            });
                        }else{
                            Picasso.get().load(R.drawable.defaultgroupicon).placeholder(R.drawable.defaultgroupicon).into(holder.circleImageView);
                            Log.d("rock","Else Excecuted");
                        }

                        Log.d("rock","Image Uri In Group Fragent is " + imageUri[0]);
                        final String groupName = dataSnapshot.child("name").getValue().toString();Log.d("rock","Group Name is " + groupName);
                        final String groupMotto = dataSnapshot.child("motto").getValue().toString();Log.d("rock","Group Motto is "+ groupMotto);
                        holder.groupName.setVisibility(View.VISIBLE);holder.groupAbout.setVisibility(View.VISIBLE);holder.circleImageView.setVisibility(View.VISIBLE);
                        holder.groupName.setText(groupName);holder.groupAbout.setText(groupMotto);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                moveToGroupChatActivity(grpIds,groupName,imageUri[0],groupMotto);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,null);
                GroupChatViewHolder chatsViewHolder = new GroupChatViewHolder(view);
                return chatsViewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    private void moveToGroupChatActivity(String grpId,String grpName,String grpImage,String grpMotto) {
        Intent intent = new Intent(getContext(), GroupChatActivity.class);
        intent.putExtra("grpId",grpId);intent.putExtra("grpName",grpName);
        intent.putExtra("grpImage",grpImage);intent.putExtra("grpMotto",grpMotto);
        startActivity(intent);
    }



    public class GroupChatViewHolder extends RecyclerView.ViewHolder
    {
        TextView groupName,groupAbout;
        String uid = "";
        CircleImageView circleImageView;
        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = (TextView)itemView.findViewById(R.id.UserDisplayLayout_userName_textView);              //Using User name for GroupName
            groupAbout = (TextView)itemView.findViewById(R.id.UserDisplayLayout_userAbout_textView);            //Using User About for Group Motto
            circleImageView = (CircleImageView)itemView.findViewById(R.id.UserDisplayLayout_circleImageView);
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
    public void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser() != null  && !userHasMovenToGroupChatActivity)
            updateUserOnlineStatus("offline");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mAuth.getCurrentUser() != null && !userHasMovenToGroupChatActivity)
            updateUserOnlineStatus("offline");
    }
}
