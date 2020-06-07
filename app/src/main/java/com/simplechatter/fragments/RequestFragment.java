package com.simplechatter.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.simplechatter.classes.Contact;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestFragmentView;


    //Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseRootRef,userContactsRef,UsersRef,chatRequestRef;        //here userRef is the refrence to All the USers not the /*Not the Actual user Logged in Ref*/
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mAuth;
    private StorageReference storageRootref;
    private DatabaseReference ContactsRef;
    private String currentUserId = "";

    //Activity Components
    private RecyclerView recyclerView;
    ;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        //Initializing FireBase Components
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseRootRef = firebaseDatabase.getReference();
        storageRootref = firebaseStorage.getReference();
        currentUserId = mAuth.getCurrentUser().getUid();
        ContactsRef = databaseRootRef.child("Contacts");
        userContactsRef = databaseRootRef.child("Contacts").child(currentUserId);
        UsersRef = databaseRootRef.child("Users");
        chatRequestRef = databaseRootRef.child("ChatRequest");

        //Initializing Activity Components
        recyclerView = (RecyclerView)requestFragmentView.findViewById(R.id.RequestFragment_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(chatRequestRef.child(currentUserId),Contact.class).build();

        FirebaseRecyclerAdapter<Contact,RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contact model) {
                holder.itemView.findViewById(R.id.UserDisplayLayout_accept_button).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.UserDisplayLayout_decline_button).setVisibility(View.VISIBLE);

                final String list = getRef(position).getKey();
                DatabaseReference typeRef = getRef(position).child("request_type");
                typeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String request_type = dataSnapshot.getValue().toString();Log.d("rock","Request type is " + request_type);
                            if(request_type.equalsIgnoreCase("received")){
                               UsersRef.child(list).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       if(dataSnapshot.hasChild("image") && !dataSnapshot.child("image").getValue().toString().equalsIgnoreCase("")){
                                           String imageUrl = dataSnapshot.child("image").getValue().toString();
                                           Picasso.get().load(imageUrl).placeholder(R.drawable.userdefaultprofile).into(holder.circleImageView);
                                       }else
                                           Picasso.get().load(R.drawable.userdefaultprofile).placeholder(R.drawable.userdefaultprofile).into(holder.circleImageView);
                                       final String profileName = dataSnapshot.child("name").getValue().toString();
                                       String profileAbout = dataSnapshot.child("about").getValue().toString();
                                       holder.userName.setText(profileName);holder.userAbout.setText(profileAbout);

                                       holder.itemView.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               CharSequence charSequence[] = new CharSequence[]{"Accept","decline"};
                                               AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                                       .setTitle("Accept " + profileName + " Chat Request");
                                               builder.setItems(charSequence, new DialogInterface.OnClickListener() {
                                                   @Override
                                                   public void onClick(DialogInterface dialog, int i) {
                                                       if(i == 0){
                                                            ContactsRef.child(currentUserId).child(list).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        ContactsRef.child(list).child(currentUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    chatRequestRef.child(currentUserId).child(list).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                chatRequestRef.child(list).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task){
                                                                                                        if(task.isSuccessful()){
                                                                                                            Toast.makeText(getContext(), "Now You and " + profileName + " are friends", Toast.LENGTH_SHORT).show();
                                                                                                            Log.d("rock","Now You and " + profileName + " are friends");
                                                                                                        }else{
                                                                                                            Toast.makeText(getContext(), "Something Went Wrong " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                            Log.d("rock","Something Went Wrong" + task.getException().getMessage());
                                                                                                        }

                                                                                                    }
                                                                                                });
                                                                                            }else{
                                                                                                Toast.makeText(getContext(), "Something Went Wrong " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                Log.d("rock","Something Went Wrong " + task.getException().getMessage());
                                                                                            }

                                                                                        }
                                                                                    });

                                                                                }else{

                                                                                }
                                                                            }
                                                                        });
                                                                    }else{
                                                                        Toast.makeText(getContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                                                                        Log.d("rock","Something Went Wrong");
                                                                    }
                                                                }
                                                            });
                                                       }else if(i == 1){
                                                           chatRequestRef.child(currentUserId).child(list).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                   if(task.isSuccessful()){
                                                                       chatRequestRef.child(list).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<Void> task){
                                                                               if(task.isSuccessful()){
                                                                                   Toast.makeText(getContext(), "Request Declined ", Toast.LENGTH_SHORT).show();
                                                                                   Log.d("rock","Request Declined ");
                                                                               }else{
                                                                                   Toast.makeText(getContext(), "Something Went Wrong " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                   Log.d("rock","Something Went Wrong " + task.getException().getMessage());
                                                                               }

                                                                           }
                                                                       });
                                                                   }else{
                                                                       Toast.makeText(getContext(), "Something Went Wrong " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                       Log.d("rock","Something Went Wrong " + task.getException().getMessage());
                                                                   }

                                                               }
                                                           });
                                                       }
                                                   }
                                               }).show();


                                           }
                                       });
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });
                            }else if(request_type.equalsIgnoreCase("send")){
                                Button request_send_button = (Button)holder.itemView.findViewById(R.id.UserDisplayLayout_accept_button);
                                request_send_button.setText("Request Send");
                                Button request_cancel_button = (Button)holder.itemView.findViewById(R.id.UserDisplayLayout_decline_button);
                                request_cancel_button.setText("Cancel Request");
                                UsersRef.child(list).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image") && !dataSnapshot.child("image").getValue().toString().equalsIgnoreCase("")){
                                            String imageUrl = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(imageUrl).placeholder(R.drawable.userdefaultprofile).into(holder.circleImageView);
                                        }else
                                            Picasso.get().load(R.drawable.userdefaultprofile).placeholder(R.drawable.userdefaultprofile).into(holder.circleImageView);
                                        final String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileAbout = dataSnapshot.child("about").getValue().toString();
                                        holder.userName.setText(profileName);holder.userAbout.setText(profileAbout);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence charSequence[] = new CharSequence[]{"Cancel Request"};
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                                        .setTitle("Already Sent Chat Request to " + profileName);
                                                builder.setItems(charSequence, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {
                                                        if(i == 0){
                                                            chatRequestRef.child(currentUserId).child(list).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        chatRequestRef.child(list).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task){
                                                                                if(task.isSuccessful()){
                                                                                    Toast.makeText(getContext(), "You Have Cancelled The Vhat Request for  " + profileName, Toast.LENGTH_SHORT).show();
                                                                                    Log.d("rock","You Have Cancelled The Vhat Request for  " + profileName);
                                                                                }else{
                                                                                    Toast.makeText(getContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                                                                                    Log.d("rock","Something Went Wrong");
                                                                                }

                                                                            }
                                                                        });
                                                                    }else{
                                                                        Toast.makeText(getContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                                                                        Log.d("rock","Something Went Wrong");
                                                                    }

                                                                }
                                                            });
                                                        }
                                                    }
                                                }).show();


                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                            else{

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                RequestViewHolder requestViewHolder = new RequestViewHolder(view);
                return requestViewHolder;
            };
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }



    public class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userAbout;
        Button acceptButton,declineButton;
        String uid = "";
        CircleImageView circleImageView;
        public RequestViewHolder(@NonNull View view) {
            super(view);
            userName = (TextView) view.findViewById(R.id.UserDisplayLayout_userName_textView);
            userAbout = (TextView)view.findViewById(R.id.UserDisplayLayout_userAbout_textView);
            circleImageView = (CircleImageView)view.findViewById(R.id.UserDisplayLayout_circleImageView);
            acceptButton = (Button)view.findViewById(R.id.UserDisplayLayout_accept_button);
            declineButton = (Button)view.findViewById(R.id.UserDisplayLayout_decline_button);
        }
    }
}
