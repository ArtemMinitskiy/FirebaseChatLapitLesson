package com.example.artem.firebasechatlapitlesson;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String user_id = getIntent().getStringExtra("user_id");

        profileName = (TextView) findViewById(R.id.profile_name);
        profileName.setText(user_id);

    }
}
