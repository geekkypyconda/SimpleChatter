package com.simplechatter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simplechatter.R;
import com.simplechatter.chatactivity.GroupChatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View view;
    private View groupFragmentView;
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> arrayList,grpIDS;

    private ListView listView;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootRef;
    private boolean userHasMovenToGroupChatActivity = false;
    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        listView = (ListView)groupFragmentView.findViewById(R.id.GroupFragment_listView);
        arrayList = new ArrayList<>();arrayAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,arrayList);arrayAdapter.notifyDataSetChanged();
        grpIDS = new ArrayList<>();
        listView.setAdapter(arrayAdapter);
        arrayList.clear();grpIDS.clear();arrayAdapter.notifyDataSetChanged();

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        displayGroups();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), GroupChatActivity.class);
                intent.putExtra("grp_name",arrayList.get(position));
                userHasMovenToGroupChatActivity = true;
                intent.putExtra("grp_id",grpIDS.get(position));
                startActivity(intent);
            }
        });
        return groupFragmentView;
    }
    public void displayGroups()
    {
        final ArrayList<String> set = new ArrayList<>();

        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayList.clear();grpIDS.clear();arrayAdapter.notifyDataSetChanged();
                set.clear();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext())
                {
                    DataSnapshot p = (DataSnapshot) iterator.next();
                    grpIDS.add(p.getKey());
                    set.add((p).child("Group Name").getValue().toString());
                }

                arrayList.clear();arrayAdapter.notifyDataSetChanged();
                arrayList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
