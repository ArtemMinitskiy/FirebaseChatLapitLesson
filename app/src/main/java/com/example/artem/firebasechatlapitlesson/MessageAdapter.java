package com.example.artem.firebasechatlapitlesson;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> messagesList;
    private DatabaseReference userDatabase;

    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item, viewGroup, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
//        firebaseAuth = FirebaseAuth.getInstance();
//        String current_user_id = firebaseAuth.getCurrentUser().getUid();
        Messages messages = messagesList.get(position);
        String from_user = messages.getFrom();
        String message_type = messages.getType();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("ChatUsers").child(from_user);
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                holder.nameUser.setText(name);
                Picasso.get().load(image).placeholder(R.drawable.user_default).into(holder.userImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (message_type.equals("text")){
            holder.messageText.setText(messages.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }else {
            holder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(messages.getMessage()).placeholder(R.drawable.user_default).into(holder.messageImage);
        }

//        String from_user = messages.getFrom();
//        if (from_user.equals(current_user_id)) {
//            holder.messageText.getResources().getColor(R.color.messageFromColor);
//            holder.messageText.setTextColor(Color.BLACK);
//        }else {
//            holder.messageText.setBackgroundResource(R.drawable.text_background);
//            holder.messageText.setTextColor(Color.WHITE);
//        }
        holder.messageText.setText(messages.getMessage());
//        holder.nameUser.setText(messages);
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView messageText, nameUser;
        CircleImageView userImage;
        ImageView messageImage;
        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_view);
            nameUser = (TextView) view.findViewById(R.id.name_view);
            userImage = (CircleImageView) view.findViewById(R.id.user_view);
            messageImage = (ImageView) view.findViewById(R.id.message_image);
        }
    }
}
