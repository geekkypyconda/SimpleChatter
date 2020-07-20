package com.simplechatter.groupchatactivties;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.simplechatter.R;
import com.simplechatter.chatactivity.ImageViewerActivity;
import com.simplechatter.chatactivity.Message;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder> {

    //Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseRootRef,groupContactsRef,UsersRef,groupRef;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mAuth;
    private StorageReference storageRootref;
    private String senderId = "",TAG = "rock",tempRecieverUserName = "";


    private List<Message> messageList;

    public GroupMessageAdapter(List<Message> messageList)
    {
        this.messageList = messageList;

        //Initializing FireBase Components
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseRootRef = firebaseDatabase.getReference();
        storageRootref = firebaseStorage.getReference();
        senderId = mAuth.getCurrentUser().getUid();
        groupContactsRef = databaseRootRef.child("Group_Contacts").child(senderId);
        UsersRef = databaseRootRef.child("Users");
        groupRef = databaseRootRef.child("Groups");

    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_custom_message_layout,parent,false);
        return new GroupMessageViewHolder(view);
    }


    public static class FireProcessor extends AsyncTask<Void,Void,Void>{

        private String fuid,resName = "";
        private WeakReference<GroupMessageAdapter> weakReference;
        private GroupMessageAdapter groupMessageAdapter;
        public FireProcessor(String fuid,GroupMessageAdapter groupMessageAdapter) {
            this.fuid = fuid;
            weakReference = new WeakReference<GroupMessageAdapter>(groupMessageAdapter);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            groupMessageAdapter = weakReference.get();
            if(groupMessageAdapter == null)
                return null;

            if(!fuid.equalsIgnoreCase(groupMessageAdapter.senderId)){
                groupMessageAdapter.databaseRootRef.child("Users").child(fuid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        groupMessageAdapter.tempRecieverUserName = dataSnapshot.child("name").getValue().toString();
                        resName = groupMessageAdapter.tempRecieverUserName + "";
                        Log.d(groupMessageAdapter.TAG,"User Name from Indside " + groupMessageAdapter.tempRecieverUserName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            groupMessageAdapter = weakReference.get();
            if(groupMessageAdapter == null)
                return;

            Log.d(groupMessageAdapter.TAG, "onPostExecute: Name is" + groupMessageAdapter.tempRecieverUserName);
        }
    }





    @Override
    public void onBindViewHolder(@NonNull final GroupMessageViewHolder holder, final int position) {
        final Message message = messageList.get(position);
        final String fromUserId = message.getFrom();Log.d("rock","From user id is " + fromUserId);
        FireProcessor FP =  new FireProcessor(fromUserId,this);FP.execute();
        Log.d(TAG, "onBindViewHolder: Name From async Tassk " + tempRecieverUserName);

        final String fromMessageType = message.getType();Log.d("rock","From Message type is " + fromMessageType);
        final String fromMessageId = message.getMessageId();Log.d("rock","From Message ID is " + fromMessageId);
        final String fromGroupId = message.getGroupId();Log.d("rock","From gr id is " + fromGroupId);

        final String[] groupMotto = new String[1],groupId = new String[1];
        DatabaseReference fromUserRef = UsersRef.child(fromUserId);
        fromUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("rock","Inside on Ddtata Cgange");
                if(dataSnapshot.hasChild("image") && !dataSnapshot.child("image").getValue().toString().equalsIgnoreCase("")){
                    String imageUrl = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(imageUrl).placeholder(R.drawable.userdefaultprofile).into(holder.recieverProfileImageView);
                }else
                    Picasso.get().load(R.drawable.userdefaultprofile).placeholder(R.drawable.userdefaultprofile).into(holder.recieverProfileImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!fromUserId.equalsIgnoreCase(senderId)){
            databaseRootRef.child("Users").child(fromUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    tempRecieverUserName = dataSnapshot.child("name").getValue().toString();
                    Log.d(TAG,"User Name from Indside " + dataSnapshot.child("name").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        Log.d(TAG,"From userId is " + fromUserId + "Snder Id is " + senderId + " Receiver name is " + tempRecieverUserName);

        holder.recieverTextView.setVisibility(GONE);
        holder.recieverImageView.setVisibility(GONE);holder.recieverFilesDateAndTimeTextView.setVisibility(GONE);
        holder.senderNameTextView.setVisibility(GONE);
        holder.senderImageView.setVisibility(GONE);holder.senderFilesDateAndTimeTextView.setVisibility(GONE);
        holder.receiverNameTextView.setVisibility(GONE);
        holder.senderTextView.setVisibility(GONE);
        holder.recieverProfileImageView.setVisibility(GONE);


        if(fromMessageType.equalsIgnoreCase("text")){
            if(fromUserId.equalsIgnoreCase(senderId)){
                holder.senderTextView.setVisibility(View.VISIBLE);
                holder.senderTextView.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderTextView.setTextColor(Color.WHITE);
                holder.senderTextView.setText(message.getMessage() + "\n\n" + message.getDate() + "\n" + message.getTime());
            }else{
                holder.recieverTextView.setVisibility(View.VISIBLE);
                holder.receiverNameTextView.setVisibility(View.VISIBLE);holder.receiverNameTextView.setText(message.getName());

                holder.recieverTextView.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.recieverProfileImageView.setVisibility(View.VISIBLE);
                holder.recieverTextView.setText(message.getMessage() + "\n\n" + message.getDate() + "\n" + message.getTime());
            }
        }else if(fromMessageType.equalsIgnoreCase("image")){
            if(fromUserId.equalsIgnoreCase(senderId)){
                holder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).placeholder(R.drawable.defaultimagesendimage).into(holder.senderImageView);
                holder.senderFilesDateAndTimeTextView.setText(message.getDate() + "\n" + message.getTime());
                holder.senderFilesDateAndTimeTextView.setVisibility(View.VISIBLE);
            }else{
                holder.recieverImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).placeholder(R.drawable.defaultimagesendimage).into(holder.recieverImageView);
                holder.recieverProfileImageView.setVisibility(View.VISIBLE);
                holder.receiverNameTextView.setVisibility(View.VISIBLE);holder.receiverNameTextView.setText(message.getName() + "\n" + message.getDate() + "\n" + message.getTime());
                holder.recieverFilesDateAndTimeTextView.setText(message.getDate() + "\n" + message.getTime());
                holder.recieverFilesDateAndTimeTextView.setVisibility(View.VISIBLE);
            }
        }else if(fromMessageType.equalsIgnoreCase("pdf")){
            if(fromUserId.equalsIgnoreCase(senderId)){
                holder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.pdffiledefaulticon).into(holder.senderImageView);
                holder.senderFilesDateAndTimeTextView.setText(message.getDate() + "\n" + message.getTime());
                holder.senderFilesDateAndTimeTextView.setVisibility(View.VISIBLE);
            }else{
                holder.recieverImageView.setVisibility(View.VISIBLE);
                holder.recieverProfileImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.pdffiledefaulticon).into(holder.recieverImageView);
                holder.receiverNameTextView.setVisibility(View.VISIBLE);holder.receiverNameTextView.setText(message.getName() + "\n" + message.getDate() + "\n" + message.getTime());
                holder.recieverFilesDateAndTimeTextView.setText(message.getDate() + "\n" + message.getTime());
                holder.recieverFilesDateAndTimeTextView.setVisibility(View.VISIBLE);
            }
        }else if(fromMessageType.equalsIgnoreCase("document")){
            if(fromUserId.equalsIgnoreCase(senderId)){
                holder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.filesdefaulticon).into(holder.senderImageView);
                holder.senderFilesDateAndTimeTextView.setText(message.getDate() + "\n" + message.getTime());
                holder.senderFilesDateAndTimeTextView.setVisibility(View.VISIBLE);
            }else{
                holder.recieverImageView.setVisibility(View.VISIBLE);
                holder.recieverProfileImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(R.drawable.filesdefaulticon).into(holder.recieverImageView);
                holder.receiverNameTextView.setVisibility(View.VISIBLE);holder.receiverNameTextView.setText(message.getName() + "\n" + message.getDate() + "\n" + message.getTime());
                holder.recieverFilesDateAndTimeTextView.setText(message.getDate() + "\n" + message.getTime());
                holder.recieverFilesDateAndTimeTextView.setVisibility(View.VISIBLE);
            }
        }
        Log.d("rock","From is " + fromUserId + " sender is " + senderId);
        if(fromUserId.equalsIgnoreCase(senderId)){
            Log.d("rock","Inside if." + "");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(messageList.get(position).getType().equalsIgnoreCase("pdf") || messageList.get(position).getType().equalsIgnoreCase("document")){
                        CharSequence options[] = {
                                "Delete Message",
                                "Download and View This Document",
                                "Cancel",

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    AlertDialog.Builder ab = new AlertDialog.Builder(holder.itemView.getContext());
                                    ab.setTitle("Are you sure want to delete message?");
                                    ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteSentMessage(position,holder,fromMessageId,fromGroupId);
                                        }
                                    });
                                    ab.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
                                }else if(which == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if(which == 2){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
                    }else if(messageList.get(position).getType().equalsIgnoreCase("text")){
                        CharSequence options[] = {
                                "Delete Message",
                                "Cancel",

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    AlertDialog.Builder ab = new AlertDialog.Builder(holder.itemView.getContext());
                                    ab.setTitle("Are you sure want to delete message?");
                                    ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteSentMessage(position,holder,fromMessageId,fromGroupId);
                                        }
                                    });
                                    ab.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
                                }else if(which == 1){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
                    }else if(messageList.get(position).getType().equalsIgnoreCase("image")){
                        CharSequence options[] =
                                {
                                        "Delete This Image",
                                        "View This Image",
                                        "Cancel",

                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());

                        builder.setTitle("");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    AlertDialog.Builder ab = new AlertDialog.Builder(holder.itemView.getContext());
                                    ab.setTitle("Are you sure want to delete message?");
                                    ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteSentMessage(position,holder,fromMessageId,fromGroupId);
                                        }
                                    });
                                    ab.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
                                }else if(which == 1){
                                    moveToImageViewerActivity(position,holder);
                                }else if(which == 2){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
                    }
                }
            });
        }else{
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(messageList.get(position).getType().equalsIgnoreCase("pdf") || messageList.get(position).getType().equalsIgnoreCase("document")){
                        CharSequence options[] = {
                                "Download and View This Document",
                                "Cancel",

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }else if(which == 1){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
                    }else if(messageList.get(position).getType().equalsIgnoreCase("text")){
                        //Do Nothing
                    }else if(messageList.get(position).getType().equalsIgnoreCase("image")){
                        CharSequence options[] =
                                {
                                        "View This Image",
                                        "Cancel",

                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    moveToImageViewerActivity(position,holder);
                                }else if(which == 1){
                                    Toast.makeText(holder.itemView.getContext(), "Never Mind!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
                    }
                }
            });
        }
        Log.d(TAG, "onBindViewHolder: Name outside of function " + tempRecieverUserName);
    }

    private void moveToImageViewerActivity(int position, GroupMessageViewHolder holder)
    {
        Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
        intent.putExtra("image_url",messageList.get(position).getMessage());
        holder.itemView.getContext().startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void deleteSentMessage(final int pos, final GroupMessageViewHolder holder,String messageKeyId,String grpId)
    {
        databaseRootRef.child("Group_Messages").child(grpId).child(messageKeyId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    Log.d("rock","Error in Deleting Messages!");
                    Toast.makeText(holder.itemView.getContext(), "Failed to delete message :: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public class GroupMessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderTextView,recieverTextView,receiverNameTextView,senderNameTextView,senderFilesDateAndTimeTextView,recieverFilesDateAndTimeTextView;
        public CircleImageView recieverProfileImageView;
        public ImageView senderImageView,recieverImageView;

        public GroupMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = (TextView)itemView.findViewById(R.id.group_custom_message_layout_senderMessage_TextView);
            recieverTextView = (TextView)itemView.findViewById(R.id.group_custom_message_layout_receiverMessage_TextView);
            recieverProfileImageView = (CircleImageView)itemView.findViewById(R.id.group_custom_message_layout_imageView);
            senderImageView = (ImageView)itemView.findViewById(R.id.group_custom_message_layout_sender_imageView);
            recieverImageView = (ImageView)itemView.findViewById(R.id.group_custom_message_layout_reciever_imageView);
            receiverNameTextView = (TextView)itemView.findViewById(R.id.group_custom_message_layout_receiverName_textView);
            senderNameTextView = (TextView)itemView.findViewById(R.id.group_custom_message_layout_senderName_textView);

            senderFilesDateAndTimeTextView = (TextView)itemView.findViewById(R.id.group_custom_message_layout_senderFilesDateAndTime_TextView);
            recieverFilesDateAndTimeTextView = (TextView)itemView.findViewById(R.id.group_custom_message_layout_receiverFilesDateAndTime_TextView);
        }
    }


}
