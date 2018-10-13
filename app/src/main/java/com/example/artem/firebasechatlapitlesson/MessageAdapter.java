package com.example.artem.firebasechatlapitlesson;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> messagesList;
    private FirebaseAuth firebaseAuth;

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
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        firebaseAuth = FirebaseAuth.getInstance();
        String current_user_id = firebaseAuth.getCurrentUser().getUid();
        Log.i("mLog", current_user_id + " 1");
        Messages messages = messagesList.get(position);
        String from_user = messages.getFrom();
        Log.e("mLog", from_user + " 2");
        Log.i("mLog", messages.getMessage());
        if (from_user.equals(current_user_id)) {
            holder.messageText.getResources().getColor(R.color.messageFromColor);
            holder.messageText.setTextColor(Color.BLACK);
        }else {
            holder.messageText.setBackgroundResource(R.drawable.text_background);
            holder.messageText.setTextColor(Color.WHITE);
        }
        holder.messageText.setText(messages.getMessage());
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView messageText;
        CircleImageView userImage;
        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_view);
            userImage = (CircleImageView) view.findViewById(R.id.user_view);
        }
    }
}
