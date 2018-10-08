package com.example.artem.firebasechatlapitlesson;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView profileName, profileStatus, profileFriendsCount;
    private Button sendReqBtn, declineBtn;

    private FirebaseUser firebaseAuth;

    private String current_state;

    private DatabaseReference usersReference, friendsReqReference, friendsReference, notificationDatabase;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        firebaseAuth = FirebaseAuth.getInstance().getCurrentUser();
        usersReference = FirebaseDatabase.getInstance().getReference().child("ChatUsers").child(user_id);
        friendsReqReference = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friend");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        current_state = "not_friends";

        imageView = (ImageView) findViewById(R.id.profile_image);
        profileName = (TextView) findViewById(R.id.profile_name);
        profileStatus = (TextView) findViewById(R.id.profile_status);
        profileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        sendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        declineBtn = (Button) findViewById(R.id.profile_decline_btn);

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.user_default).into(imageView);

                friendsReqReference.child(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")){

                                current_state = "req_received";
                                sendReqBtn.setText("Accept Friend Request");


                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(true);
                            }else { if (req_type.equals("sent")){
                                current_state = "req_sent";
                                sendReqBtn.setText("Cancel Friend Request");

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }

                            }
                            progressDialog.dismiss();
                        }else{

                            friendsReference.child(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        current_state = "friends";
                                        sendReqBtn.setText("Unfriend this Person");


                                        declineBtn.setVisibility(View.INVISIBLE);
                                        declineBtn.setEnabled(false);

                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_state.equals("not_friends")){

                    sendReqBtn.setEnabled(false);

                    friendsReqReference.child(firebaseAuth.getUid())
                            .child(user_id)
                            .child("request_type")
                            .setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                friendsReqReference.child(user_id)
                                        .child(firebaseAuth.getUid())
                                        .child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notifications = new HashMap<>();
                                        notifications.put("from", firebaseAuth.getUid());
                                        notifications.put("type", "request");

                                        notificationDatabase.child(user_id).push().setValue(notifications).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                current_state = "req_sent";
                                                sendReqBtn.setText("Cancel Friend Request");

                                                declineBtn.setVisibility(View.INVISIBLE);
                                                declineBtn.setEnabled(false);

                                            }
                                        });


                                        Toast.makeText(ProfileActivity.this, "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                            }
                            sendReqBtn.setEnabled(true);
                        }
                    });
                }


                if (current_state.equals("req_sent")){
                    friendsReqReference.child(firebaseAuth.getUid())
                            .child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendsReqReference.child(user_id)
                                    .child(firebaseAuth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendReqBtn.setEnabled(true);
                                    current_state = "not_friends";
                                    sendReqBtn.setText("Send Friend Request");

                                    declineBtn.setVisibility(View.INVISIBLE);
                                    declineBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                if (current_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    friendsReference.child(firebaseAuth.getUid())
                            .child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendsReference.child(user_id).child(firebaseAuth.getUid())
                                    .setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friendsReqReference.child(firebaseAuth.getUid())
                                            .child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            friendsReqReference.child(user_id)
                                                    .child(firebaseAuth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    sendReqBtn.setEnabled(true);
                                                    current_state = "friends";
                                                    sendReqBtn.setText("Unfriend this Person");
                                                    declineBtn.setVisibility(View.INVISIBLE);
                                                    declineBtn.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });

                }
            }
        });

    }
}
