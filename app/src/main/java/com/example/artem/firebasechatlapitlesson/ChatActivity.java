package com.example.artem.firebasechatlapitlesson;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.format.DateFormat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final int GALLERY_PICK =1 ;
    private String chatUser, userName, currentUserId;
    private DatabaseReference rootRef;
    private StorageReference imageStorage;
    private FirebaseAuth auth;
    private TextView titleUserName, titleLastSeen;
    private CircleImageView userImage;
    private ImageButton chatAddButton, chatSendButton;
    private EditText chatMessage;
    private RecyclerView recyclerView;
    private List<Messages> messagesList;
    private LinearLayoutManager layoutManager;
    private MessageAdapter messageAdapter;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;
    private SwipeRefreshLayout swipeRefresh;
    private String lastKey = "";
    private String prevKey = "";
    private int itemPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatAddButton = (ImageButton) findViewById(R.id.chat_add_btn);
        chatSendButton = (ImageButton) findViewById(R.id.chat_send_btn);
        chatMessage = (EditText) findViewById(R.id.chat_message_view);
        userImage = (CircleImageView) findViewById(R.id.chat_user_image);
        recyclerView = (RecyclerView) findViewById(R.id.messages_list);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        messagesList = new ArrayList<>();
        rootRef = FirebaseDatabase.getInstance().getReference();
        imageStorage = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        chatUser = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_bar, null);

        titleUserName = (TextView) view.findViewById(R.id.chat_user_name);
        titleLastSeen = (TextView) view.findViewById(R.id.chat_last_seen);
        userImage = (CircleImageView) view.findViewById(R.id.chat_user_image);

        messageAdapter = new MessageAdapter(messagesList);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);

        loadMessages();

        titleUserName.setText(userName);
        rootRef.child("ChatUsers").child(chatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                if (online.equals("true")){
                    titleLastSeen.setText("Online");
                }else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    Long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    titleLastSeen.setText("Last Seen: " + lastSeenTime);
                }
                Picasso.get().load(image).placeholder(R.drawable.user_default).into(userImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        actionBar.setCustomView(view);

        rootRef.child("ChatUsers").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(chatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + currentUserId + "/" + chatUser, chatAddMap);
                    chatUserMap.put("Chat/" + chatUser + "/" + currentUserId, chatAddMap);

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null){
                                Log.d("mLog", databaseError.getMessage().toString());

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        chatAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;

                itemPos = 0;

                loadMoreMessages();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            Log.i("mLog", imageUri + " Chat");
            final String current_user_ref = "message/" + currentUserId + "/" + chatUser;
            final String chat_user_ref = "message/" + chatUser + "/" + currentUserId;

            DatabaseReference userMessagePush = rootRef.child("message").child(currentUserId).child(chatUser).push();
            final String push_id = userMessagePush.getKey();

            final StorageReference filepath = imageStorage.child("message_image").child(push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String download_url = uri.toString();

                                Map messageMap = new HashMap();
                                messageMap.put("message", download_url);
                                messageMap.put("seen", false);
                                messageMap.put("type", "image");
                                messageMap.put("time", ServerValue.TIMESTAMP);
                                messageMap.put("from", currentUserId);

                                Map messageUserMap = new HashMap();
                                messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                                messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                                chatMessage.setText("");
                                rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                        if (databaseError != null){
                                            Log.d("mLog", databaseError.getMessage().toString());
                                        }
                                    }
                                });
                            }
                        });

                    }
                }
            });
        }
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = rootRef.child("message").child(currentUserId).child(chatUser);

        Query messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                if(!prevKey.equals(messageKey)){
                    messagesList.add(itemPos++, message);
                } else {
                    prevKey = lastKey;
                }

                if(itemPos == 1) {
                    lastKey = messageKey;
                }


                Log.i("mLog", "Last Key: " + lastKey + "|Prev Key : " + prevKey + "|Message Key : " + messageKey);

                messageAdapter.notifyDataSetChanged();

                swipeRefresh.setRefreshing(false);

                layoutManager.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = rootRef.child("message").child(currentUserId).child(chatUser);

        Query messageQuery = messageRef.limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    lastKey = messageKey;
                    prevKey = messageKey;

                }

                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();

                recyclerView.scrollToPosition(messagesList.size() - 1);

                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {

        String message = chatMessage.getText().toString();
        if (!TextUtils.isEmpty(message)){
            String current_user_ref = "message/" + currentUserId + "/" + chatUser;
            String chat_user_ref = "message/" + chatUser + "/" + currentUserId;

            DatabaseReference user_message_push = rootRef.child("messages").child(currentUserId).child(chatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message );
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", currentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            chatMessage.setText("");

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if (databaseError != null){
                    Log.d("mLog", databaseError.getMessage().toString());
                    }
                }
            });

        }
    }


}
