package com.simplechatter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    //Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseRootRef,userContactsRef,UsersRef;        //here userRef is the refrence to All the USers not the /*Not the Actual user Logged in Ref*/
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mAuth;
    private StorageReference storageRootref;
    private String currentUserId = "";

    //Activity Components
    private RecyclerView recyclerView;


    View contactsView;
    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        //Initializing FireBase Components
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseRootRef = firebaseDatabase.getReference();
        storageRootref = firebaseStorage.getReference();
        currentUserId = mAuth.getCurrentUser().getUid();
        userContactsRef = databaseRootRef.child("Contacts").child(currentUserId);
        UsersRef = databaseRootRef.child("Users");

        //Initializing Activity Components
        recyclerView = (RecyclerView)contactsView.findViewById(R.id.ContactsFragment_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(userContactsRef,Contact.class).build();

        FirebaseRecyclerAdapter<Contact,ContactsViewHolder>  adapter = new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contact model) {
                String usersIds = getRef(position).getKey();
                UsersRef.child(usersIds).addValueEventListener(new ValueEventListener() {
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userAbout;
        String uid = "";
        CircleImageView circleImageView;
        public ContactsViewHolder(@NonNull View view) {
            super(view);
            userName = (TextView) view.findViewById(R.id.UserDisplayLayout_userName_textView);
            userAbout = (TextView)view.findViewById(R.id.UserDisplayLayout_userAbout_textView);
            circleImageView = (CircleImageView)view.findViewById(R.id.UserDisplayLayout_circleImageView);
        }
    }
}
