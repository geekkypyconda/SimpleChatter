package com.simplechatter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.simplechatter.chatactivity.OneToOneChatActivity;
import com.simplechatter.classes.Contact;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {


    //Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseRootRef,userContactsRef,UsersRef,ContactRef,ChatRef;        //here userRef is the refrence to All the USers not the /*Not the Actual user Logged in Ref*/
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mAuth;
    private StorageReference storageRootref;
    private String currentUserId = "";
    private String userSendAbout = "offline";
    boolean isOnline = false;

    //Activity Components
    private RecyclerView recyclerView;
    View chatFragmentView;
    private boolean UserHasMovenToOneToOneChatActivity = false;


    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chatFragmentView = inflater.inflate(R.layout.fragment_messages, container, false);

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
        //Initializing Activity Components
        recyclerView = (RecyclerView)chatFragmentView.findViewById(R.id.MessagesFragment_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return chatFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            updateUserOnlineStatus("online");
            Log.d("rock","Making User online from Messages Fragment  in onStart() " + "\n" +
                    "Curent Moving  state is " + UserHasMovenToOneToOneChatActivity);
        }

        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(ChatRef,Contact.class).build();

        FirebaseRecyclerAdapter<Contact, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contact model) {
                final String userIds = getRef(position).getKey();
                final String[] imageUrl = {""};
                UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild("uid") && !dataSnapshot.child("uid").getValue().toString().equalsIgnoreCase(currentUserId)){
                            if(dataSnapshot.hasChild("image") && !dataSnapshot.child("image").getValue().toString().equalsIgnoreCase("")){
                                imageUrl[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(imageUrl[0]).placeholder(R.drawable.userdefaultprofile).into(holder.circleImageView);
                            }else
                                Picasso.get().load(R.drawable.userdefaultprofile).placeholder(R.drawable.userdefaultprofile).into(holder.circleImageView);

                            final String profileName = dataSnapshot.child("name").getValue().toString();
                            String profileAbout = dataSnapshot.child("about").getValue().toString();
                            holder.userName.setText(profileName);
                            if(dataSnapshot.hasChild("online_status") && dataSnapshot.child("online_status").hasChild("current_status")){
                                String state = dataSnapshot.child("online_status").child("current_status").getValue().toString();
                                String date = dataSnapshot.child("online_status").child("current_date").getValue().toString();
                                String time = dataSnapshot.child("online_status").child("current_time").getValue().toString();

                                userSendAbout = "Last Seen: " + date + "\n" + time;
                                if(state.equalsIgnoreCase("online")){
                                    holder.isUserOnlineImageView.setVisibility(View.VISIBLE);
                                    holder.userAbout.setText("online");
                                    isOnline = true;
                                }else
                                    holder.userAbout.setText(userSendAbout);
                            }else
                                holder.userAbout.setText("offline");

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    moveToOneToOneChatActivity(userIds,profileName,imageUrl[0],userSendAbout,isOnline);
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                ChatsViewHolder chatsViewHolder = new ChatsViewHolder(view);
                return chatsViewHolder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public void moveToOneToOneChatActivity(String visitUserId,String visitUserName,String visitUserImageUrl,String userSendAbout,boolean isOnline)
    {
        Intent intent = new Intent(getContext(), OneToOneChatActivity.class);
        intent.putExtra("visit_user_id",visitUserId);
        intent.putExtra("visit_user_name",visitUserName);
        intent.putExtra("image_url",visitUserImageUrl);
        intent.putExtra("visit_user_about",userSendAbout);
        intent.putExtra("visit_is_online",isOnline);
        UserHasMovenToOneToOneChatActivity = true;
        startActivity(intent);

    }


    public class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userAbout;
        String uid = "";
        ImageView isUserOnlineImageView;
        CircleImageView circleImageView;
        public ChatsViewHolder(@NonNull View view) {
            super(view);
            userName = (TextView) view.findViewById(R.id.UserDisplayLayout_userName_textView);
            userAbout = (TextView)view.findViewById(R.id.UserDisplayLayout_userAbout_textView);
            circleImageView = (CircleImageView)view.findViewById(R.id.UserDisplayLayout_circleImageView);
            isUserOnlineImageView = (ImageView)view.findViewById(R.id.UserDisplayLayout_userOnlineGreenCircle_imageVew);
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
        String disp = "[" + saveCurrentDate + ", " + saveCurrentTime + ", " + state + ", " + currentUserId + "]" + " From MessageFragment" ;
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
    public void onStop() {
        if(mAuth.getCurrentUser() != null  && !UserHasMovenToOneToOneChatActivity){
            updateUserOnlineStatus("offline");
            Log.d("rock","Making User Offline from Messages Fragment  in onStop() " + "\n" +
                    "Curent Moving  state is " + UserHasMovenToOneToOneChatActivity);
        }
        super.onStop();


    }

    @Override
    public void onDestroy() {

        if(mAuth.getCurrentUser() != null && !UserHasMovenToOneToOneChatActivity){
            updateUserOnlineStatus("offline");
            Log.d("rock","Making User Offline from Messages Fragment  in onDestroy() " + "\n" +
                    "Curent Moving  state is " + UserHasMovenToOneToOneChatActivity);
        }
        super.onDestroy();
    }



}
