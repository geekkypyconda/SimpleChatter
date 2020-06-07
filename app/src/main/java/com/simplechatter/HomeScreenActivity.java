package com.simplechatter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.simplechatter.AboutInfoSection.AboutActivity;
import com.simplechatter.AboutInfoSection.FeedbackActivity;
import com.simplechatter.fragments.ContactsFragment;
import com.simplechatter.fragments.GroupsFragment;
import com.simplechatter.fragments.MessagesFragment;
import com.simplechatter.fragments.RequestFragment;
import com.simplechatter.fragments.ViewPagerAdapter;
import com.simplechatter.settingspane.SettingsActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class HomeScreenActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private boolean doubleBackToExitPressedOnce = false,userHasMovenToSendChatRequestActivity = false,UserHasMovenToOneToOneChatActivity = false;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String uniqueGroupId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_home_screen);
        toolbar = (Toolbar)findViewById(R.id.HomeScreen_toolBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("     Simple Chatter");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = (TabLayout)findViewById(R.id.HomeScreen_tabLayout);
        viewPager = (ViewPager)findViewById(R.id.HomeScreen_viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.AddFragment(new MessagesFragment(),"Messages");
        viewPagerAdapter.AddFragment(new GroupsFragment(),"Groups");
        viewPagerAdapter.AddFragment(new ContactsFragment(),"Contacts");
        viewPagerAdapter.AddFragment(new RequestFragment(),"Requests");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("Messages");tabLayout.getTabAt(0).setIcon(R.drawable.msg_vector);
        tabLayout.getTabAt(1).setText("Groups");tabLayout.getTabAt(1).setIcon(R.drawable.grp_vector);
        tabLayout.getTabAt(2).setText("Contacts");tabLayout.getTabAt(2).setIcon(R.drawable.cnts_vector);
        tabLayout.getTabAt(3).setText("Requests");tabLayout.getTabAt(3).setIcon(R.drawable.pen_req_vector);

        //INITIALIZING fIREBASE
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        rootRef = firebaseDatabase.getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.home_screen_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Hepler Method for Creating Group In FireBase
    public void makeGroupInFireBase(String groupName)
    {
        uniqueGroupId = rootRef.child("Groups").push().getKey();
        //String groupUniqueKey;
        rootRef.child("Groups").child(uniqueGroupId).child("Group Name").setValue(groupName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(HomeScreenActivity.this, "Group Created", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(HomeScreenActivity.this, "Group Creating Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
    }
    public void makeNewGroup()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);
        builder.setTitle("Enter Group Name");
        final EditText builderEditText = new EditText(this);
        builderEditText.setHint("Simple Group");
        builder.setView(builderEditText);

        builder.setPositiveButton("Create Group ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = builderEditText.getText().toString();
                if(text.equalsIgnoreCase("") || text.isEmpty())
                    Toast.makeText(HomeScreenActivity.this, "Group Name Cannot Be Empty!", Toast.LENGTH_SHORT).show();
                else
                    makeGroupInFireBase(text);
            }
        });
        builder.setNegativeButton("Cancel ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(HomeScreenActivity.this, "We Never Mind!", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
        builder.show();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.home_screen_menu_new_group:
                makeNewGroup();
                break;
            case R.id.home_screen_menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.home_screen_menu_feedback:
                startActivity(new Intent(this, FeedbackActivity.class));
                break;
            case R.id.home_screen_menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.home_screen_menu_findChatters:
                startActivity(new Intent(this, FindFriendsActivity.class));
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            updateUserOnlineStatus("offline");
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
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
        if(doubleBackToExitPressedOnce){
            rootRef.child("Users").child(currentUserId).child("online_status").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        super.onStop();
        if(mAuth.getCurrentUser() != null && doubleBackToExitPressedOnce){
            updateUserOnlineStatus("offline");
            Log.d("rock","making User offline from HomeScreenActivity in onStop()." + "\n" + " Current Status of back Pressed is: " + doubleBackToExitPressedOnce);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAuth.getCurrentUser() != null && doubleBackToExitPressedOnce){
            updateUserOnlineStatus("offline");
            Log.d("rock","making User offline from HomeScreenActivity in onDestroy()." + "\n" + " Current Status of back Pressed is: " + doubleBackToExitPressedOnce);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            updateUserOnlineStatus("online");
            Log.d("rock","making User online from HomeScreenActivity in onStart()." + "\n" + " Current Status of back Pressed is: " + doubleBackToExitPressedOnce);
        }

    }
}
