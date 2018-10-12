package com.example.artem.firebasechatlapitlesson;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatUser, userName;
    private DatabaseReference rootRef;
    private TextView titleUserName, titleLastSeen;
    private CircleImageView userImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();

        chatUser = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setTitle(userName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_bar, null);
        titleUserName = (TextView) view.findViewById(R.id.chat_user_name);
        titleLastSeen = (TextView) view.findViewById(R.id.chat_last_seen);
        userImage = (CircleImageView) view.findViewById(R.id.chat_user_image);

        titleUserName.setText(userName);
        rootRef.child("ChatUsers").child(chatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                if (online.equals("true")){
                    titleLastSeen.setText("Online");
                }else {
                    titleLastSeen.setText("Last seen" + online);
                }
//                userImage
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        actionBar.setCustomView(view);




    }
}
