package com.example.artem.firebasechatlapitlesson;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    private CircleImageView circleImageView;
    private TextView user_name, user_status;

    Button btnCngImg, btnCngSts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        circleImageView = (CircleImageView) findViewById(R.id.settings_image);
        user_name = (TextView) findViewById(R.id.txtName);
        user_status = (TextView) findViewById(R.id.txtStatus);
        btnCngImg = (Button) findViewById(R.id.chg_img_btn);
        btnCngSts = (Button) findViewById(R.id.cng_sts_btn);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = firebaseUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("ChatUsers").child(current_uid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                user_name.setText(name);
                user_status.setText(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnCngSts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = user_status.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
                finish();
            }
        });
    }
}
