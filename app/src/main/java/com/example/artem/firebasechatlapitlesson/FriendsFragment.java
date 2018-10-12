package com.example.artem.firebasechatlapitlesson;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {

    private RecyclerView friendRecyclerView;
    private DatabaseReference friendDateDatabase,friendDataBase;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    private View view;

    public FriendsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_friends, container, false);

        friendRecyclerView = (RecyclerView) view.findViewById(R.id.friends_list);
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        friendDateDatabase = FirebaseDatabase.getInstance().getReference().child("Friend").child(current_user_id);
        friendDateDatabase.keepSynced(true);
        friendDataBase = FirebaseDatabase.getInstance().getReference().child("ChatUsers");
        friendDataBase.keepSynced(true);

        friendRecyclerView.setHasFixedSize(true);
        friendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(friendDateDatabase, Friends.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends friends) {
                holder.setDate(friends.getDate());

                final String list_user_id = getRef(position).getKey();

                friendDataBase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String friendsName = dataSnapshot.child("name").getValue().toString();
                        String friendsImage = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")){
                            String friendsOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setFriend_online(friendsOnline);
                        }

                        holder.setFriend_name(friendsName);
                        holder.setFriend_image(friendsImage, getContext());

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {

                                        if (position == 0){
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if (position == 1){
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", friendsName);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_item, parent, false);

                return new FriendsViewHolder(view);
            }

        };
        adapter.startListening();
        friendRecyclerView.setAdapter(adapter);
    }


    public class FriendsViewHolder extends RecyclerView.ViewHolder {

        View view;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
        public void setDate(String date){
            TextView userNameView = (TextView) itemView.findViewById(R.id.user_status);
            userNameView.setText(date);
        }
        public void setFriend_name(String name){
            TextView friend_name = (TextView) itemView.findViewById(R.id.user_name);
            friend_name.setText(name);
        }
        public void setFriend_image(String friend_image, Context context){
            CircleImageView userImageView = (CircleImageView) itemView.findViewById(R.id.user_image);
            Picasso.get().load(friend_image).placeholder(R.drawable.user_default).into(userImageView);
        }
        public void setFriend_online(String friend_online){
            ImageView onlineImage = (ImageView) view.findViewById(R.id.ic_online);
            if (friend_online.equals("true")){

                onlineImage.setVisibility(View.VISIBLE);
            }else {
                onlineImage.setVisibility(View.INVISIBLE);
            }
        }

    }
}
