package com.simplechatter.chatactivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import com.simplechatter.HomeScreenActivity;
import com.simplechatter.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {


    //Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseRootRef,userContactsRef,UsersRef,ContactRef,ChatRef;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mAuth;
    private StorageReference storageRootref;
    private String senderId = "";

    private List<Message> messageList;

    public MessagesAdapter(List<Message> messageList)
    {
        this.messageList = messageList;

        //Initializing FireBase Components
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseRootRef = firebaseDatabase.getReference();
        storageRootref = firebaseStorage.getReference();
        senderId = mAuth.getCurrentUser().getUid();
        userContactsRef = databaseRootRef.child("Contacts").child(senderId);
        ContactRef = databaseRootRef.child("Contacts");
        UsersRef = databaseRootRef.child("Users");
        ChatRef = ContactRef.child(senderId);



    }


    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderTextView,recieverTextView;
        public CircleImageView recieverProfileImageView;
        public ImageView senderImageView,recieverImageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = (TextView)itemView.findViewById(R.id.custom_message_layout_senderMessage_TextView);
            recieverTextView = (TextView)itemView.findViewById(R.id.custom_message_layout_receiverMessage_TextView);
            recieverProfileImageView = (CircleImageView)itemView.findViewById(R.id.custom_message_layout_imageView);
            senderImageView = (ImageView)itemView.findViewById(R.id.custom_message_layout_sender_imageView);
            recieverImageView = (ImageView)itemView.findViewById(R.id.custom_message_layout_reciever_imageView);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout,parent,false);
        return new MessageViewHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        Message message = messageList.get(position);
        String fromUserId = message.getFrom();
        String fromMessageType = message.getType();
        final String[] recieverUserAbout = new String[1];
        final String[] recieverUserId = new String[1];
        DatabaseReference fromUserRef = UsersRef.child(fromUserId);
        Log.d("rock","from User id is " + fromUserId);
        fromUserRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("rock","Going into AsyncTask");
                if(dataSnapshot.hasChild("image")){
                    String imageUrl = dataSnapshot.child("image").getValue().toString();
                    Log.d("rock","from User Imgae Url is " + imageUrl);
                    Picasso.get().load(imageUrl).placeholder(R.drawable.userdefaultprofile).into(holder.recieverProfileImageView);
                    Log.d("rock","Image Loading Successfull!");
                }else{
                    Picasso.get().load(R.drawable.userdefaultprofile).placeholder(R.drawable.userdefaultprofile).into(holder.recieverProfileImageView);
                    Log.d("rock","User Profile Not Set using default profile");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.recieverTextView.setVisibility(View.GONE);
        holder.recieverProfileImageView.setVisibility(View.GONE);
        holder.senderTextView.setVisibility(View.GONE);
        holder.senderImageView.setVisibility(View.GONE);
        holder.recieverImageView.setVisibility(View.GONE);

        if(fromMessageType.equals("text")){

            if(fromUserId.equals(senderId)){
                holder.senderTextView.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderTextView.setText(message.getMessage() + "\n\n" + message.getDate() + ": " + message.getTime());
                holder.senderTextView.setVisibility(View.VISIBLE);
                holder.senderTextView.setTextColor(Color.WHITE);
            }else{
                holder.recieverTextView.setVisibility(View.VISIBLE);
                holder.recieverProfileImageView.setVisibility(View.VISIBLE);
                holder.recieverTextView.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.recieverTextView.setText(message.getMessage() + "\n\n" + message.getDate() + ": " + message.getTime());
            }
        }else if(fromMessageType.equalsIgnoreCase("image")){
            if(fromUserId.equalsIgnoreCase(senderId)){
                holder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).placeholder(R.drawable.defaultimagesendimage).into(holder.senderImageView);
            }else {
                holder.recieverImageView.setVisibility(View.VISIBLE);
                holder.recieverProfileImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).placeholder(R.drawable.defaultimagesendimage).into(holder.recieverImageView);
            }

        }else if(fromMessageType.equalsIgnoreCase("pdf")){
            if(fromUserId.equalsIgnoreCase(senderId)){
                holder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.pdffiledefaulticon).into(holder.senderImageView);

            }else {
                holder.recieverImageView.setVisibility(View.VISIBLE);
                holder.recieverProfileImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.pdffiledefaulticon).into(holder.recieverImageView);

            }




        }else if(fromMessageType.equalsIgnoreCase("document")){
            if(fromUserId.equalsIgnoreCase(senderId)){
                holder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.filesdefaulticon).into(holder.senderImageView);


            }else {
                holder.recieverImageView.setVisibility(View.VISIBLE);
                holder.recieverProfileImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.filesdefaulticon).into(holder.recieverImageView);



            }


        }
        if(fromUserId.equalsIgnoreCase(senderId)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(messageList.get(position).getType().equalsIgnoreCase("pdf") || messageList.get(position).getType().equalsIgnoreCase("document")){
                        CharSequence options[] =
                        {
                            "Delete For Me",
                            "Download and View This Document",
                            "Cancel",
                            "Delete For Every One"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    deleteSentMessage(position,holder,messageList.get(position).getMessageId());
                                }else if(which == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if(which == 2){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }else if(which == 3){
                                    deleteMessageForEveryOne(position,holder);
                                }
                            }
                        });
                        builder.show();
                    }else if(messageList.get(position).getType().equalsIgnoreCase("text")){
                        CharSequence options[] =
                                {
                                        "Delete For Me",
                                        "Cancel",
                                        "Delete For Every One"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    deleteSentMessage(position,holder,messageList.get(position).getMessageId());
                                }else if(which == 1){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }else if(which == 2){
                                    deleteMessageForEveryOne(position,holder);
                                }
                            }
                        });
                        builder.show();
                    }else if(messageList.get(position).getType().equalsIgnoreCase("image")){
                        CharSequence options[] =
                                {
                                        "Delete For Me",
                                        "View This Image",
                                        "Cancel",
                                        "Delete For Every One"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    Log.d("rock",messageList.get(position).toString());
                                    Log.d("rock","Message id is " + messageList.get(position).getMessageId());
                                    deleteSentMessage(position,holder,messageList.get(position).getMessageId());
                                }else if(which == 1){
                                    moveToImageViewerActivity(position,holder);
                                }else if(which == 2){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }else if(which == 3){
                                    deleteMessageForEveryOne(position,holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }else{
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(messageList.get(position).getType().equalsIgnoreCase("pdf") || messageList.get(position).getType().equalsIgnoreCase("document")){
                        CharSequence options[] =
                                {
                                        "Delete For Me",
                                        "Download and View This Document",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    deleteRecieveMessage(position,holder,messageList.get(position).getMessageId());
                                }else if(which == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if(which == 2){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        builder.show();
                    }else if(messageList.get(position).getType().equalsIgnoreCase("text")){
                        CharSequence options[] =
                                {
                                        "Delete For Me",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    deleteRecieveMessage(position,holder,messageList.get(position).getMessageId());
                                }else if(which == 1){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        builder.show();
                    }else if(messageList.get(position).getType().equalsIgnoreCase("image")){
                        CharSequence options[] =
                                {
                                        "Delete For Me",
                                        "Download and View This Image",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    deleteRecieveMessage(position,holder,messageList.get(position).getMessageId());

                                }else if(which == 1){
                                    moveToImageViewerActivity(position,holder);
                                }else if(which == 2){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private void deleteSentMessage(final int pos, final MessageViewHolder holder,String messageKeyId)
    {
        databaseRootRef.child("Messages").child(messageList.get(pos).getFrom())
                .child(messageList.get(pos).getTo())
                .child(messageKeyId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("rock","Message Deleted Successfully!");
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully!", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d("rock","Error in Deleting Messages!");
                    Toast.makeText(holder.itemView.getContext(), "Failed to delete message :: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void deleteRecieveMessage(final int pos, final MessageViewHolder holder,String messageKeyId)
    {
        databaseRootRef.child("Messages").child(messageList.get(pos).getTo())
                .child(messageList.get(pos).getFrom())
                .child(messageKeyId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("rock","Message Deleted Successfully!");
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully!", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d("rock","Error in Deleting Messages!");
                    Toast.makeText(holder.itemView.getContext(), "Failed to delete message :: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void deleteMessageForEveryOne(final int pos, final MessageViewHolder holder)
    {
        databaseRootRef.child("Messages").child(messageList.get(pos).getFrom())
                .child(messageList.get(pos).getTo())
                .child(messageList.get(pos).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    databaseRootRef.child("Messages").child(messageList.get(pos).getTo())
                            .child(messageList.get(pos).getFrom())
                            .child(messageList.get(pos).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d("rock","Message Deleted Successfully!");
                                Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully!", Toast.LENGTH_SHORT).show();
                            }else{
                                Log.d("rock","Error in Deleting Messages!");
                                Toast.makeText(holder.itemView.getContext(), "Failed to delete message :: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    Log.d("rock","Error in Deleting Messages!");
                    Toast.makeText(holder.itemView.getContext(), "Failed to delete message :: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void moveToImageViewerActivity(int position,MessageViewHolder holder)
    {
        Intent intent = new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
        intent.putExtra("image_url",messageList.get(position).getMessage());
        holder.itemView.getContext().startActivity(intent);
    }
    private void moveToHomeSreenActivity(MessageViewHolder holder)
    {
        Intent intent  = new Intent(holder.itemView.getContext(), HomeScreenActivity.class);
        holder.itemView.getContext().startActivity(intent);
    }


}
