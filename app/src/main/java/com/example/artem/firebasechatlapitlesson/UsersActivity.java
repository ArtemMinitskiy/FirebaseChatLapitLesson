package com.example.artem.firebasechatlapitlesson;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private DatabaseReference usersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("ChatUsers");

        recyclerUsers = (RecyclerView) findViewById(R.id.recyclerUsers);
        recyclerUsers.setHasFixedSize(true);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(usersDatabase, Users.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users users) {
                holder.setUser_name(users.getName());
                holder.setUser_status(users.getStatus());
//                holder.setUser_image(users.getThumb_image(), getApplicationContext());
            }

            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_item, parent, false);

                return new UsersViewHolder(view);
            }


        };

        adapter.startListening();
        recyclerUsers.setAdapter(adapter);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(UsersActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        private TextView user_name, user_status;
        View view;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setUser_name(String name){
            user_name = (TextView) itemView.findViewById(R.id.user_name);
            user_name.setText(name);
        }
        public void setUser_status(String status){
            user_status = (TextView) itemView.findViewById(R.id.user_status);
            user_status.setText(status);
        }
//        public void setUser_image(String name, Context applicationContext){
//            CircleImageView userImageView = (CircleImageView) itemView.findViewById(R.id.user_image);
//
//            Picasso.get().load(thumb_image).placeholder(R.drawable.user_default).into(userImageView);
//
//        }

    }
}

