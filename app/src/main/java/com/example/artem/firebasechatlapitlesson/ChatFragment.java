package com.example.artem.firebasechatlapitlesson;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference convDatabase, messageDatabase, usersDatabase;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private View view;


    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.chat_recycler);
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();
        convDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(current_user_id);
        convDatabase.keepSynced(true);
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("ChatUsers");
        messageDatabase = FirebaseDatabase.getInstance().getReference().child("message").child(current_user_id);
        usersDatabase.keepSynced(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = convDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(query, Conv.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_item, viewGroup, false);

                return new ConvViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final Conv model) {
                final String list_user_id = getRef(position).getKey();
                Query lastmessageQuery = messageDatabase.child(list_user_id).limitToLast(1);
                lastmessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String dataMessage = dataSnapshot.child("message").getValue().toString();
                        String dataType = dataSnapshot.child("type").getValue().toString();
                        holder.setMessage(dataMessage, dataType, model.isSeen());

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

                usersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);
                        }
                        holder.setName(userName);
                        holder.setImage(userThumb, getContext());
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    public class ConvViewHolder extends RecyclerView.ViewHolder {
        View view;
        public ConvViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

        }

        public void setMessage(String message, String type, boolean isSeen) {
            TextView userStatusView = (TextView) view.findViewById(R.id.user_status);

            if (type.equals("text")){
                userStatusView.setText(message);
            }else {
                if (type.equals("image")){
                    userStatusView.setText("image");
                }
            }

            if (!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            }else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }
        }
        public void setName(String name) {
            TextView userNameView = (TextView) view.findViewById(R.id.user_name);
            userNameView.setText(name);
        }
        public void setImage(String image, Context context) {
            CircleImageView userImageView = (CircleImageView) view.findViewById(R.id.user_image);
            Picasso.get().load(image).placeholder(R.drawable.user_default).into(userImageView);
        }
        public void setUserOnline(String online) {
            ImageView userOnlineView = (ImageView) view.findViewById(R.id.ic_online);
            if (online.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }


    }
}
