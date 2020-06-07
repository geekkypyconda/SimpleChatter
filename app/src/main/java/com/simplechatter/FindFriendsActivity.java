package com.simplechatter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.simplechatter.AboutInfoSection.SendChatRequestToUserActivity;
import com.simplechatter.classes.Contact;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView recyclerView;
    private DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        mToolbar = (Toolbar)findViewById(R.id.FindFriendsActivity_appBarLayout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Chatters");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = (RecyclerView)findViewById(R.id.FindFriendsActivity_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(userRef,Contact.class).build();

        FirebaseRecyclerAdapter<Contact,FindFriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, FindFriendsViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull Contact model) {


                    holder.userName.setText(model.getName());
                    holder.userAbout.setText(model.getAbout());
                    if(model.getImage() != null && !model.getImage().equalsIgnoreCase(""))
                    Picasso.get().load(model.getImage()).into(holder.circleImageView);

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String visit_user_id = getRef(position).getKey();
                            moveToSendChatRequestToUserActivity(visit_user_id);
                        }
                    });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout,viewGroup,false);
                FindFriendsViewHolder findFriendsViewHolder = new FindFriendsViewHolder(view);
                return findFriendsViewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void moveToSendChatRequestToUserActivity(String visit_user_id) {
        Intent intent = new Intent(FindFriendsActivity.this, SendChatRequestToUserActivity.class);
        intent.putExtra("visit_user_id",visit_user_id);
        startActivity(intent);
    }



    /*Static Class****************************************************************/
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userAbout;
        String uid = "";
        CircleImageView circleImageView;
        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.UserDisplayLayout_userName_textView);
            userAbout = itemView.findViewById(R.id.UserDisplayLayout_userAbout_textView);
            circleImageView = itemView.findViewById(R.id.UserDisplayLayout_circleImageView);

        }
    }
    /****************************************************************Static Class*/

}

