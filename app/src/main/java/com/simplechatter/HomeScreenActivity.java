package com.simplechatter;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.simplechatter.AboutInfoSection.AboutActivity;
import com.simplechatter.AboutInfoSection.FeedbackActivity;
import com.simplechatter.fragments.ContactsFragment;
import com.simplechatter.fragments.GroupsFragment;
import com.simplechatter.fragments.MessagesFragment;
import com.simplechatter.fragments.RequestFragment;
import com.simplechatter.fragments.ViewPagerAdapter;
import com.simplechatter.settingspane.SettingsActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeScreenActivity extends AppCompatActivity {

    private Toolbar toolbar;
    public static final int IMAGE_REQUEST_CODE = 5;
    private boolean doubleBackToExitPressedOnce = false,userHasMovenToSendChatRequestActivity = false,UserHasMovenToOneToOneChatActivity = false,userHasMoven = false;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private EditText groupName,groupMotto;
    private LayoutInflater dialogInflater;
    private ProgressDialog progressDialog;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private CircleImageView circleImageView;
    private DatabaseReference rootRef;
    private StorageReference storageRootRef;
    private String uniqueGroupId,groupImageUrl = "";
    private String userIdForGroup = "",userImage,userName,userAbout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_home_screen);
        toolbar = (Toolbar)findViewById(R.id.HomeScreen_toolBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Simple Chatter");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = (TabLayout)findViewById(R.id.HomeScreen_tabLayout);
        progressDialog = new ProgressDialog(this);
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
        userIdForGroup =  mAuth.getCurrentUser().getUid();
        storageRootRef = FirebaseStorage.getInstance().getReference();
        rootRef.child("Users").child(userIdForGroup).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image") && !dataSnapshot.child("image").getValue().toString().equalsIgnoreCase(""))
                    userImage = dataSnapshot.child("image").getValue().toString();
                else
                    userImage = "";
                userName = dataSnapshot.child("name").getValue().toString();
                userAbout = dataSnapshot.child("about").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Groups

        uniqueGroupId = "";
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.home_screen_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("rock","Requestcode " + requestCode + ", ResultCode " + resultCode + " CropImage Code " + CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAutoZoomEnabled(true)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1,1)
                    .start(this);

        }if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                disableEveryThing();showProgressDialog("Uploading Image");
                final Uri croppedImage = result.getUri();
                final StorageReference filePath = storageRootRef.child("Groups").child("Profile Images").child(uniqueGroupId + ".jpg");
                filePath.putFile(croppedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Toast.makeText(HomeScreenActivity.this, "Profile Image updated", Toast.LENGTH_SHORT).show();
                        Log.d("rock","Profile Image updated");
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                groupImageUrl = uri.toString();
                            }
                        });
                        rootRef.child("Groups").child(uniqueGroupId).child("image").setValue(groupImageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(HomeScreenActivity.this, "Profile Image updated", Toast.LENGTH_SHORT).show();
                                    Log.d("rock","Profile Image updated");
                                    circleImageView.setImageURI(croppedImage);
                                    enableEveryThing();progressDialog.dismiss();

                                }else{
                                    Toast.makeText(HomeScreenActivity.this, "Some Problem In Importing Image", Toast.LENGTH_SHORT).show();
                                    Log.d("rock","Some Problem In Importing Image");
                                    enableEveryThing();progressDialog.dismiss();
                                }

                            }
                        });
                    }
                });
            }else{
                Toast.makeText(this, "Some Problem In Importing Image", Toast.LENGTH_SHORT).show();
                Log.d("rock","Some Problem In Importing Image");
                enableEveryThing();progressDialog.dismiss();
            }
        }
        else{
            Toast.makeText(this, "Some Problem In Importing Image", Toast.LENGTH_SHORT).show();
            Log.d("rock","Some Problem In Importing Image");
            enableEveryThing();progressDialog.dismiss();
        }
    }


    //Hepler Method for Creating Group In FireBase
    public void makeGroupInFireBase(String groupName,String groupMotto)
    {

        rootRef.child("Group_Contacts").child(userIdForGroup).child(uniqueGroupId).child("Contacts").setValue("saved");
        //String groupUniqueKey;
        rootRef.child("Groups").child(uniqueGroupId).child("name").setValue(groupName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(HomeScreenActivity.this, "Group Created", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(HomeScreenActivity.this, "Group Creating Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rootRef.child("Groups").child(uniqueGroupId).child("motto").setValue(groupMotto).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(HomeScreenActivity.this, "Group Created", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(HomeScreenActivity.this, "Group Creating Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rootRef.child("Groups").child(uniqueGroupId).child("Members").child(userIdForGroup).child("status").setValue("Admin").addOnCompleteListener(new OnCompleteListener<Void>() {
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
    public void checkPermission()
    {


        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else {
            //requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        }

        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }else {
            //requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }



    }
    public void makeNewGroup()
    {

//        LinearLayout ll = new LinearLayout(this);
//        final EditText groupName = new EditText(this),groupMotto = new EditText(this);
//        ll.addView(groupName);ll.addView(groupMotto);
        dialogInflater = LayoutInflater.from(this);
        View groupInfoEntryView = dialogInflater.inflate(R.layout.group_info_entry,null);
        groupName =  (EditText) groupInfoEntryView.findViewById(R.id.GroupInfoEntry_name_editText);

        groupMotto =  (EditText) groupInfoEntryView.findViewById(R.id.GroupInfoEntry_motto_editText);

        uniqueGroupId = rootRef.child("Groups").push().getKey();
        dialogInflater = LayoutInflater.from(this);
        dialogInflater = LayoutInflater.from(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);
        builder.setTitle("Create new group");
        builder.setView(groupInfoEntryView);


        builder.setPositiveButton("Create Group ", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tempName = groupName.getText().toString(),tempMotto = groupMotto.getText().toString();
                String grpName = tempName,grpMotto = tempMotto;

                if(grpName.equalsIgnoreCase("") || grpName.isEmpty())
                    Toast.makeText(HomeScreenActivity.this, "Group Name Cannot Be Empty!", Toast.LENGTH_SHORT).show();
                if(grpMotto.equalsIgnoreCase("") || grpMotto.isEmpty())
                    Toast.makeText(HomeScreenActivity.this, "Group Motto Cannot Be Empty!", Toast.LENGTH_SHORT).show();
                else
                    makeGroupInFireBase(grpName,grpMotto);
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
                userHasMoven = true;
                Intent intent = new Intent(this,SettingsActivity.class);
                intent.putExtra("userId",userIdForGroup);
                intent.putExtra("userImage",userImage);
                intent.putExtra("userAbout",userAbout);
                intent.putExtra("userName",userName);
                startActivity(intent);
                break;
            case R.id.home_screen_menu_feedback:
                userHasMoven = true;
                Intent intent2 = new Intent(this,FeedbackActivity.class);
                intent2.putExtra("userId",userIdForGroup);
                intent2.putExtra("userImage",userImage);
                intent2.putExtra("userAbout",userAbout);
                intent2.putExtra("userName",userName);
                startActivity(intent2);
                break;
            case R.id.home_screen_menu_about:
                userHasMoven = true;
                Intent intent3 = new Intent(this,AboutActivity.class);
                intent3.putExtra("userId",userIdForGroup);
                intent3.putExtra("userImage",userImage);
                intent3.putExtra("userAbout",userAbout);
                intent3.putExtra("userName",userName);
                startActivity(intent3);
                break;
            case R.id.home_screen_menu_findChatters:
                userHasMoven = true;
                startActivity(new Intent(this, FindFriendsActivity.class));
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
        }
        else if(requestCode == 2){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            userHasMoven = false;
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


    public void showProgressDialog(String s)
    {
        progressDialog.setTitle(s);
        progressDialog.setMessage("Please Wait.....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        Log.d("rock","inside progeess Dialog");

    }
    public void disableEveryThing()
    {
        groupName.setEnabled(false);
        groupMotto.setEnabled(false);

        Log.d("rock","disabe All");
    }
    public void enableEveryThing()
    {
        groupName.setEnabled(true);
        groupMotto.setEnabled(true);

        Log.d("rock","Enalble all");
    }


    @Override
    protected void onStop() {
        if(!userHasMoven){
            updateUserOnlineStatus("offline");
            Log.d("rock","making User offline from HomeScreenActivity in onStop()." + "\n" + " Current Status of back Pressed is: " + userHasMoven);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(!userHasMoven){
            updateUserOnlineStatus("offline");
            Log.d("rock","making User offline from HomeScreenActivity in onDestroy()." + "\n" + " Current Status of back Pressed is: " + userHasMoven);
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            updateUserOnlineStatus("online");
            Log.d("rock","making User online from HomeScreenActivity in onStart()." + "\n" + " Current Status of back Pressed is: " + userHasMoven);
        }

    }
}
